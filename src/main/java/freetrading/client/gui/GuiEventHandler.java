package freetrading.client.gui;

import freetrading.ClientNetworkHandler;
import freetrading.FreeTradingMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiEventHandler {
	
	@SubscribeEvent
	public void onGuiOpenEvent(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!(event.getGui() instanceof GuiMerchant))
			return;
		Minecraft mc = Minecraft.getMinecraft();
		Entity entity = mc.objectMouseOver.entityHit;
		if(!(entity instanceof EntityVillager))
			return;
		EntityVillager villager = (EntityVillager) entity;
		if (villager!=null) {
			mc.currentScreen = null;
			ClientNetworkHandler cn = (ClientNetworkHandler) FreeTradingMod.network;
			cn.openMerchantGui(villager);
		}
	}
}


