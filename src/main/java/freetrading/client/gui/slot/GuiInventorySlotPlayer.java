package freetrading.client.gui.slot;

import freetrading.client.gui.GuiCounter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class GuiInventorySlotPlayer extends GuiInventorySlot {

	private final InventoryPlayer inventory;
	private final GuiCounter playerCounter;

	public GuiInventorySlotPlayer(int x1, int y1, int slotIndexIn, InventoryPlayer inventoryIn, GuiCounter playerCounterIn) {
		super(x1, y1, slotIndexIn);
		inventory = inventoryIn;
		playerCounter = playerCounterIn;
		stack = inventory.getStackInSlot(slotIndexIn);
	}

	public boolean inCounter() {
		return playerCounter.slotsInCounter.contains(this);
	}

	public void moveToCounter() {
		playerCounter.slotsInCounter.add(this);
	}
	
	@Override
	protected int getX() {
		if(!inCounter())
			return super.getX();
		return playerCounter.getXOf(this);
	}
	
	@Override
	protected int getY() {
		if(!inCounter())
			return super.getY();
		return playerCounter.getYOf(this);
	}
}
