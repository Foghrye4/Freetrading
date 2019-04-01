package freetrading.client.network;

import java.util.ArrayList;
import java.util.List;

import freetrading.client.gui.GuiFreeTradingMerchant;
import freetrading.client.gui.GuiPlayerToPlayer;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;

public class TaskNotifyLock implements Runnable {

	public TaskNotifyLock() {
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen instanceof GuiPlayerToPlayer) {
			GuiPlayerToPlayer gp2p = (GuiPlayerToPlayer) mc.currentScreen;
			gp2p.partnerDealIsLocked = true;
		}
	}

}
