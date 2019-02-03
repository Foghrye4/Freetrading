package freetrading.trading_system;

import java.util.Random;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager.ITradeList;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipeList;

public class TradeWrapper implements ISellable {
	
	private final int level;
	private final ITradeList trade;
	private final MerchantRecipeList recipeList = new MerchantRecipeList();
	private final static Random random = new Random();

	public TradeWrapper(ITradeList tradeIn, int levelIn) {
		trade = tradeIn;
		level = levelIn;
	}

	@Override
	public TradeOffer getTradeOffer(IMerchant merchant) {
		recipeList.clear();
		trade.addMerchantRecipe(merchant, recipeList, random);
		if(!recipeList.isEmpty()) {
			ItemStack stack = recipeList.get(0).getItemToSell();
			return new TradeOffer(stack, level, TradingSystem.getHighPriceOf(stack));
		}
		return new TradeOffer(ItemStack.EMPTY, level,0);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof TradeWrapper) {
			TradeWrapper otw = (TradeWrapper)other;
			return otw.trade.equals(trade);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return trade.hashCode();
	}
}
