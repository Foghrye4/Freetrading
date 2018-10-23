package freetrading.client.network;

import java.util.ArrayList;
import java.util.List;

import freetrading.client.gui.GuiFreeTradingMerchant;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;

public class TaskShowGui implements Runnable {

	private final EntityVillager villager;
	private List<TradeOffer> toList;
	private List<TradeOffer> playerToList;

	public TaskShowGui(EntityVillager villagerIn, List<TradeOffer> toListIn, List<TradeOffer> playerToListIn) {
		villager = villagerIn;
		toList = toListIn;
		playerToList = playerToListIn;
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if(mc.currentScreen==null) {
			mc.displayGuiScreen(new GuiFreeTradingMerchant(player, villager));
		}
		InventoryFreeTradingMerchant iftm = (InventoryFreeTradingMerchant) player.openContainer;
		iftm.tradeOffers.clear();
		iftm.tradeOffers.addAll(toList);
		iftm.playerTradeOffers.clear();
		iftm.playerTradeOffers.addAll(playerToList);
		mc.currentScreen.initGui();
	}

}
