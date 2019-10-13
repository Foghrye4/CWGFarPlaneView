package cwgfarplaneview.command;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import cwgfarplaneview.CWGFarPlaneViewMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CWGFarPlaneViewCommand extends CommandBase {

	@Override
	public String getName() {
		return "cwgfarplaneview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/" + getName() + " [test]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new WrongUsageException(getUsage(sender), new Object[0]);
		} else {
			if (args[0].equals("flush")) {
				network.sendCommandFlush();
			} else if (args[0].equals("info")) {
				CWGFarPlaneViewMod.eventHandler.dumpProgressInfo();
			} else if (args[0].equals("test")) {
				network.testTerrainCubeRender((EntityPlayerMP) sender);
			} else {
				throw new WrongUsageException(getUsage(sender), new Object[0]);
			}
		}
	}
}
