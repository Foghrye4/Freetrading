package freetrading.mod_interaction.fe_integration;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.economy.Wallet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ForgeEssentialsIntegrationUtil {
	
	private static Wallet getWallet(EntityPlayerMP player) {
		return  APIRegistry.economy.getWallet(UserIdent.get(player));
	}

	public static long getMoneyOf(EntityPlayerMP player) {
		return getWallet(player).get();
	}

	public static void addMoneyTo(EntityPlayerMP player, long amount) {
		getWallet(player).add(amount);
	}
}
