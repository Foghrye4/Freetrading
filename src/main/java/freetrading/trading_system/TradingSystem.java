package freetrading.trading_system;

import static freetrading.FreeTradingMod.network;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import freetrading.FreeTradingMod;
import freetrading.inventory.InventoryFreeTradingMerchant;
import freetrading.mod_interaction.fe_integration.ForgeEssentialsIntegrationUtil;
import freetrading.mod_interaction.ge_integration.GrandEconomyIntegrationUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
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
	private Long2ObjectMap<TradingSystemPriceEntry> pricesByItemIdAndMeta = new Long2ObjectOpenHashMap<TradingSystemPriceEntry>();
	private TradingSystemPriceEntry ZERO_PRICE;
	private final static int MAX_CAREER_ID = 6;
	public final Map<ResourceLocation,Map<String,Set<ISellable>>> goodsByMerchantAndCareer = new HashMap<ResourceLocation,Map<String,Set<ISellable>>>();

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() !=0)
			return;
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
		this.addPrice(new ItemStack(Blocks.LOG), 32, 1);
		this.addPrice(new ItemStack(Blocks.LOG2), 32, 1);
		
		for(VillagerProfession proffession:ForgeRegistries.VILLAGER_PROFESSIONS.getValues()) {
			next_career:for(int i=0;i<MAX_CAREER_ID;i++) {
				VillagerCareer career = proffession.getCareer(i);
				for(int i1=0;i1<7;i1++) {
					List<ITradeList> trades = career.getTrades(i1);
					if(trades==null)
						continue next_career;
					for(ITradeList trade:trades) {
						trade.addMerchantRecipe(this, recipeList, world.rand);
						this.registerTrade(proffession.getRegistryName(), career.getName(), new TradeWrapper(trade, i1));
					}
				}
			}
		}
		this.getPrices(null);
        File folder = new File(".", "config");
        folder.mkdirs();
        File configFile = new File(folder, "freetrading_custom_trades.json");
        try {
            if (configFile.exists())
            	this.readConfigFromJson(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NBTException e) {
			e.printStackTrace();
		}
		world = null;
	}
	
	private void registerTrade(ResourceLocation profession, String career, ISellable sellable) {
		Map<String, Set<ISellable>> customGoodsByCareer;
		if (goodsByMerchantAndCareer.containsKey(profession)) {
			customGoodsByCareer = goodsByMerchantAndCareer.get(profession);
		}
		else {
			customGoodsByCareer = new HashMap<String, Set<ISellable>>();
			goodsByMerchantAndCareer.put(profession, customGoodsByCareer);
		}
		Set<ISellable> sellables;
		if(customGoodsByCareer.containsKey(career)) {
			sellables = customGoodsByCareer.get(career);
		}
		else {
			sellables = new HashSet<ISellable>();
			customGoodsByCareer.put(career,sellables);
		}
		sellables.add(sellable);
	}
	
	private void getPrices(@Nullable ItemStack toFind) {
		for(MerchantRecipe recipe:recipeList) {
			if(this.getPrices(recipe, toFind)) {
				break;
			}
		}
	}
	
	private void readConfigFromJson(File configFile) throws IOException, NBTException {
		JsonReader reader = new JsonReader(new FileReader(configFile));
		reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject(); {
				int priceToSell = 144;
				int priceToBuy = 1;
				int level = 1;
				Item item = null;
				int data = 0;
				ResourceLocation profession = null;
				NBTTagCompound nbt = null;
				String career = null;
				while (reader.hasNext()) {
					String name = reader.nextName();
					if(name.equals("price_to_sell")) {
						priceToSell = reader.nextInt();
					}
					else if(name.equals("price_to_buy")) {
						priceToBuy = reader.nextInt();
					}
					else if(name.equals("item")) {
						item = Item.getByNameOrId(reader.nextString());
					}
					else if(name.equals("level")) {
						level = reader.nextInt();
					}
					else if(name.equals("data")) {
						data = reader.nextInt();
					}
					else if(name.equals("nbt")) {
						nbt = JsonToNBT.getTagFromJson(reader.nextString());
					}
					else if(name.equals("village_profession")) {
						profession = new ResourceLocation(reader.nextString());
					}
					else if(name.equals("career")) {
						career = reader.nextString();
					}
					else {
						reader.skipValue();
					}
				}
				ItemStack stack = new ItemStack(item,1,data);
				stack.setTagCompound(nbt);
				this.addPrice(stack, priceToBuy, priceToSell);
				if (profession == null)
					continue;
				if (career == null) {
					career = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(profession).getCareer(0).getName();
				}
				CustomTrade ctrade = new CustomTrade(stack, level, priceToBuy);
				registerTrade(profession,career, ctrade);
			}
			reader.endObject();
		}
		reader.close();
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
			int priceToBuy = getLowPriceOf(buy1) + getLowPriceOf(buy2);
			this.addPrice(sell, priceToBuy, priceToBuy / 24);
			return toFind!=null;
		} else if (b1known && !b2known && sknown) {
			int priceToSell = getHighPriceOf(sell) - getLowPriceOf(buy1);
			this.addPrice(buy2, priceToSell * 24, priceToSell);
			return toFind!=null;
		} else if (!b1known && b2known && sknown) {
			int priceToSell = getHighPriceOf(sell) - getLowPriceOf(buy2);
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
			pricesByItemIdAndMeta.putIfAbsent(encodeIdMetaPair(id, existing.getMetadata()), entry);
			TradingSystemPriceEntry newEntry = new TradingSystemPriceEntry(stack);
			newEntry.sellingPrice = priceToSell;
			newEntry.buyingPrice = priceToBuy;
			pricesByItemIdAndMeta.put(encodeIdMetaPair(id, stack.getMetadata()), newEntry);
		} else {
			TradingSystemPriceEntry newEntry = new TradingSystemPriceEntry(stack);
			newEntry.sellingPrice = priceToSell;
			newEntry.buyingPrice = priceToBuy;
			pricesByItemId.put(id, newEntry);
		}
	}
	
	private static long encodeIdMetaPair(int id, int meta) {
		return meta << 32 | id;
	}
	
	public static long getMoneyOf(Entity entity) {
		if(entity instanceof EntityPlayerMP && FreeTradingMod.isGrandEconomyLoaded) {
			return GrandEconomyIntegrationUtil.getMoneyOf((EntityPlayerMP)entity);
		}
		if(entity instanceof EntityPlayerMP && FreeTradingMod.isForgeEssentialsLoaded) {
			return ForgeEssentialsIntegrationUtil.getMoneyOf((EntityPlayerMP)entity);
		}
		return entity.getEntityData().getLong(MONEY);
	}
	
	public static long addMoneyTo(Entity entity, long profit) {
		if (entity instanceof EntityPlayerMP && FreeTradingMod.isGrandEconomyLoaded) {
			GrandEconomyIntegrationUtil.addMoneyTo((EntityPlayerMP) entity, profit);
			return GrandEconomyIntegrationUtil.getMoneyOf((EntityPlayerMP) entity);
		}
		if (entity instanceof EntityPlayerMP && FreeTradingMod.isForgeEssentialsLoaded) {
			ForgeEssentialsIntegrationUtil.addMoneyTo((EntityPlayerMP) entity, profit);
			return ForgeEssentialsIntegrationUtil.getMoneyOf((EntityPlayerMP) entity);
		}
		long current = entity.getEntityData().getLong(MONEY) + profit;
		entity.getEntityData().setLong(MONEY, current);
		return current;
	}
	
	public static int getLowPriceOf(ItemStack stack) {
		return getPriceEntry(stack).sellingPrice*stack.getCount();
	}
	
	public static int getHighPriceOf(ItemStack stack) {
		return getPriceEntry(stack).buyingPrice*stack.getCount();
	}
	
	private static TradingSystemPriceEntry getPriceEntry(ItemStack stack) {
		if(stack.isEmpty())
			return instance.ZERO_PRICE;
		int id = Item.REGISTRY.getIDForObject(stack.getItem());
		long metaToIDPair = encodeIdMetaPair(id, stack.getMetadata());
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
