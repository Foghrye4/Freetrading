package freetrading.entity.player;

import freetrading.common.network.TaskOpenPlayerToPlayerContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerInteractionEventHandler {

	@SubscribeEvent
	public void onPlayerInteractEvent(PlayerInteractEvent.EntityInteract event) {
		if(event.getTarget() instanceof EntityPlayerMP) {
			event.setCanceled(true);
			((WorldServer) event.getWorld()).addScheduledTask(new TaskOpenPlayerToPlayerContainer((WorldServer) event.getWorld(), event.getEntityPlayer().getEntityId(), event.getTarget().getEntityId()));
		}
	}
}
