package freetrading.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import freetrading.ClientNetworkHandler;
import freetrading.FreeTradingMod;
import freetrading.ServerNetworkHandler.ServerCommands;
import freetrading.client.gui.slot.GuiInventorySlot;
import freetrading.client.gui.slot.GuiInventorySlotCounter;
import freetrading.client.gui.slot.GuiInventorySlotPlayer;
import freetrading.client.gui.slot.GuiInventorySlotTradeOffer;
import freetrading.inventory.ContainerPlayerToPlayer;
import freetrading.trading_system.TradingSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiPlayerToPlayer extends GuiScreen {

	private final static ResourceLocation bg = new ResourceLocation(FreeTradingMod.MODID,
			"textures/gui/container/player_to_player.png");
	private final static int COUNTER_SLOT_NUM = 3 * 4;
	private final static int PLAYER_SLOT_NUM = 4 * 9;
	private final GuiInventorySlotCounter[] partnerInventorySlots = new GuiInventorySlotCounter[COUNTER_SLOT_NUM];
	private final GuiInventorySlotPlayer[] playerInventorySlots = new GuiInventorySlotPlayer[PLAYER_SLOT_NUM];
	protected int xSize = 255;
	protected int ySize = 166;
	private final static int xMargin = 8;
	private final static int yMargin = 8;
	private final static int xPartnerMargin = 96;
	private final static int yPlayerSlotsMargin1 = 84;
	private final static int yPlayerSlotsMargin2 = 142;
	GuiCounter playerCounter = new GuiCounter(xMargin,yMargin,3,4);
	GuiButton seal;
	GuiButton abort;
	GuiTextField moneyOfferInput;
	private String moneyString = "";
	private int yourOfferInt = 0;
	public long partnerOfferInt = 0;
	private String yourOffer = "";
	private String partnerOffer = "";
	public final List<ItemStack> partnerItemStackOffer = new ArrayList<ItemStack>();
	int prevMoneyOffer = -1;

	public GuiPlayerToPlayer(EntityPlayer playerIn) {
		super();
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 4; ++column) {
				int index = column + row * 4;
				partnerInventorySlots[index] = new GuiInventorySlotCounter(xPartnerMargin + column * 18,
						yMargin + row * 18, index, partnerItemStackOffer);
			}
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int index = j + i * 9 + 9;
				playerInventorySlots[index] = new GuiInventorySlotPlayer(xMargin + j * 18, yPlayerSlotsMargin1 + i * 18,
						index, playerIn.inventory, playerCounter);
			}
		}
		for (int k = 0; k < 9; ++k) {
			playerInventorySlots[k] = new GuiInventorySlotPlayer(xMargin + k * 18, yPlayerSlotsMargin2, k,
					playerIn.inventory, playerCounter);
		}
	}
	
	private GuiInventorySlotPlayer getSlotFromPos(int x, int y) {
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
        return null;
	}


	public void initGui() {
		super.initGui();
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.yourOffer = I18n.format("freetrading.your_offer",
				TradingSystem.getMoneyOf(Minecraft.getMinecraft().player));
		this.partnerOffer = I18n.format("freetrading.partner_offer",
				TradingSystem.getMoneyOf(Minecraft.getMinecraft().player));
		this.moneyString = I18n.format("freetrading.money", TradingSystem.getMoneyOf(Minecraft.getMinecraft().player));
		this.seal = new GuiButton(0, guiLeft + 181, guiTop + 119, 70, 20, I18n.format("freetrading.seal_a_deal"));
		this.abort = new GuiButton(1, guiLeft + 181, guiTop + 119 + 24, 70, 20, I18n.format("freetrading.abort"));
		this.buttonList.add(seal);
		this.buttonList.add(abort);
		this.moneyOfferInput = new GuiTextField(2, this.fontRenderer, guiLeft + 180, guiTop + 27, 70, 16);
		this.moneyOfferInput.setMaxStringLength(12);
		this.moneyOfferInput.setText(String.valueOf(0));

	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		this.mc.renderEngine.bindTexture(bg);
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		this.fontRenderer.drawString(moneyString, guiLeft + 7, guiTop + 64, 0x000000);
		this.fontRenderer.drawString(yourOffer, guiLeft + 180, guiTop + 7, 0x000000);
		this.fontRenderer.drawString(String.valueOf(yourOfferInt), guiLeft + 180, guiTop + 17, 0x000000);
		this.fontRenderer.drawString(partnerOffer, guiLeft + 180, guiTop + 47, 0x444444);
		this.fontRenderer.drawString(String.valueOf(partnerOfferInt), guiLeft + 180, guiTop + 57, 0x444444);
		this.moneyOfferInput.drawTextBox();
		for(GuiInventorySlotPlayer slot:playerInventorySlots) {
			slot.render(guiLeft, guiTop, mouseX, mouseY);
		}
		for(GuiInventorySlotCounter slot:partnerInventorySlots) {
			slot.render(guiLeft, guiTop, mouseX, mouseY);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		ClientNetworkHandler cnh = (ClientNetworkHandler) FreeTradingMod.network;
		switch (button.id) {
		case 0:
			cnh.runServerCommand(ServerCommands.ACCEPT_DEAL_AND_LOCK_INVENTORY);
			break;
		case 1:
			this.mc.displayGuiScreen(null);
			break;
		}
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		ClientNetworkHandler cnh = (ClientNetworkHandler) FreeTradingMod.network;
		cnh.runServerCommand(ServerCommands.ABORT_DEAL);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		this.moneyOfferInput.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		if (!playerCounter.onClick(guiLeft, guiTop, mouseX, mouseY)) {
			GuiInventorySlotPlayer playerSlot = getSlotFromPos(mouseX, mouseY);
			if (playerSlot == null || playerSlot.inCounter() || playerSlot.stack.isEmpty())
				return;
			playerSlot.moveToCounter();
			this.updateOffer();
		} else {
			this.updateOffer();
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.moneyOfferInput.textboxKeyTyped(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		} else {
			int newMoneyOffer = this.prevMoneyOffer;
			try {
				newMoneyOffer = Integer.parseInt(this.moneyOfferInput.getText());
			} catch (NumberFormatException e) {
			}
			if (newMoneyOffer != this.prevMoneyOffer) {
				this.prevMoneyOffer = newMoneyOffer;
				this.updateOffer();
			}
		}
	}
	
	private void updateOffer() {
		ClientNetworkHandler cnh = (ClientNetworkHandler) FreeTradingMod.network;
		cnh.sendPacketUpdateOffer(playerCounter.slotsInCounter, this.prevMoneyOffer);
	}

	@Override
	public void updateScreen() {
		this.moneyOfferInput.updateCursorCounter();
		super.updateScreen();
	}

	public void refreshPartnerItemStackOffer() {
		for(GuiInventorySlotCounter slot:this.partnerInventorySlots) {
			slot.refresh();
		}
	}
}
