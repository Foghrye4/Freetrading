package freetrading.client.gui.slot;

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
	public ItemStack stack = ItemStack.EMPTY;
	public final int x;
	public final int y;
	public final int slotIndex;
	private final RenderItem renderItem;
	protected boolean isSelected;

	public GuiInventorySlot(int x1, int y1, int slotIndexIn) {
		x = x1;
		y = y1;
		slotIndex = slotIndexIn;
		renderItem = Minecraft.getMinecraft().getRenderItem();
	}
	
	public void render(int guiLeft, int guiTop, int mousePosX, int mousePosY) {
		if(stack.isEmpty())
			return;
		int x1 = guiLeft + getX();
		int y1 = guiTop + getY();
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
		}
	}
	
	public void drawToolTip(int guiLeft, int guiTop, int mousePosX, int mousePosY) {
		if(stack.isEmpty())
			return;
		int x1 = guiLeft + getX();
		int y1 = guiTop + getY();
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
	
	protected int getX() {
		return x;
	}
	protected int getY() {
		return y;
	}
}