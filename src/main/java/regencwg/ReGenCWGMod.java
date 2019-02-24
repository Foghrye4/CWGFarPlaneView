package regencwg;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import regencwg.command.ReGenCWGCommand;
import regencwg.event.ReGenCWGEventHandler;

@Mod(modid = ReGenCWGMod.MODID, name = ReGenCWGMod.NAME, version = ReGenCWGMod.VERSION, dependencies = ReGenCWGMod.DEPENCIES)
public class ReGenCWGMod {
	public static final String MODID = "regencwg";
	public static final String NAME = "ReGen CWG";
	public static final String VERSION = "0.0.1";
	public static final String DEPENCIES = "required:cubicchunks@[0.0.928.0,);required:cubicgen@[0.0.39.0,);required:forge@[14.23.3.2658,)";

	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(new ReGenCWGEventHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side sideIn) {
		return true;
	}
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new ReGenCWGCommand());
	}
}
