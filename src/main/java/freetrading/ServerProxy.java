package freetrading;

import freetrading.entity.player.EntityEventHandler;
import freetrading.item.crafting.CraftingHandler;
import freetrading.trading_system.TradingSystem;
import net.minecraftforge.common.MinecraftForge;

public class ServerProxy {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(TradingSystem.instance);
		MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
		MinecraftForge.EVENT_BUS.register(new CraftingHandler());
	}

	public void init() {}
}
