package freetrading.entity.player;

import freetrading.tileentity.TileEntityVillageMarket;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityEventHandler {

	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent event) {
		if(event.getEntity() instanceof EntityVillager) {
			TileEntityVillageMarket.updateAll();
		}
	}
}
