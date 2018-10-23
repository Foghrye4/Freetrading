package freetrading.trading_system;

import net.minecraft.entity.IMerchant;
import net.minecraft.item.ItemStack;

public class CustomTrade extends TradeOffer implements ISellable {

	public CustomTrade(ItemStack stackIn, int levelIn, int priceIn) {
		super(stackIn, levelIn, priceIn);
	}

	@Override
	public TradeOffer getTradeOffer(IMerchant merchant) {
		return this;
	}
	
	@Override
	public String toString() {
		return stack.toString()+"$" + price + "^" + level;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof CustomTrade) {
			CustomTrade otw = (CustomTrade)other;
			return stack.equals(otw.stack);
		}
		return false;
	}
}
