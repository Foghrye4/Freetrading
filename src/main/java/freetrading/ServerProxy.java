package freetrading;

import freetrading.trading_system.TradingSystem;
import net.minecraftforge.common.MinecraftForge;

public class ServerProxy {

	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(TradingSystem.instance);
	}

}
