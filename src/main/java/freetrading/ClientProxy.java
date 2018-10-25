package freetrading;

import freetrading.client.gui.GuiEventHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends ServerProxy {
	
	@Override
	public void registerEvents() {
		super.registerEvents();
		MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
	}

}
