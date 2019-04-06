package freetrading;

import static freetrading.FreeTradingMod.MODID;

import freetrading.client.Icon;
import freetrading.client.gui.GuiEventHandler;
import freetrading.client.renderer.SpecialRendererRegistry;
import freetrading.client.renderer.TileEntityVillageMarketRenderer;
import freetrading.init.FreetradingBlocks;
import freetrading.tileentity.TileEntityVillageMarket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends ServerProxy {
	private final TileEntityVillageMarketRenderer vmr = new TileEntityVillageMarketRenderer();
	public static final Icon EMERALD_SHARD = SpecialRendererRegistry.registerIcon(new ResourceLocation(MODID + ":items/emerald_shard"));
	public static final GuiEventHandler guiHandler = new GuiEventHandler();
	
	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(guiHandler);
		MinecraftForge.EVENT_BUS.register(SpecialRendererRegistry.instance);
		MinecraftForge.EVENT_BUS.register(vmr);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVillageMarket.class, vmr);
	}

	@Override
	public void init() {
		FreetradingBlocks.registerRenders();
		vmr.setRenders();
	}
}
