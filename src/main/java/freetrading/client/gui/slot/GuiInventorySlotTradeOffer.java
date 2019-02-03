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

public class GuiInventorySlotTradeOffer extends GuiInventorySlot {
	private final static ResourceLocation bg = new ResourceLocation(FreeTradingMod.MODID,
			"textures/gui/container/villager.png");
	public int tradeTier = 0;
	public int price = 0;
	public final List<TradeOffer> inventory;

	public GuiInventorySlotTradeOffer(int x1, int y1, List<TradeOffer> inventoryIn, int slotIndexIn) {
		super(x1, y1, slotIndexIn);
		inventory = inventoryIn;
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
		super.render(guiLeft, guiTop, mousePosX, mousePosY);
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		int x1 = guiLeft + x;
		int y1 = guiTop + y;
		if (!isSelected && tradeTier > 0) {
			currentScreen.mc.renderEngine.bindTexture(bg);
			currentScreen.zLevel += 200.0f;
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
			currentScreen.drawTexturedModalRect(x1, y1, 16 * (tradeTier - 1), 166, 16, 16);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			currentScreen.zLevel -= 200.0f;
		}
	}
}