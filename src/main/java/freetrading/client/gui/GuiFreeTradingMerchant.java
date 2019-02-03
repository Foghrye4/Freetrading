package freetrading.client.gui;

import java.io.IOException;

import freetrading.ClientNetworkHandler;
import freetrading.FreeTradingMod;
import freetrading.ServerNetworkHandler.ServerCommands;
import freetrading.client.gui.slot.GuiInventorySlotTradeOffer;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.trading_system.TradingSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import static freetrading.FreeTradingMod.*;

public class GuiFreeTradingMerchant extends GuiScreen {
	private final static ResourceLocation bg = new ResourceLocation(FreeTradingMod.MODID,"textures/gui/container/villager.png");
    protected int xSize = 255;
    protected int ySize = 166;
	private final static int xMargin = 8;
	private final static int yMerchantSlotsMargin1 = 7;
	private final static int yPlayerSlotsMargin1 = 84;
	private final static int yPlayerSlotsMargin2 = 142;
	private final static int MERCHANT_SLOT_NUM = 3*9;
	private final static int PLAYER_SLOT_NUM = 4*9;
	private final static String[] ROMAN_NUMERALS = new String[] {"0","I","II","III","IV","V","VI","VII","VIII"};
	private final GuiInventorySlotTradeOffer[] merchantInventorySlots = new GuiInventorySlotTradeOffer[MERCHANT_SLOT_NUM];
	private final GuiInventorySlotTradeOffer[] playerInventorySlots = new GuiInventorySlotTradeOffer[PLAYER_SLOT_NUM];
	private InventoryFreeTradingMerchant merchantInventory;
	private EntityVillager merchant;
	private EntityPlayer player;
	private int selectedSlotNum = -1;
	private boolean isSelectedSlotMerchantSlot;
	private String moneyString = "";
	private String priceString = "";
	private String merchantNameString = "";
	private String merchantMoneyString1 = "";
	private String merchantMoneyString2 = "";
	private String merchantLevelString = "";
	private int priceColor = 0x00FF00;
	
	public GuiFreeTradingMerchant(EntityPlayer playerIn, EntityVillager theMerchantIn) {
		super();
		merchant = theMerchantIn;
		player = playerIn;
		merchantInventory = new InventoryFreeTradingMerchant(theMerchantIn);
		playerIn.openContainer = merchantInventory;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int index = j + i * 9;
				merchantInventorySlots[index] = new GuiInventorySlotTradeOffer(xMargin + j * 18, yMerchantSlotsMargin1 + i * 18, merchantInventory.tradeOffers, index);
			}
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int index = j + i * 9 + 9;
				playerInventorySlots[index] = new GuiInventorySlotTradeOffer(xMargin + j * 18, yPlayerSlotsMargin1 + i * 18, merchantInventory.playerTradeOffers, index);
			}
		}
		for (int k = 0; k < 9; ++k) {
			playerInventorySlots[k] = new GuiInventorySlotTradeOffer(xMargin + k * 18, yPlayerSlotsMargin2, merchantInventory.playerTradeOffers, k);
		}
	}
	
	private GuiInventorySlotTradeOffer getSlotFromPos(int x, int y) {
        int x0 = (this.width - this.xSize) / 2;
        int y0 = (this.height - this.ySize) / 2;
        x-=x0+xMargin;
        y-=y0;
        if(y>yPlayerSlotsMargin2) {
        	return playerInventorySlots[x/18];
        }
        else if(y>yPlayerSlotsMargin1) {
            y-=yPlayerSlotsMargin1;
            int index = x/18 + y/18*9+9;
			if (index >= PLAYER_SLOT_NUM)
            	return null;
        	return playerInventorySlots[index];
        }
        else if(y>yMerchantSlotsMargin1) {
            y-=yMerchantSlotsMargin1;
            int index = x/18 + y/18*9;
			if (index >= MERCHANT_SLOT_NUM)
            	return null;
        	return merchantInventorySlots[index];
        }
        
        return null;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		moneyString = I18n.format("freetrading.money", TradingSystem.getMoneyOf(player));
		merchantNameString = I18n.format(merchantInventory.getName());
		merchantMoneyString1 = I18n.format("freetrading.merchant_money");
		merchantMoneyString2 = String.valueOf(TradingSystem.getMoneyOf(merchantInventory.merchant));
		int merchantLevel = merchantInventory.merchant.careerLevel;
		merchantLevelString = I18n.format("freetrading.merchant_level", merchantLevel>=ROMAN_NUMERALS.length?merchantLevel:ROMAN_NUMERALS[merchantLevel]);
		for(GuiInventorySlotTradeOffer slot:playerInventorySlots) {
			slot.refresh(9);
		}
		for(GuiInventorySlotTradeOffer slot:merchantInventorySlots) {
			slot.refresh(merchantInventory.merchant.careerLevel);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
		this.mc.renderEngine.bindTexture(bg);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		Minecraft.getMinecraft().fontRenderer.drawString(moneyString, guiLeft+7, guiTop+64, 0x000000);
		Minecraft.getMinecraft().fontRenderer.drawString(priceString, guiLeft+140, guiTop+64, priceColor);
		Minecraft.getMinecraft().fontRenderer.drawString(merchantNameString, guiLeft+180, guiTop+8, 0x000000);
		Minecraft.getMinecraft().fontRenderer.drawString(merchantMoneyString1, guiLeft+180, guiTop+18, 0x000000);
		Minecraft.getMinecraft().fontRenderer.drawString(merchantMoneyString2, guiLeft+180, guiTop+28, 0x000000);
		Minecraft.getMinecraft().fontRenderer.drawString(merchantLevelString, guiLeft+180, guiTop+38, 0x000000);
		for(GuiInventorySlotTradeOffer slot:merchantInventorySlots) {
			slot.render(guiLeft, guiTop, mouseX, mouseY);
		}
		for(GuiInventorySlotTradeOffer slot:playerInventorySlots) {
			slot.render(guiLeft, guiTop, mouseX, mouseY);
		}
		for(GuiInventorySlotTradeOffer slot:merchantInventorySlots) {
			slot.drawToolTip(guiLeft, guiTop, mouseX, mouseY);
		}
		for(GuiInventorySlotTradeOffer slot:playerInventorySlots) {
			slot.drawToolTip(guiLeft, guiTop, mouseX, mouseY);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		GuiInventorySlotTradeOffer i = getSlotFromPos(mouseX,mouseY);
		if(i==null)
			return;
		if(i.inventory==merchantInventory.tradeOffers) {
			this.onMerchantSlotClick(i.slotIndex);
		}
		else {
			this.onPlayerSlotClick(i.slotIndex);
		}
	}
	
	public void onMerchantSlotClick(int index) {
		if(selectedSlotNum>=0 && isSelectedSlotMerchantSlot) {
			priceString = "";
			merchantInventorySlots[selectedSlotNum].setSelected(false);
			selectedSlotNum = -1;
			this.selectMerchantSlot(index);
			return;
		} else if(selectedSlotNum<0) {
			this.selectMerchantSlot(index);
		}
		else {
			ClientNetworkHandler cnetwork = (ClientNetworkHandler) network;
			cnetwork.sellItem(selectedSlotNum, merchant);
			playerInventorySlots[selectedSlotNum].setSelected(false);
			priceString = "";
			selectedSlotNum = -1;
		}
	}
	
	public void selectMerchantSlot(int index) {
		if (merchantInventorySlots[index].stack.isEmpty() || merchantInventorySlots[index].tradeTier > 0)
			return;
		selectedSlotNum = index;
		isSelectedSlotMerchantSlot = true;
		merchantInventorySlots[index].setSelected(true);
		int price = merchantInventorySlots[index].price;
		priceString = I18n.format("freetrading.buying_price", price);
		priceColor = 0xFF0000;
	}
	
	public void onPlayerSlotClick(int index) {
		if(selectedSlotNum<0) {
			this.selectPlayerSlot(index);
		}
		else if(selectedSlotNum>=0 && isSelectedSlotMerchantSlot){
			ClientNetworkHandler cnetwork = (ClientNetworkHandler) network;
			cnetwork.buyItem(selectedSlotNum, merchant);
			merchantInventorySlots[selectedSlotNum].setSelected(false);
			priceString = "";
			selectedSlotNum = -1;
		}
		else if(selectedSlotNum>=0 && !isSelectedSlotMerchantSlot){
			priceString = "";
			playerInventorySlots[selectedSlotNum].setSelected(false);
			this.selectPlayerSlot(index);
		}
	}
	
	private void selectPlayerSlot(int index) {
		if(playerInventorySlots[index].stack.isEmpty())
			return;
		selectedSlotNum = index;
		isSelectedSlotMerchantSlot = false;
		playerInventorySlots[index].setSelected(true);
		int price = playerInventorySlots[index].price;
		priceString = I18n.format("freetrading.selling_price", price);
		priceColor = 0x00FF00;
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		ClientNetworkHandler cnetwork = (ClientNetworkHandler) network;
		cnetwork.runServerCommand(ServerCommands.CLOSE_VILLAGER_GUI);
	}
}
