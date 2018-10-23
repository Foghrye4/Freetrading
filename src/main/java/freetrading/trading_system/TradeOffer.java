package freetrading.trading_system;

import net.minecraft.item.ItemStack;

public class TradeOffer {
	
	public final int price;
	public final ItemStack stack;
	public final int level;
	
	public TradeOffer(ItemStack stackIn, int levelIn, int priceIn) {
		stack = stackIn;
		level = levelIn;
		price = priceIn;
	}
}
