package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradingSystem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

public class TaskSellItem extends TaskVillagerInteractionBase {

	private int slotId;

	public TaskSellItem(WorldServer worldIn, int playerEntityIdIn, int villagerIdIn, int slotIdIn) {
		super(worldIn, playerEntityIdIn, villagerIdIn);
		slotId = slotIdIn;
	}

	@Override
	public void run() {
		if(!this.getAndCheckEntities())
			return;
		if (!(player.openContainer instanceof InventoryFreeTradingMerchant)) {
			return;
		}
		ItemStack stack = player.inventory.removeStackFromSlot(slotId);
		int price = TradingSystem.getLowPriceOf(stack);
		TradingSystem.addMoneyTo(player, price);
		FreeTradingMod.network.sendPacketUpdateTrading(player);
	}
}
