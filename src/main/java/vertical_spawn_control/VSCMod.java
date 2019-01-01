package vertical_spawn_control;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import vertical_spawn_control.event.VSCEventHandler;

@Mod(modid = VSCMod.MODID, name = VSCMod.NAME, version = VSCMod.VERSION)
public class VSCMod {
	public static final String MODID = "vertical_spawn_control";
	public static final String NAME = "Vertical spawn control";
	public static final String VERSION = "1.7.4";

	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(new VSCEventHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@NetworkCheckHandler
	public boolean checkModLists(Map<String, String> modList, Side sideIn) {
		return true;
	}
}
