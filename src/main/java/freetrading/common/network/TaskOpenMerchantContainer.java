package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TaskOpenMerchantContainer extends TaskVillagerInteractionBase {

	public TaskOpenMerchantContainer(WorldServer worldIn, int playerEntityIdIn, int villagerIdIn) {
		super(worldIn, playerEntityIdIn, villagerIdIn);
	}

	@Override
	public void run() {
		if(!this.getAndCheckEntities())
			return;
		player.closeContainer();
		player.openContainer = new InventoryFreeTradingMerchant(villager);
		FreeTradingMod.network.sendPacketUpdateTrading(player);
	}
}
