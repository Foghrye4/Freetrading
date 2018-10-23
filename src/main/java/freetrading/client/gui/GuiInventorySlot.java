package freetrading.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradeOffer;
import freetrading.trading_system.TradingSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiInventorySlot {
	private final static ResourceLocation bg = new ResourceLocation(FreeTradingMod.MODID,"textures/gui/container/villager.png");
	public ItemStack stack = ItemStack.EMPTY;
	public final int x;
	public final int y;
	public int tradeTier = 0;
	public int price = 0;
	public final List<TradeOffer> inventory;
	public final int slotIndex;
	private final RenderItem renderItem;
	private boolean isSelected;

	public GuiInventorySlot(int x1, int y1, List<TradeOffer> inventoryIn, int slotIndexIn) {
		x = x1;
		y = y1;
		inventory = inventoryIn;
		slotIndex = slotIndexIn;
		renderItem = Minecraft.getMinecraft().getRenderItem();
	}
	
	public void refresh(int careerLevel) {
		if (inventory.size() <= slotIndex) {
			stack = ItemStack.EMPTY;
			tradeTier = 0;
			price = 0;
			return;
		}
		TradeOffer tradeOffer = inventory.get(slotIndex);
		stack = tradeOffer.stack;
		if (careerLevel < tradeOffer.level)
			tradeTier = tradeOffer.level;
		else {
			tradeTier = 0;
		}
		price = tradeOffer.price;
	}
	
	public void render(int guiLeft, int guiTop, int mousePosX, int mousePosY) {
		if(stack.isEmpty())
			return;
		int x1 = guiLeft + x;
		int y1 = guiTop + y;
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        RenderHelper.enableGUIStandardItemLighting();
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		if(isSelected) {
			renderItem.zLevel+=100.0f;
			renderItem.renderItemAndEffectIntoGUI(stack, mousePosX, mousePosY);
			renderItem.renderItemOverlayIntoGUI(fontRenderer, stack, mousePosX, mousePosY, null);
			renderItem.zLevel-=100.0f;
		}
		else {
			renderItem.renderItemAndEffectIntoGUI(stack, x1, y1);
			renderItem.renderItemOverlayIntoGUI(fontRenderer, stack, x1, y1, null);
			if(tradeTier>0) {
				currentScreen.mc.renderEngine.bindTexture(bg);
				currentScreen.zLevel += 200.0f;
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
				currentScreen.drawTexturedModalRect(x1, y1, 16*(tradeTier-1), 166, 16, 16);
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				currentScreen.zLevel -= 200.0f;
			}
		}
	}
	
	public void drawToolTip(int guiLeft, int guiTop, int mousePosX, int mousePosY) {
		if(stack.isEmpty())
			return;
		int x1 = guiLeft + x;
		int y1 = guiTop + y;
		int x2 = x1+18;
		int y2 = y1+18;
		boolean hovered  = mousePosX > x1 && mousePosX < x2; 
		hovered  &= mousePosY > y1 && mousePosY < y2; 
		if(hovered) {
			GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
	        net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack);
	        currentScreen.drawHoveringText(currentScreen.getItemToolTip(stack), x1, y1);
	        net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip();
		}
	}

	public void setSelected(boolean b) {
		isSelected = b;
	}
}