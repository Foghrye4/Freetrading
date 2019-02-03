package freetrading.common.network;

import freetrading.inventory.InventoryFreeTradingMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TaskCloseVillagerGUI implements Runnable {

	protected final int playerEntityId;
	protected final WorldServer world;
	protected EntityPlayerMP player;

	public TaskCloseVillagerGUI(WorldServer worldIn, int playerEntityIdIn) {
		playerEntityId = playerEntityIdIn;
		world = worldIn;
	}
	
	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if( !(entity instanceof EntityPlayerMP))
			return false;
		player = (EntityPlayerMP) world.getEntityByID(playerEntityId);
		if(!(player.openContainer instanceof InventoryFreeTradingMerchant))
			return false;
		return true;
	}

	@Override
	public void run() {
		if(!getAndCheckEntities())
			return;
		InventoryFreeTradingMerchant inv = (InventoryFreeTradingMerchant) player.openContainer;
		inv.merchant.setCustomer(null);
		player.closeContainer();
	}
}
