package freetrading.common.network;

import freetrading.inventory.ContainerPlayerToPlayer;
import freetrading.trading_system.TradingSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

public class TaskAcceptDealAndLockInventory implements Runnable {

	protected final int playerEntityId;
	protected final WorldServer world;
	protected EntityPlayerMP player1;

	public TaskAcceptDealAndLockInventory(WorldServer worldIn, int playerEntityIdIn) {
		playerEntityId = playerEntityIdIn;
		world = worldIn;
	}

	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if (!(entity instanceof EntityPlayerMP))
			return false;
		player1 = (EntityPlayerMP) entity;
		if (!(player1.openContainer instanceof ContainerPlayerToPlayer))
			return false;
		return true;
	}

	@Override
	public void run() {
		if (!this.getAndCheckEntities())
			return;
		ContainerPlayerToPlayer container = (ContainerPlayerToPlayer) player1.openContainer;
		container.lockOrFinish(true);
	}
}
