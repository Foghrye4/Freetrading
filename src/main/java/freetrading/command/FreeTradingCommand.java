package freetrading.command;

import freetrading.common.network.TaskOpenPlayerToPlayerContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** For debug purposes */
public class FreeTradingCommand extends CommandBase {

	@Override
	public String getName() {
		return "freetrading";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return getName() + "debugp2p";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1)
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		if (args[0].equalsIgnoreCase("debugp2p")) {
			Entity commandSender = sender.getCommandSenderEntity();
			if (commandSender instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) commandSender;
				(player.getServerWorld()).addScheduledTask(new TaskOpenPlayerToPlayerContainer(player.getServerWorld(),
						player.getEntityId(), player.getEntityId()));
			}
		}
	}
}
