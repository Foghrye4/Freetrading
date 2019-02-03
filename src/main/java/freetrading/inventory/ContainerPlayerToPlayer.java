package freetrading.inventory;

import java.util.ArrayList;
import java.util.List;

import freetrading.FreeTradingMod;
import freetrading.trading_system.TradingSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerPlayerToPlayer extends Container {

	public final World world;
	private final EntityPlayerMP owner;
	private boolean dealIsLocked = false;
	private boolean dealIsFinished = false;
	public final int partnerEntityID;
	private long moneyOffer = 0;
	private final IntList itemOffer = new IntArrayList();

	public ContainerPlayerToPlayer(EntityPlayerMP ownerIn, int partnerEntityIDIn, World worldIn) {
		super();
		world = worldIn;
		owner = ownerIn;
		partnerEntityID = partnerEntityIDIn;
	}

	private EntityPlayerMP getPartner() {
		Entity entity = world.getEntityByID(partnerEntityID);
		if (!(entity instanceof EntityPlayerMP))
			return null;
		EntityPlayerMP player2 = (EntityPlayerMP) entity;
		return player2;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		Entity entity = world.getEntityByID(partnerEntityID);
		if (!(entity instanceof EntityPlayerMP)) {
			return false;
		}
		EntityPlayerMP player2 = (EntityPlayerMP) entity;
		if (!(player2.openContainer instanceof ContainerPlayerToPlayer)) {
			return false;
		}
		return !dealIsFinished;
	}

	public void setDealFinished() {
		dealIsFinished = true;
	}

	public void lockOrFinish(boolean lock) {
		if (dealIsFinished)
			return;
		dealIsLocked = lock;
		if (dealIsLocked) {
			EntityPlayerMP partner = getPartner();
			if (!(partner.openContainer instanceof ContainerPlayerToPlayer))
				return;
			ContainerPlayerToPlayer partnerCP2P = (ContainerPlayerToPlayer) partner.openContainer;
			if (partnerCP2P.dealIsLocked) {
				trade(partner);
				setDealFinished();
				owner.closeContainer();
				partnerCP2P.trade(owner);
				partnerCP2P.setDealFinished();
				partner.closeContainer();
			}
		}
	}

	public void trade(EntityPlayerMP partner) {
		for (int slotIndex : itemOffer) {
			if (slotIndex >= owner.inventory.getSizeInventory())
				continue;
			ItemStack stack = owner.inventory.removeStackFromSlot(slotIndex);
			if (stack.isEmpty())
				continue;
			if (!partner.addItemStackToInventory(stack)) {
				world.spawnEntity(new EntityItem(world, partner.posX, partner.posY, partner.posZ, stack));
			}
		}
		TradingSystem.addMoneyTo(partner, moneyOffer);
		TradingSystem.addMoneyTo(owner, -moneyOffer);
	}

	public void updateOffer(int newMoneyOffer, int[] slots) {
		if (dealIsLocked)
			return;
		long playerMoney = TradingSystem.getMoneyOf(owner);
		if (newMoneyOffer < 0) {
			moneyOffer = 0;
		} else if (newMoneyOffer > playerMoney) {
			moneyOffer = playerMoney;
		} else {
			moneyOffer = newMoneyOffer;
		}
		itemOffer.clear();
		itemOffer.addElements(0, slots);
		List<ItemStack> offeredStacks = new ArrayList<ItemStack>();
		for (int slotIndex : itemOffer) {
			if (slotIndex < owner.inventory.getSizeInventory()) {
				offeredStacks.add(owner.inventory.getStackInSlot(slotIndex));
			}
		}
		EntityPlayerMP partner = getPartner();
		if (partner == null)
			return;
		FreeTradingMod.network.sendPacketOfferUpdate(partner, moneyOffer, offeredStacks);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		dealIsLocked = false;
		setDealFinished();
		FreeTradingMod.network.sendCommandCloseGUI(owner);
	}
}
