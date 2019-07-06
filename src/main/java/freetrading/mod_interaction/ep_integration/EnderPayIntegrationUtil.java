package freetrading.mod_interaction.ep_integration;
import com.kamildanak.minecraft.enderpay.economy.Account;

import net.minecraft.entity.player.EntityPlayerMP;;

public class EnderPayIntegrationUtil {
	public static long getMoneyOf(EntityPlayerMP player) {
        Account account = Account.get(player);
        account.update();
		return account.getBalance();
	}

	public static void addMoneyTo(EntityPlayerMP player, long amount) {
        Account account = Account.get(player);
        account.update();
        account.addBalance(amount);
	}
	
}
