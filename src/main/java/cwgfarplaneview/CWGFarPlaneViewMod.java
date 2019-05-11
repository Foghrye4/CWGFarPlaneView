package cwgfarplaneview;

import org.apache.logging.log4j.Logger;

import cwgfarplaneview.command.CWGFarPlaneViewCommand;
import cwgfarplaneview.event.CWGFarPlaneViewEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CWGFarPlaneViewMod.MODID, name = CWGFarPlaneViewMod.NAME, version = CWGFarPlaneViewMod.VERSION, dependencies = CWGFarPlaneViewMod.DEPENCIES, guiFactory = CWGFarPlaneViewMod.GUI_FACTORY)
public class CWGFarPlaneViewMod {
	public static final String MODID = "cwgfarplaneview";
	public static final String NAME = "CWG Far plane view";
	public static final String VERSION = "0.1.3";
	public static final String DEPENCIES = "required:cubicchunks@[0.0.951.0,);required:cubicgen@[0.0.54.0,);required:forge@[14.23.3.2658,)";
	public static final String GUI_FACTORY = "cwgfarplaneview.client.gui.GuiFactory";

	@SidedProxy(clientSide = "cwgfarplaneview.ClientProxy", serverSide = "cwgfarplaneview.ServerProxy")
	public static ServerProxy proxy;

	@SidedProxy(clientSide = "cwgfarplaneview.ClientNetworkHandler", serverSide = "cwgfarplaneview.ServerNetworkHandler")
	public static ServerNetworkHandler network;

	public static Logger logger;
	public static CWGFarPlaneViewEventHandler eventHandler;
	public static CWGFarPlaneViewConfig config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new CWGFarPlaneViewConfig(new Configuration(event.getSuggestedConfigurationFile()));
		proxy.preInit();
		network.load();
		logger = event.getModLog();
		eventHandler = new CWGFarPlaneViewEventHandler();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		MinecraftForge.EVENT_BUS.register(config);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		network.setServer(event.getServer());
		event.registerServerCommand(new CWGFarPlaneViewCommand());
	}
}
