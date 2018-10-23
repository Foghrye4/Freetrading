package freetrading.trading_system;

import net.minecraft.item.ItemStack;

public class TradingSystemPriceEntry {
	
	public final ItemStack itemStack;
	public int sellingPrice = 1;
	public int buyingPrice = 144;
	
	public TradingSystemPriceEntry(ItemStack itemStackIn) {
		itemStack = itemStackIn.copy();
		itemStack.setCount(1);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + '@' + "[Price entry for: " + itemStack.toString() + ", selling price:"
				+ sellingPrice + ", buying price:" + buyingPrice + "]";
	}
}
