package freetrading.trading_system;

import net.minecraft.entity.IMerchant;
import net.minecraft.item.ItemStack;

public interface ISellable {
	public TradeOffer getTradeOffer(IMerchant merchant);
}
