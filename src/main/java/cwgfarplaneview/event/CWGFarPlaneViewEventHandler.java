package cwgfarplaneview.event;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import java.util.HashSet;
import java.util.Set;

import cwgfarplaneview.world.terrain.flat.TerrainSurfaceBuilderWorker;
import cwgfarplaneview.world.terrain.volumetric.TerrainVolumeBuilderWorker;
import io.github.opencubicchunks.cubicchunks.api.world.CubeUnWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CWGFarPlaneViewEventHandler {

	public static Set<TerrainSurfaceBuilderWorker> workers = new HashSet<TerrainSurfaceBuilderWorker>();
	public static Set<TerrainVolumeBuilderWorker> volumeWorkers = new HashSet<TerrainVolumeBuilderWorker>();

	@SubscribeEvent
	public void onEntityJoinEvent(EntityJoinWorldEvent event) {
		World world = event.getWorld();
		if (!(event.getEntity() instanceof EntityPlayerMP))
			return;
		if (!isRenderableWorld(world))
			return;
		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
		network.sendSeaLevel(player, event.getWorld().getSeaLevel());
		TerrainSurfaceBuilderWorker worker = new TerrainSurfaceBuilderWorker(player, (WorldServer) world);
		workers.add(worker);
		Thread thread = new Thread(worker, player.getName() + "'s terrain surface builder worker");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		TerrainVolumeBuilderWorker volumetricWorker = new TerrainVolumeBuilderWorker(player, (WorldServer) world);
		volumeWorkers.add(volumetricWorker);
		Thread thread2 = new Thread(volumetricWorker, player.getName() + "'s terrain volumetric builder worker");
		thread2.setPriority(Thread.MIN_PRIORITY);
		thread2.start();
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (!isRenderableWorld(world))
			return;
		for (TerrainSurfaceBuilderWorker worker : workers) {
			if (worker.getWorld() == world)
				worker.stop();
		}
		for (TerrainVolumeBuilderWorker worker : volumeWorkers) {
			if (worker.getWorld() == world)
				worker.stop();
		}
		network.sendCommandFlush();
	}
	
	@SubscribeEvent
	public void onCubeUnWatchEvent(CubeUnWatchEvent event) {
		World world = (World) event.getWorld();
		if (!isRenderableWorld(world))
			return;
//		WorldSavedDataTerrainSurface3d data = WorldSavedDataTerrainSurface3d.getOrCreateWorldSavedData(world);
	}

	private boolean isRenderableWorld(World world) {
		if (world.isRemote || !(world instanceof WorldServer))
			return false;
		if (world.provider.getDimension() != 0 || !((ICubicWorld) world).isCubicWorld())
			return false;
		// Vanilla generator have no methods of retrieving ChunkPrimer directly. Using
		// it to get chunks instead cause data corruption.
		if (world.getWorldType().getName().equals("VanillaCubic"))
			return false;
		return true;
	}

	public void dumpProgressInfo() {
		for (TerrainSurfaceBuilderWorker worker : workers) {
			worker.dumpProgressInfo = true;
		}
		for (TerrainVolumeBuilderWorker worker : volumeWorkers)
			worker.dumpProgressInfo = true;
	}
}
