package freetrading.client.network;

import java.util.List;

import freetrading.client.gui.GuiPlayerToPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class TaskSynchronizeOffer implements Runnable {

	private final long moneyOffer;
	private final List<ItemStack> stacksOffer;

	public TaskSynchronizeOffer(long moneyOfferIn, List<ItemStack> stacksOfferIn) {
		moneyOffer = moneyOfferIn;
		stacksOffer = stacksOfferIn;
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		if(!(mc.currentScreen instanceof GuiPlayerToPlayer))
			return;
		GuiPlayerToPlayer gui = (GuiPlayerToPlayer) mc.currentScreen;
		gui.partnerItemStackOffer.clear();
		gui.partnerItemStackOffer.addAll(stacksOffer);
		gui.partnerOfferInt = moneyOffer;
		gui.refreshPartnerItemStackOffer();
	}
}
