package cwgfarplaneview.event;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import java.util.HashSet;
import java.util.Set;

import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface3d;
import cwgfarplaneview.world.terrain.flat.TerrainSurfaceBuilderWorker;
import cwgfarplaneview.world.terrain.volumetric.TerrainVolumeBuilderWorker;
import static cwgfarplaneview.util.TerrainConfig.*;
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
		volumetricWorker.setThread(thread2);
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
		boolean canRun = false;
		for (TerrainVolumeBuilderWorker worker : volumeWorkers) {
			if (worker.canRun() && worker.getWorld() == world) {
				canRun = true;
			}
		}
		if(canRun) {
			int x = event.getCubePos().getX() >> CUBE_SIZE_BIT_MESH + MESH_SIZE_BIT_CHUNKS;
			int y = event.getCubePos().getY() >> CUBE_SIZE_BIT_MESH + MESH_SIZE_BIT_CHUNKS;
			int z = event.getCubePos().getZ() >> CUBE_SIZE_BIT_MESH + MESH_SIZE_BIT_CHUNKS;
			WorldSavedDataTerrainSurface3d ref = WorldSavedDataTerrainSurface3d.instance;
			if (ref != null)
				ref.removeFromMap(x, y, z);
		}
	}

	private boolean isRenderableWorld(World world) {
		if (world.isRemote || !(world instanceof WorldServer))
			return false;
		if (world.provider.getDimension() != 0 || !((ICubicWorld) world).isCubicWorld())
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

	public void setPriority(int priorityIn) {
		for (TerrainVolumeBuilderWorker worker : volumeWorkers) {
			worker.setPriority(priorityIn);
		}
	}
}
