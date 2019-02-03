package freetrading.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TaskAbortDeal implements Runnable {

	protected final int playerEntityId;
	protected final WorldServer world;
	protected EntityPlayerMP player1;

	public TaskAbortDeal(WorldServer worldIn, int playerEntityIdIn) {
		playerEntityId = playerEntityIdIn;
		world = worldIn;
	}

	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if (!(entity instanceof EntityPlayerMP))
			return false;
		player1 = (EntityPlayerMP) entity;
		return true;
	}

	@Override
	public void run() {
		if (!this.getAndCheckEntities())
			return;
		player1.closeContainer();
	}
}
