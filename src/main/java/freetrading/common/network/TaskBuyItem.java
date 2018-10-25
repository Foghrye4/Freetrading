package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import freetrading.trading_system.TradingSystem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

public class TaskBuyItem extends TaskVillagerInteractionBase {

	private int slotId;

	public TaskBuyItem(WorldServer worldIn, int playerEntityIdIn, int villagerIdIn, int slotIdIn) {
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
		InventoryFreeTradingMerchant merchantInventory = (InventoryFreeTradingMerchant) player.openContainer;

		TradeOffer to = merchantInventory.tradeOffers.get(slotId);
		int tier = to.level;
		if (tier > merchantInventory.merchant.careerLevel) {
			return;
		}
		int price = to.price;
		long playerMoney = TradingSystem.getMoneyOf(player);
		if (price > playerMoney) {
			return;
		}
		playerMoney = TradingSystem.addMoneyTo(player, -price);
		price -= TradingSystem.getLowPriceOf(to.stack);
		player.addItemStackToInventory(to.stack.copy());
		if (price > 0) {
			long money = TradingSystem.addMoneyTo(villager, price);
			villager.careerLevel = (int) (money / 1000) + 1;
		}
		merchantInventory.onCraftMatrixChanged(merchantInventory);
		FreeTradingMod.network.sendPacketUpdateTrading(player);
	}
}
