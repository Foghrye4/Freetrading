package freetrading;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.Logger;

import freetrading.player.TradingSystem;

@Mod(modid = FreeTradingMod.MODID, name = FreeTradingMod.NAME, version = FreeTradingMod.VERSION)
public class FreeTradingMod {
	public static final String MODID = "freetrading";
	public static final String NAME = "Free trading mod";
	public static final String VERSION = "0.1.0";
	
	@SidedProxy(clientSide = "freetrading.ClientNetworkHandler", serverSide = "freetrading.ServerNetworkHandler")
	public static ServerNetworkHandler network;
	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		network.load();
		MinecraftForge.EVENT_BUS.register(TradingSystem.instance);
		logger = event.getModLog();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		network.setServer(event.getServer());
	}
}
