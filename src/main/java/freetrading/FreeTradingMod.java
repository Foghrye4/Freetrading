package freetrading;

import org.apache.logging.log4j.Logger;

import freetrading.command.FreeTradingCommand;
import freetrading.entity.player.PlayerInteractionEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = FreeTradingMod.MODID, name = FreeTradingMod.NAME, version = FreeTradingMod.VERSION)
public class FreeTradingMod {
	public static final String MODID = "freetrading";
	public static final String NAME = "Free trading mod";
	public static final String VERSION = "0.3.6";
	
	@SidedProxy(clientSide = "freetrading.ClientNetworkHandler", serverSide = "freetrading.ServerNetworkHandler")
	public static ServerNetworkHandler network;
	@SidedProxy(clientSide = "freetrading.ClientProxy", serverSide = "freetrading.ServerProxy")
	public static ServerProxy proxy;
	public static Logger logger;
	public static boolean isForgeEssentialsLoaded = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		network.load();
		logger = event.getModLog();
		proxy.registerEvents();
		MinecraftForge.EVENT_BUS.register(new PlayerInteractionEventHandler());
		isForgeEssentialsLoaded = Loader.isModLoaded("forgeessentials");
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		network.setServer(event.getServer());
		event.registerServerCommand(new FreeTradingCommand());
	}
}
