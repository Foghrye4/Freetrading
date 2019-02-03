package freetrading.client.network;

import freetrading.client.gui.GuiPlayerToPlayer;
import net.minecraft.client.Minecraft;

public class TaskShowPlayerToPlayerGui implements Runnable {

	protected final int otherPlayerId;

	public TaskShowPlayerToPlayerGui(int otherPlayerIdIn) {
		otherPlayerId = otherPlayerIdIn;
	}

	@Override
	public void run() {
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.currentScreen instanceof GuiPlayerToPlayer))
			mc.displayGuiScreen(new GuiPlayerToPlayer(mc.player));
	}
}
