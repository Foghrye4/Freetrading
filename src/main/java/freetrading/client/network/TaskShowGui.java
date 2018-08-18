package freetrading.client.network;

import java.util.ArrayList;
import java.util.List;

import freetrading.client.gui.GuiFreeTradingMerchant;
import freetrading.inventory.InventoryFreeTradingMerchant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;

public class TaskShowGui implements Runnable {

	private final List<ItemStack> containerContent;
	private final EntityVillager villager;
	private final int[] itemTiers;

	public TaskShowGui(EntityVillager villagerIn, List<ItemStack> containerContentIn, int[] itemTiersIn) {
		villager = villagerIn;
		containerContent = containerContentIn;
		itemTiers = itemTiersIn;
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if(mc.currentScreen==null) {
			mc.displayGuiScreen(new GuiFreeTradingMerchant(player, villager));
		}
		player.openContainer.inventoryItemStacks.clear();
		player.openContainer.inventoryItemStacks.addAll(containerContent);
		InventoryFreeTradingMerchant iftm = (InventoryFreeTradingMerchant) player.openContainer;
		for(int i=0;i<iftm.itemTierLevel.length;i++) {
			iftm.itemTierLevel[i] = itemTiers[i];
		}
		mc.currentScreen.initGui();
	}

}
