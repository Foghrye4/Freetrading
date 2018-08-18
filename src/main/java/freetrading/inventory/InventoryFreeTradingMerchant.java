package freetrading.inventory;

import java.util.List;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;

public class InventoryFreeTradingMerchant extends Container implements IInventory {

	public final EntityVillager merchant;
	private final MerchantRecipeList recipeList = new MerchantRecipeList();
	public final int[] itemTierLevel = new int[9*3];

	public InventoryFreeTradingMerchant(EntityVillager theMerchantIn) {
		merchant = theMerchantIn;
		this.onCraftMatrixChanged(this);
	}

	@Override
	public String getName() {
		VillagerCareer career = merchant.getProfessionForge().getCareer(merchant.careerId);
		return career.getName();
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		recipeList.clear();
		this.fillItemTier(0, 0);
		VillagerCareer career = merchant.getProfessionForge().getCareer(merchant.careerId);
		for(int level=0;level<6; level++) {
			List<ITradeList> trades = career.getTrades(level);
			if(trades==null)
				continue;
			if(level>merchant.careerLevel)
				this.fillItemTier(recipeList.size(), level);
			for(ITradeList trade:trades) {
				trade.addMerchantRecipe(merchant, recipeList, merchant.getRNG());
			}
		}
		for(int i=0;i<recipeList.size();i++) {
			this.inventoryItemStacks.add(i, recipeList.get(i).getItemToSell());
		}
	}
	
	private void fillItemTier(int from, int tier) {
		for(int i=from;i<itemTierLevel.length;i++) {
			itemTierLevel[i]=tier;
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public int getSizeInventory() {
		return recipeList.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventoryItemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if(index>=this.inventoryItemStacks.size())
			return ItemStack.EMPTY;
		return this.inventoryItemStacks.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.inventoryItemStacks, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack stack = ItemStackHelper.getAndRemove(this.inventoryItemStacks, index);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.inventoryItemStacks.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.merchant.getCustomer() == player;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		inventoryItemStacks.clear();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getDistanceSq(merchant)<16d;
	}
}
