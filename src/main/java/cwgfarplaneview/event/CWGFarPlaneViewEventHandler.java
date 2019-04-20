package cwgfarplaneview.event;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import cwgfarplaneview.ClientNetworkHandler;
import cwgfarplaneview.ServerNetworkHandler.ServerCommands;
import cwgfarplaneview.world.TerrainSurfaceBuilderWorker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CWGFarPlaneViewEventHandler {

	public static TerrainSurfaceBuilderWorker worker;

	public CWGFarPlaneViewEventHandler() {
	}

	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		World world = event.getWorld();
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		if (world.provider.getDimension() != 0)
			return;
		worker = new TerrainSurfaceBuilderWorker((WorldServer) world);
		Thread thread = new Thread(worker, "Terrain surface builder worker");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		worker.stop();
	}

	@SubscribeEvent
	public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayerMP && event.getWorld().provider.getDimension()==0) {
			worker.sendAllDataToPlayer((EntityPlayerMP) event.getEntity());
			network.sendSeaLevel((EntityPlayerMP) event.getEntity(),event.getWorld().getSeaLevel());
		}
	}
}