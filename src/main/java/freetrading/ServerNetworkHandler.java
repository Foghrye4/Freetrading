package freetrading;

import java.io.IOException;
import java.util.List;

import freetrading.common.network.TaskAbortDeal;
import freetrading.common.network.TaskAcceptDealAndLockInventory;
import freetrading.common.network.TaskBuyItem;
import freetrading.common.network.TaskCloseVillagerGUI;
import freetrading.common.network.TaskOpenMerchantContainer;
import freetrading.common.network.TaskOpenPlayerToPlayerContainer;
import freetrading.common.network.TaskSellItem;
import freetrading.common.network.TaskUpdateOffer;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import freetrading.trading_system.TradingSystem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import static freetrading.FreeTradingMod.*;

public class ServerNetworkHandler {
	public enum ClientCommands {
		UPDATE_TRADING, OPEN_PLAYER_TO_PLAYER_GUI, SYNCHRONIZE_OFFER, CLOSE_GUI, NOTIFY_LOCK, UPDATE_OVERLAY_BALANCE;
	}

	public enum ServerCommands {
		SELL_ITEM, BUY_ITEM, OPEN_MERCHANT_GUI, OPEN_PLAYER_TO_PLAYER_CONTAINER, ACCEPT_DEAL_AND_LOCK_INVENTORY, ABORT_DEAL, CLOSE_VILLAGER_GUI, UPDATE_OFFER, REQUEST_PLAYER_MONEY_INFO;
	}

	protected static FMLEventChannel channel;
	private MinecraftServer server;

	public void load() {
		if (channel == null) {
			channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);
			channel.register(this);
		}
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onPacketFromClientToServer(FMLNetworkEvent.ServerCustomPacketEvent event) {
		ByteBuf data = event.getPacket().payload();
		ByteBufInputStream byteBufInputStream = new ByteBufInputStream(data);
		int playerEntityId;
		int worldDimensionId;
		int slotId;
		WorldServer world;
		try {
			ServerCommands command = ServerCommands.values()[byteBufInputStream.read()];
			playerEntityId = byteBufInputStream.readInt();
			worldDimensionId = byteBufInputStream.readInt();
			world = server.getWorld(worldDimensionId);
			if(world==null) {
				byteBufInputStream.close();
				throw new NullPointerException("There is no world for dimension "+worldDimensionId);
			}
			int villagerId = 0;
			switch (command) {
			case ABORT_DEAL:
				world.addScheduledTask(new TaskAbortDeal(world, playerEntityId));
				break;
			case ACCEPT_DEAL_AND_LOCK_INVENTORY:
				world.addScheduledTask(new TaskAcceptDealAndLockInventory(world, playerEntityId));
				break;
			case OPEN_PLAYER_TO_PLAYER_CONTAINER:
				int otherPlayerID = byteBufInputStream.readInt();
				world.addScheduledTask(new TaskOpenPlayerToPlayerContainer(world, playerEntityId, otherPlayerID));
				break;
			case CLOSE_VILLAGER_GUI:
				world.addScheduledTask(new TaskCloseVillagerGUI(world, playerEntityId));
				break;
			case OPEN_MERCHANT_GUI:
				villagerId = byteBufInputStream.readInt();
				world.addScheduledTask(new TaskOpenMerchantContainer(world, playerEntityId,villagerId));
				break;
			case SELL_ITEM:
				slotId = byteBufInputStream.readInt();
				villagerId = byteBufInputStream.readInt();
				world.addScheduledTask(new TaskSellItem(world, playerEntityId,villagerId, slotId));
				break;
			case BUY_ITEM:
				slotId = byteBufInputStream.readInt();
				villagerId = byteBufInputStream.readInt();
				world.addScheduledTask(new TaskBuyItem(world, playerEntityId,villagerId, slotId));
				break;
			case UPDATE_OFFER:
				long newMoneyOffer = byteBufInputStream.readLong();
				int slotsNum = byteBufInputStream.readInt();
				int[] slots = new int[slotsNum];
				for(int i=0;i<slotsNum;i++) {
					slots[i] = byteBufInputStream.readInt();
				}
				world.addScheduledTask(new TaskUpdateOffer(world, playerEntityId, newMoneyOffer, slots));
				break;
			case REQUEST_PLAYER_MONEY_INFO:
				world.addScheduledTask(() -> {
					Entity player = world.getEntityByID(playerEntityId);
					if(!(player instanceof EntityPlayerMP))
						return;
					this.sendPacketUpdatePlayerBalance((EntityPlayerMP) player,0L);
				});
				break;
			default:
				break;
			}
			byteBufInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendPacketUpdatePlayerBalance(EntityPlayerMP player, long balance) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.UPDATE_OVERLAY_BALANCE.ordinal());
		byteBufOutputStream.writeLong(TradingSystem.getMoneyOf(player));
		byteBufOutputStream.writeLong(balance);
		channel.sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), (EntityPlayerMP) player);
	}
	
	public void sendPacketUpdateTrading(EntityPlayerMP player) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.UPDATE_TRADING.ordinal());
		byteBufOutputStream.writeLong(TradingSystem.getMoneyOf(player));
		InventoryFreeTradingMerchant merchantInventory = (InventoryFreeTradingMerchant) player.openContainer;
		byteBufOutputStream.writeInt(merchantInventory.merchant.getEntityId());
		byteBufOutputStream.writeLong(TradingSystem.getMoneyOf(merchantInventory.merchant));
		byteBufOutputStream.writeInt(merchantInventory.tradeOffers.size());
		for(int i=0;i<merchantInventory.tradeOffers.size();i++) {
			TradeOffer to = merchantInventory.tradeOffers.get(i);
			byteBufOutputStream.writeItemStack(to.stack);
			byteBufOutputStream.writeInt(to.level);
			byteBufOutputStream.writeInt(to.price);
		}
		byteBufOutputStream.writeInt(merchantInventory.merchant.careerId);
		byteBufOutputStream.writeInt(merchantInventory.merchant.careerLevel);
		NonNullList<ItemStack> main = player.inventory.mainInventory;
		byteBufOutputStream.writeInt(main.size());
		for(int i=0;i<main.size();i++) {
			ItemStack stack = main.get(i);
			byteBufOutputStream.writeItemStack(stack);
			byteBufOutputStream.writeInt(TradingSystem.getLowPriceOf(stack));
		}
		channel.sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void setServer(MinecraftServer serverIn) {
		server = serverIn;
	}

	public void sendPacketOpenPlayerToPlayerGUI(EntityPlayerMP player, int otherPlayerID) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.OPEN_PLAYER_TO_PLAYER_GUI.ordinal());
		byteBufOutputStream.writeLong(TradingSystem.getMoneyOf(player));
		byteBufOutputStream.writeInt(otherPlayerID);
		channel.sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void sendPacketOfferUpdate(EntityPlayerMP partner, long moneyOffer, List<ItemStack> offeredStacks) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.SYNCHRONIZE_OFFER.ordinal());
		byteBufOutputStream.writeLong(moneyOffer);
		byteBufOutputStream.writeInt(offeredStacks.size());
		for(ItemStack stack:offeredStacks) {
			byteBufOutputStream.writeItemStack(stack);
		}
		FMLProxyPacket packet = new FMLProxyPacket(byteBufOutputStream, MODID);
		channel.sendTo(packet, partner);
	}

	public void sendCommandCloseGUI(EntityPlayerMP owner) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.CLOSE_GUI.ordinal());
		FMLProxyPacket packet = new FMLProxyPacket(byteBufOutputStream, MODID);
		channel.sendTo(packet, owner);
	}

	public void sendPacketPartnerLockHisDeal(EntityPlayerMP partner) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.NOTIFY_LOCK.ordinal());
		FMLProxyPacket packet = new FMLProxyPacket(byteBufOutputStream, MODID);
		channel.sendTo(packet, partner);
	}
}
