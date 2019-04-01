package freetrading.entity.player;

import freetrading.common.network.TaskOpenPlayerToPlayerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class PlayerInteractionEventHandler {

	@SubscribeEvent
	public void onPlayerInteractEvent(PlayerInteractEvent.EntityInteract event) {
		if(event.getTarget() instanceof EntityPlayerMP && event.getEntityPlayer().getHeldItemMainhand().isEmpty()) {
			event.setCanceled(true);
			((WorldServer) event.getWorld()).addScheduledTask(new TaskOpenPlayerToPlayerContainer((WorldServer) event.getWorld(), event.getEntityPlayer().getEntityId(), event.getTarget().getEntityId()));
		}
	}
}
