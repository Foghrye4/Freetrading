package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.ContainerPlayerToPlayer;
import freetrading.trading_system.TradingSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TaskUpdateOffer implements Runnable {

	protected final int playerEntityId;
	protected final WorldServer world;
	protected final long newMoneyOffer;
	protected EntityPlayerMP player1;
	private final int[] slots;

	public TaskUpdateOffer(WorldServer worldIn, int playerEntityIdIn, long newMoneyOffer2, int[] slotsIn) {
		playerEntityId = playerEntityIdIn;
		world = worldIn;
		newMoneyOffer = newMoneyOffer2;
		slots = slotsIn;
	}

	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if (!(entity instanceof EntityPlayerMP))
			return false;
		player1 = (EntityPlayerMP) entity;
		if(!(player1.openContainer instanceof ContainerPlayerToPlayer))
			return false;
		return true;
	}

	@Override
	public void run() {
		if (!this.getAndCheckEntities())
			return;
		ContainerPlayerToPlayer cp2p = (ContainerPlayerToPlayer) player1.openContainer;
		cp2p.updateOffer(newMoneyOffer, slots);
	}
}
