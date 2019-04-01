package freetrading;

import static freetrading.FreeTradingMod.MODID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import freetrading.client.gui.slot.GuiInventorySlotPlayer;
import freetrading.client.network.TaskCloseGui;
import freetrading.client.network.TaskNotifyLock;
import freetrading.client.network.TaskShowPlayerToPlayerGui;
import freetrading.client.network.TaskShowVillagerGui;
import freetrading.client.network.TaskSynchronizeOffer;
import freetrading.trading_system.TradeOffer;
import freetrading.trading_system.TradingSystem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class ClientNetworkHandler extends ServerNetworkHandler {

	@SubscribeEvent
	public void onPacketFromServerToClient(FMLNetworkEvent.ClientCustomPacketEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		WorldClient world = Minecraft.getMinecraft().world;
		ByteBuf data = event.getPacket().payload();
		PacketBuffer byteBufInputStream = new PacketBuffer(data);
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		try {
			int stackAmount;
			switch (ClientCommands.values()[byteBufInputStream.readByte()]) {
			case CLOSE_GUI:
				mc.addScheduledTask(new TaskCloseGui());
				break;
			case SYNCHRONIZE_OFFER:
				long moneyOffer = byteBufInputStream.readLong();
				stackAmount = byteBufInputStream.readInt();
				List<ItemStack> offeredStacks = new ArrayList<ItemStack>();
				while (--stackAmount >= 0) {
					ItemStack stack = byteBufInputStream.readItemStack();
					offeredStacks.add(stack);
				}
				mc.addScheduledTask(new TaskSynchronizeOffer(moneyOffer, offeredStacks));
				break;
			case UPDATE_TRADING:
				player.getEntityData().setLong(TradingSystem.MONEY, byteBufInputStream.readLong());
				EntityVillager villager = (EntityVillager) world.getEntityByID(byteBufInputStream.readInt());
				villager.getEntityData().setLong(TradingSystem.MONEY, byteBufInputStream.readLong());
				int toLength = byteBufInputStream.readInt();
				List<TradeOffer> toList = new ArrayList<TradeOffer>();
				for (int i = 0; i < toLength; i++) {
					ItemStack stack = byteBufInputStream.readItemStack();
					int level = byteBufInputStream.readInt();
					int price = byteBufInputStream.readInt();
					toList.add(new TradeOffer(stack, level, price));
				}
				villager.careerId = byteBufInputStream.readInt();
				villager.careerLevel = byteBufInputStream.readInt();
				stackAmount = byteBufInputStream.readInt();
				List<TradeOffer> playerToList = new ArrayList<TradeOffer>();
				for (int i = 0; i < stackAmount; i++) {
					ItemStack stack = byteBufInputStream.readItemStack();
					int price = byteBufInputStream.readInt();
					playerToList.add(new TradeOffer(stack, 0, price));
				}
				mc.addScheduledTask(new TaskShowVillagerGui(villager, toList, playerToList));
				break;
			case OPEN_PLAYER_TO_PLAYER_GUI:
				player.getEntityData().setLong(TradingSystem.MONEY, byteBufInputStream.readLong());
				mc.addScheduledTask(new TaskShowPlayerToPlayerGui(byteBufInputStream.readInt()));
				break;
			case NOTIFY_LOCK:
				mc.addScheduledTask(new TaskNotifyLock());
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sellItem(int selectedSlot, EntityVillager merchant) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ServerCommands.SELL_ITEM.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		byteBufOutputStream.writeInt(selectedSlot);
		byteBufOutputStream.writeInt(merchant.getEntityId());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}

	public void buyItem(int selectedSlot, EntityVillager merchant) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ServerCommands.BUY_ITEM.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		byteBufOutputStream.writeInt(selectedSlot);
		byteBufOutputStream.writeInt(merchant.getEntityId());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}

	public void openMerchantGui(EntityVillager merchant) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ServerCommands.OPEN_MERCHANT_GUI.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		byteBufOutputStream.writeInt(merchant.getEntityId());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}

	public void runServerCommand(ServerCommands command) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(command.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}

	public void sendPacketUpdateOffer(List<GuiInventorySlotPlayer> slotsInCounter, long yourOfferInt) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ServerCommands.UPDATE_OFFER.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		byteBufOutputStream.writeLong(yourOfferInt);
		byteBufOutputStream.writeInt(slotsInCounter.size());
		for (GuiInventorySlotPlayer slot : slotsInCounter) {
			byteBufOutputStream.writeInt(slot.slotIndex);
		}
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}
}
