package freetrading.item.crafting;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static freetrading.FreeTradingMod.*;

import freetrading.init.FreetradingBlocks;

public class CraftingHandler {

	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		NonNullList<Ingredient> counterRecipeIngridients = NonNullList.from(Ingredient.EMPTY, 
				Ingredient.EMPTY,
				Ingredient.fromStacks(new ItemStack(Items.SIGN, 1, 0)), 
				Ingredient.EMPTY, 
				Ingredient.EMPTY,
				Ingredient.fromStacks(new ItemStack(Blocks.WOOL, 1, EnumDyeColor.LIGHT_BLUE.getMetadata())),
				Ingredient.EMPTY, 
				Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.EMPTY);
		ShapedRecipes counterRecipe = new ShapedRecipes(MODID+":shaped", 3, 3, counterRecipeIngridients, new ItemStack(FreetradingBlocks.COUNTER,1));
		counterRecipe.setRegistryName(new ResourceLocation(MODID,"counter"));
		event.getRegistry().register(counterRecipe);
	}
}
