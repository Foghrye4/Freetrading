package freetrading.client.gui;

import java.util.ArrayList;
import java.util.List;

import freetrading.client.gui.slot.GuiInventorySlotPlayer;

public class GuiCounter {
	private static final int SLOT_SIZE = 18;
	public final List<GuiInventorySlotPlayer> slotsInCounter = new ArrayList<GuiInventorySlotPlayer>();
	public final int xPos;
	public final int yPos;
	private final int rows;
	private final int columns;

	public GuiCounter(int xIn, int yIn, int rowsIn, int columnsIn) {
		xPos = xIn;
		yPos = yIn;
		rows = rowsIn;
		columns = columnsIn;
	}

	public boolean onClick(int guiLeft, int guiTop, int mouseX, int mouseY) {
		int x = mouseX - xPos - guiLeft;
		int y = mouseY - yPos - guiTop;
		int slotX = x / SLOT_SIZE;
		int slotY = y / SLOT_SIZE;
		if (slotX >= columns)
			return false;
		if (slotY >= rows)
			return false;
		int index = slotX + slotY * columns;
		if (index < slotsInCounter.size())
			slotsInCounter.remove(index);
		return true;
	}

	public int getXOf(GuiInventorySlotPlayer slot) {
		int index = slotsInCounter.indexOf(slot);
		return (index % columns) * SLOT_SIZE + xPos;
	}
	
	public int getYOf(GuiInventorySlotPlayer slot) {
		int index = slotsInCounter.indexOf(slot);
		return (index / columns) * SLOT_SIZE + yPos;
	}
}
