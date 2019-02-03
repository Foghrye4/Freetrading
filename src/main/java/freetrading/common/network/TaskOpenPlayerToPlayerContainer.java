package freetrading.common.network;

import freetrading.FreeTradingMod;
import freetrading.inventory.ContainerPlayerToPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class TaskOpenPlayerToPlayerContainer implements Runnable {

	protected final int otherPlayerId;
	protected final int playerEntityId;
	protected final WorldServer world;
	protected EntityPlayerMP player;
	protected EntityPlayerMP otherPlayer;

	public TaskOpenPlayerToPlayerContainer(WorldServer worldIn, int playerEntityIdIn, int otherPlayerIdIn) {
		otherPlayerId = otherPlayerIdIn;
		playerEntityId = playerEntityIdIn;
		world = worldIn;
	}
	
	protected boolean getAndCheckEntities() {
		Entity entity = world.getEntityByID(playerEntityId);
		if( !(entity instanceof EntityPlayerMP))
			return false;
		player = (EntityPlayerMP) world.getEntityByID(playerEntityId);
		entity = world.getEntityByID(otherPlayerId);
		if( !(entity instanceof EntityPlayerMP))
			return false;
		otherPlayer = (EntityPlayerMP) world.getEntityByID(otherPlayerId);
		return true;
	}
	
	@Override
	public void run() {
		if(!this.getAndCheckEntities())
			return;
		player.openContainer = new ContainerPlayerToPlayer(player, otherPlayerId, world);
		otherPlayer.openContainer = new ContainerPlayerToPlayer(otherPlayer, playerEntityId, world);
		FreeTradingMod.network.sendPacketOpenPlayerToPlayerGUI(player, otherPlayerId);
		FreeTradingMod.network.sendPacketOpenPlayerToPlayerGUI(otherPlayer, playerEntityId);
	}
}
