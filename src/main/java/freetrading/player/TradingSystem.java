package freetrading.player;

import static freetrading.FreeTradingMod.network;

import java.util.List;

import javax.annotation.Nullable;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class TradingSystem implements IMerchant {
	
	private World world;
	public static TradingSystem instance = new TradingSystem();
	private final static int EMERALD_PRICE = 144;
	public final static String MONEY = "money";
	private MerchantRecipeList recipeList = new MerchantRecipeList();
	private Int2ObjectMap<TradingSystemPriceEntry> pricesByItemId = new Int2ObjectOpenHashMap<TradingSystemPriceEntry>();
	private Int2ObjectMap<TradingSystemPriceEntry> pricesByItemIdAndMeta = new Int2ObjectOpenHashMap<TradingSystemPriceEntry>();
	private TradingSystemPriceEntry ZERO_PRICE;

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		this.init(event.getWorld());
	}
	
	@SuppressWarnings("deprecation")
	public void init(World worldIn) {
		pricesByItemId.clear();
		pricesByItemIdAndMeta.clear();
		world = worldIn;
		instance = this;
		ZERO_PRICE = new TradingSystemPriceEntry(ItemStack.EMPTY);
		ZERO_PRICE.buyingPrice=0;
		ZERO_PRICE.sellingPrice=0;
		this.addPrice(ItemStack.EMPTY, 0, 0);
		this.addPrice(new ItemStack(Items.EMERALD), EMERALD_PRICE, EMERALD_PRICE);
		this.addPrice(new ItemStack(Items.FISH), EMERALD_PRICE, EMERALD_PRICE/24);
		this.addPrice(new ItemStack(Blocks.GRAVEL), 16, 1);
		this.addPrice(new ItemStack(Items.FILLED_MAP), EMERALD_PRICE*24, EMERALD_PRICE);
		
		for(VillagerProfession proffession:ForgeRegistries.VILLAGER_PROFESSIONS.getValues()) {
			next_career:for(int i=0;i<6;i++) {
				VillagerCareer career = proffession.getCareer(i);
				for(int i1=0;i1<7;i1++) {
					List<ITradeList> trades = career.getTrades(i1);
					if(trades==null)
						continue next_career;
					for(ITradeList trade:trades) {
						trade.addMerchantRecipe(this, recipeList, world.rand);
					}
				}
			}
		}
		this.getPrices(null);
		world = null;
	}
	
	private void getPrices(@Nullable ItemStack toFind) {
		for(MerchantRecipe recipe:recipeList) {
			if(this.getPrices(recipe, toFind)) {
				break;
			}
		}
	}
	
	private boolean getPrices(MerchantRecipe recipe, @Nullable ItemStack toFind) {
		ItemStack buy1 = recipe.getItemToBuy();
		ItemStack buy2 = recipe.getSecondItemToBuy();
		ItemStack sell = recipe.getItemToSell();
		if (toFind != null &&
				toFind.getItem()!=buy1.getItem() && 
				toFind.getItem()!=buy2.getItem() && 
				toFind.getItem()!=sell.getItem()) {
			return false;
		}
		boolean b1known = isKnown(buy1);
		boolean b2known = isKnown(buy2);
		boolean sknown = isKnown(sell);
		if (b1known && b2known && sknown) {
			return false;
		} else if (b1known && b2known && !sknown) {
			int priceToBuy = getSellingPriceOf(buy1) + getSellingPriceOf(buy2);
			this.addPrice(sell, priceToBuy, priceToBuy / 24);
			return toFind!=null;
		} else if (b1known && !b2known && sknown) {
			int priceToSell = getBuyingPriceOf(sell) - getSellingPriceOf(buy1);
			this.addPrice(buy2, priceToSell * 24, priceToSell);
			return toFind!=null;
		} else if (!b1known && b2known && sknown) {
			int priceToSell = getBuyingPriceOf(sell) - getSellingPriceOf(buy2);
			this.addPrice(buy1, priceToSell * 24, priceToSell);
			return toFind!=null;
		} else {
			if(toFind!=null)
				return false;
		}
		if (!b1known) {
			this.getPrices(buy1);
			if (!isKnown(buy1)) {
				this.addPrice(buy1, EMERALD_PRICE*24, EMERALD_PRICE);
				FreeTradingMod.logger.error("Error defining price for item " + buy1);
			}
		}
		if (!b2known) {
			this.getPrices(buy2);
			if (!isKnown(buy2)) {
				this.addPrice(buy2, EMERALD_PRICE*24, EMERALD_PRICE);
				FreeTradingMod.logger.error("Error defining price for item " + buy2);
			}
		}
		if (!sknown) {
			this.getPrices(sell);
			if (!isKnown(sell)) {
				this.addPrice(sell, EMERALD_PRICE*24, EMERALD_PRICE);
				FreeTradingMod.logger.error("Error defining price for item " + sell);
			}
		}
		return false;
	}

	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event) {
		if(event.getTarget() instanceof EntityVillager) {
			event.setCanceled(true);
			if(event.getEntity().world.isRemote)
				return;
			event.getEntityPlayer().openContainer = new InventoryFreeTradingMerchant((EntityVillager) event.getTarget());
			network.sendPacketUpdateTrading((EntityPlayerMP) event.getEntityPlayer());
		}
	}
	
	private boolean isKnown(ItemStack stack) {
		if(stack.isEmpty())
			return true;
		int id = Item.REGISTRY.getIDForObject(stack.getItem());
		return pricesByItemId.containsKey(id);
	}

	private void addPrice(ItemStack stack, int priceToBuy, int priceToSell) {
		if (stack.getCount() != 0) {
			priceToBuy /= stack.getCount();
			priceToSell /= stack.getCount();
		}
		int id = Item.REGISTRY.getIDForObject(stack.getItem());
		if (pricesByItemId.containsKey(id)) {
			TradingSystemPriceEntry entry = pricesByItemId.get(id);
			ItemStack existing = entry.itemStack;
			if (existing.getMetadata() != stack.getMetadata()) {
				pricesByItemIdAndMeta.putIfAbsent(encodeIdMetaPair(id, existing.getMetadata()), entry);
				TradingSystemPriceEntry newEntry = new TradingSystemPriceEntry(stack);
				newEntry.sellingPrice = priceToSell;
				newEntry.buyingPrice = priceToBuy;
				pricesByItemIdAndMeta.put(encodeIdMetaPair(id, stack.getMetadata()), newEntry);
			}
		} else {
			TradingSystemPriceEntry newEntry = new TradingSystemPriceEntry(stack);
			newEntry.sellingPrice = priceToSell;
			newEntry.buyingPrice = priceToBuy;
			pricesByItemId.put(id, newEntry);
		}
	}
	
	private static int encodeIdMetaPair(int id, int meta) {
		return meta << 16 | id;
	}
	
	public static long getMoneyOf(Entity entity) {
		return entity.getEntityData().getLong(MONEY);
	}
	
	public static long addMoneyTo(Entity entity, int money) {
		long current = entity.getEntityData().getLong(MONEY) + money;
		entity.getEntityData().setLong(MONEY, current);
		return current;
	}
	
	public static int getSellingPriceOf(ItemStack stack) {
		return getPriceEntry(stack).sellingPrice*stack.getCount();
	}
	
	public static int getBuyingPriceOf(ItemStack stack) {
		return getPriceEntry(stack).buyingPrice*stack.getCount();
	}
	
	private static TradingSystemPriceEntry getPriceEntry(ItemStack stack) {
		if(stack.isEmpty())
			return instance.ZERO_PRICE;
		int id = Item.REGISTRY.getIDForObject(stack.getItem());
		int metaToIDPair = encodeIdMetaPair(id, stack.getMetadata());
		if(instance.pricesByItemIdAndMeta.containsKey(metaToIDPair)) {
			return instance.pricesByItemIdAndMeta.get(metaToIDPair);
		}
		else if(instance.pricesByItemId.containsKey(id)) {
			return instance.pricesByItemId.get(id);
		}
		return instance.ZERO_PRICE;
	}

	@Override
	public void setCustomer(EntityPlayer player) {
	}

	@Override
	public EntityPlayer getCustomer() {
		return null;
	}

	@Override
	public MerchantRecipeList getRecipes(EntityPlayer player) {
		return recipeList;
	}

	@Override
	public void setRecipes(MerchantRecipeList recipeList) {
	}

	@Override
	public void useRecipe(MerchantRecipe recipe) {
	}

	@Override
	public void verifySellingItem(ItemStack stack) {
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString("");
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public BlockPos getPos() {
		return BlockPos.ORIGIN;
	}
}
