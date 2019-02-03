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

public class TaskCloseGui implements Runnable {

	public TaskCloseGui() {}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(null);
	}

}
