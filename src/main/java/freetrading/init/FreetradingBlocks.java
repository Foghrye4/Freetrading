package freetrading.init;

import freetrading.block.BlockVillageMarket;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import static freetrading.FreeTradingMod.*;

public class FreetradingBlocks {
	public static Block COUNTER;

	public FreetradingBlocks() {
		COUNTER = (new BlockVillageMarket(Material.WOOD)).setHardness(0.5F).setResistance(5.0F).setUnlocalizedName("counter")
				.setRegistryName(MODID, "counter").setCreativeTab(CreativeTabs.DECORATIONS);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
			event.getRegistry().register(COUNTER);
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemBlock(COUNTER).setRegistryName(COUNTER.getRegistryName()));
	}

	public static void registerRenders() {
		registerRender(COUNTER, 0, COUNTER.getRegistryName());
	}

	@SideOnly(value=Side.CLIENT)
	private static void registerRender(Block block, int metadata, ResourceLocation modelResourceLocation) {
		RenderItem ritem = Minecraft.getMinecraft().getRenderItem();
		ItemModelMesher mesher = ritem.getItemModelMesher();
		mesher.register(Item.getItemFromBlock(block), metadata,
				new ModelResourceLocation(modelResourceLocation, "inventory"));
	}
}
