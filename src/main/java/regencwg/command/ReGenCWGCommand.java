package regencwg.command;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import regencwg.ReGenCWGMod;
import regencwg.world.WorldSavedDataReGenCWG;

public class ReGenCWGCommand extends CommandBase {

	@Override
	public String getName() {
		return "regencwg";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/"+getName() + " [remaining|reset|stop|finish] <dimension_id>";
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
				sender.sendMessage(new TextComponentString("Remaining cubes: "+data.getRemaining()));
			}
			else if(args[0].equals("reset")) {
				data.initialize(world);
				sender.sendMessage(new TextComponentString("All saved on disk cubes will be repopulated with ores. Remaining cubes: "+data.getRemaining()));
			} 
			else if(args[0].equals("stop")) {
				data.stop();
				sender.sendMessage(new TextComponentString("Repopulation stopped. ReGenCWG will not alter existing cubes."));
			} 
			else if(args[0].equals("finish")) {
				if (checkForWatchdog()) {
					sender.sendMessage(new TextComponentString(
							"Watchdog is active. If population process will be interrupted by watchdog, cube data may be corrupted. Restart server with watchdog disabled before launching this command."));
					return;
				}
				sender.sendMessage(new TextComponentString(
						"Starting population process. This will take time."));
				for(CubePos pos:data.remainingCP) {
					ReGenCWGMod.eventHandler.populate(pos, ((ICubicWorld)world).getCubeFromCubeCoords(pos), world);
				}
				data.stop();
				sender.sendMessage(new TextComponentString(
						"ReGenCWG: Job is done."));
			}
			else if(args[0].equals("replace")) {
				if (checkForWatchdog()) {
					sender.sendMessage(new TextComponentString(
							"Watchdog is active. If replacing process will be interrupted by watchdog, cube data may be corrupted. Restart server with watchdog disabled before launching this command."));
					return;
				}
				sender.sendMessage(new TextComponentString(
						"Starting block replacing process. This will take time."));
				int replaced = ReGenCWGMod.eventHandler.runReplacer(world);
				sender.sendMessage(
						new TextComponentString("ReGenCWG: Replacing job is done. " + replaced + " cubes was altered."));
			}
			else if(args[0].equals("replace-config")) {
				if (args.length != 3)
					throw new WrongUsageException(getUsage(sender), new Object[0]);
				ReGenCWGMod.eventHandler.addReplacerConfig(dimension,args[2]);
			}
			else {
				throw new WrongUsageException(getUsage(sender), new Object[0]);
			}

		}
	}
	
	private boolean checkForWatchdog() {
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getName().equals("Server Watchdog")) {
				return true;
			}
		}
		return false;
	}
}
