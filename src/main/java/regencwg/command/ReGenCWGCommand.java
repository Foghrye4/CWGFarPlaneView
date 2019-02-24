package regencwg.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import regencwg.world.WorldSavedDataReGenCWG;

public class ReGenCWGCommand extends CommandBase {

	@Override
	public String getName() {
		return "regencwg";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return getName() + " [remaining|reset|stop] <dimension_id>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		} else {
			int dimension = 0;
			try {
				dimension = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e) {
				throw new WrongUsageException(args[1] + " is not a valid dimension id.");
			}
			WorldServer world = server.getWorld(dimension);
			WorldSavedDataReGenCWG data = WorldSavedDataReGenCWG.getOrCreateWorldSavedData(world);
			if(args[0].equals("remaining")) {
				sender.sendMessage(new TextComponentTranslation("regencwg.remaining",data.getRemaining()));
			}
			else if(args[0].equals("reset")) {
				data.initialize(world);
				sender.sendMessage(new TextComponentTranslation("regencwg.reset",data.getRemaining()));
			} 
			else if(args[0].equals("stop")) {
				sender.sendMessage(new TextComponentTranslation("regencwg.stop"));
			} 
			else {
				throw new WrongUsageException(getUsage(sender), new Object[0]);
			}

		}
	}
}
