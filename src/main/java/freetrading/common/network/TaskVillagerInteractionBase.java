package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public abstract class TaskVillagerInteractionBase implements Runnable {

	protected final int villagerId;
	protected final int playerEntityId;
	protected final WorldServer world;
	protected EntityPlayerMP player;
	protected EntityVillager villager;

	public TaskVillagerInteractionBase(WorldServer worldIn, int playerEntityIdIn, int villagerIdIn) {
		villagerId = villagerIdIn;
		playerEntityId = playerEntityIdIn;
		world = worldIn;
	}
	
	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if( !(entity instanceof EntityPlayerMP))
			return false;
		player = (EntityPlayerMP) world.getEntityByID(playerEntityId);
		entity = world.getEntityByID(villagerId);
		if( !(entity instanceof EntityVillager))
			return false;
		villager = (EntityVillager) world.getEntityByID(villagerId);
		return true;
	}
}
