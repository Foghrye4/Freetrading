package freetrading.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import freetrading.FreeTradingMod;
import freetrading.trading_system.TradingSystem;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityVillageMarket extends TileEntity {

	@Nonnull
	public ItemStack displayedItem = ItemStack.EMPTY;
	public int price = 0;

	public static final Set<EntityVillager> occupiedVillagers = new HashSet<EntityVillager>();
	public static final Set<MerchantRecipe> occupiedRecipes = new HashSet<MerchantRecipe>();
	public static final List<TileEntityVillageMarket> eventListeners = new ArrayList<TileEntityVillageMarket>();

	private EntityVillager merchant = null;
	private MerchantRecipe recipe = null;

	// Client-only
	private int iconId = 0;
	public boolean needRenderUpdate = true;
	public int displayList = -1;

	public ItemStack getDisplayedItem() {
		return displayedItem;
	}

	@Override
	public void validate() {
		if (!world.isRemote) {
			eventListeners.add(this);
			this.updateMarket();
		}
		super.validate();
	}

	@Override
	public void invalidate() {
		if (!world.isRemote) {
			eventListeners.remove(this);
			occupiedVillagers.remove(merchant);
			occupiedRecipes.remove(recipe);
		}
		super.invalidate();
	}

	private boolean recipeIsOutdated() {
		return (merchant != null && merchant.isDead) || (recipe != null && recipe.isRecipeDisabled());
	}

	public void updateMarket() {
		boolean sendUpdate = false;
		if (recipeIsOutdated()) {
			occupiedVillagers.remove(merchant);
			occupiedRecipes.remove(recipe);
			merchant = null;
			recipe = null;
			this.displayedItem = ItemStack.EMPTY;
			this.price = -1;
			sendUpdate = true;
		}

		if (merchant != null)
			return;
		EntityPlayer player = this.world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 256, false);

		AxisAlignedBB aabb = new AxisAlignedBB(this.pos.add(-64, -8, -64), this.pos.add(64, 8, 64));
		List<EntityVillager> villagers = this.world.getEntitiesWithinAABB(EntityVillager.class, aabb);
		for (EntityVillager villager : villagers) {
			if (occupiedVillagers.contains(villager))
				continue;
			MerchantRecipeList recipes = villager.getRecipes(player);
			for (int index = 0; index < recipes.size(); index++) {
				MerchantRecipe recipeIn = recipes.get(index);
				if (occupiedRecipes.contains(recipeIn) || recipeIn.isRecipeDisabled())
					continue;
				merchant = villager;
				recipe = recipeIn;
				this.displayedItem = recipe.getItemToSell().copy();
				this.price = TradingSystem.getHighPriceOf(this.displayedItem);
				occupiedRecipes.add(recipeIn);
				this.sendUpdatePacket();
				return;
			}
			occupiedVillagers.add(villager);
		}
		if (sendUpdate)
			this.sendUpdatePacket();
	}

	public void sendUpdatePacket() {
		for (Object player : world.playerEntities) {
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				playerMP.connection.sendPacket(this.getUpdatePacket());
			}
		}
	}

	public void tryTrade(EntityPlayerMP player) {
		boolean isRecipeIsOutdated = recipeIsOutdated();
		if (isRecipeIsOutdated) {
			updateAll();
			return;
		}
		boolean isTradeDone = this.trade(player);
		if (isTradeDone)
			updateAll();
	}

	public static void updateAll() {
		for (TileEntityVillageMarket market : eventListeners)
			market.updateMarket();
	}

	public boolean trade(EntityPlayerMP player) {
		if (recipe == null)
			return false;
		ItemStack stack = recipe.getItemToSell().copy();
		stack.onCrafting(player.world, player, stack.getCount());
		long playerMoney = TradingSystem.getMoneyOf(player);
		if (playerMoney < this.price)
			return false;
		playerMoney = TradingSystem.addMoneyTo(player, -price);
		int sellingPrice = TradingSystem.getLowPriceOf(stack);
		player.inventory.addItemStackToInventory(stack);
		player.addStat(StatList.TRADED_WITH_VILLAGER);
		if (sellingPrice > 0) {
			long money = TradingSystem.addMoneyTo(merchant, sellingPrice);
			merchant.careerLevel = (int) (money / 1000) + 1;
		}
		FreeTradingMod.network.sendPacketUpdatePlayerBalance(player, -price);
		return true;
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 255, this.getUpdateTag());
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.readItems(compound);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = super.writeToNBT(compound);
		this.writeDisplayItemToNBT(nbt);
		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		this.writeDisplayItemToNBT(nbt);
		return nbt;
	}

	private void readItems(NBTTagCompound compound) {
		displayedItem = new ItemStack(compound.getCompoundTag("displayedItem"));
		price = compound.getInteger("price");
		needRenderUpdate = true;
	}

	private void writeDisplayItemToNBT(NBTTagCompound nbt) {
		NBTTagCompound stackNBT1 = new NBTTagCompound();
		displayedItem.writeToNBT(stackNBT1);
		nbt.setTag("displayedItem", stackNBT1);
		nbt.setInteger("price", price);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		this.readItems(pkt.getNbtCompound());
	}

	@SideOnly(Side.CLIENT)
	public void setIconId(int iconIdIn) {
		iconId = iconIdIn;
	}

	@SideOnly(Side.CLIENT)
	public int getIconId() {
		return iconId;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
		BlockPos pos = getPos();
		return new AxisAlignedBB(pos, pos.add(1, 1, 1));
	}
}