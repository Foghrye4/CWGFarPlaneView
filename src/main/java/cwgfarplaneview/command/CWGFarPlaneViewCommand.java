package cwgfarplaneview.command;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.event.CWGFarPlaneViewEventHandler;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.biome.Biome;

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
