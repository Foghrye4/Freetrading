package freetrading.mod_interaction.ge_integration;

import net.minecraft.entity.player.EntityPlayerMP;
import the_fireplace.grandeconomy.economy.Account;

public class GrandEconomyIntegrationUtil {

	public static long getMoneyOf(EntityPlayerMP player) {
        Account account = Account.get(player);
        account.update();
		return account.getBalance();
	}

	public static void addMoneyTo(EntityPlayerMP player, long amount) {
        Account account = Account.get(player);
        account.update();
        account.addBalance(amount, false);
	}
}
