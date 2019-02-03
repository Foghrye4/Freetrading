package freetrading.client.gui.slot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class GuiInventorySlotCounter extends GuiInventorySlot {

	private List<ItemStack> inventory;

	public GuiInventorySlotCounter(int x1, int y1, int slotIndexIn, List<ItemStack> inventoryIn) {
		super(x1, y1, slotIndexIn);
		inventory = inventoryIn;
		refresh();
	}

	public void refresh() {
		if (this.slotIndex < inventory.size()) {
			stack = inventory.get(slotIndex);
		} else {
			stack = ItemStack.EMPTY;
		}
	}
}
