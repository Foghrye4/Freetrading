package freetrading;

import java.io.IOException;

import freetrading.common.network.TaskBuyItem;
import freetrading.common.network.TaskOpenContainer;
import freetrading.common.network.TaskSellItem;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import freetrading.trading_system.TradingSystem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
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
		UPDATE_TRADING;
	}

	public enum ServerCommands {
		SELL_ITEM, BUY_ITEM, OPEN_GUI;
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
			case OPEN_GUI:
				villagerId = byteBufInputStream.readInt();
				world.addScheduledTask(new TaskOpenContainer(world, playerEntityId,villagerId));
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
			}
			byteBufInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		byteBufOutputStream.writeInt(player.inventory.getSizeInventory());
		for(int i=0;i<player.inventory.getSizeInventory();i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			byteBufOutputStream.writeItemStack(stack);
			byteBufOutputStream.writeInt(TradingSystem.getLowPriceOf(stack));
		}
		channel.sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void setServer(MinecraftServer serverIn) {
		server = serverIn;
	}
}
