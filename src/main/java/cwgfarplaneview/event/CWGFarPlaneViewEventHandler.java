package cwgfarplaneview.event;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import cwgfarplaneview.world.TerrainSurfaceBuilderWorker;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;

public class CWGFarPlaneViewEventHandler {

	@SubscribeEvent
	public void onWorldLoadEvent(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		if(!(event.getEntity() instanceof EntityPlayerMP))
			return;
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		if (world.provider.getDimension() != 0 || !((ICubicWorld) world).isCubicWorld())
			return;
		// Vanilla generator have no methods of retrieving ChunkPrimer directly. Using it to get chunks instead cause data corruption.
		if (world.getWorldType().getName().equals("VanillaCubic"))
			return;
		WorldSavedDataTerrainSurface data = WorldSavedDataTerrainSurface.getOrCreateWorldSavedData(world);
		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
		network.sendSeaLevel(player, event.getWorld().getSeaLevel());
		TerrainSurfaceBuilderWorker worker = new TerrainSurfaceBuilderWorker(player, (WorldServer) world, data);
		Thread thread = new Thread(worker, player.getName() + "'s terrain surface builder worker");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY + 1);
		thread.start();
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (world.provider.getDimension() != 0 || !((ICubicWorld) world).isCubicWorld())
			return;
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		network.sendCommandFlush();
	}
}
