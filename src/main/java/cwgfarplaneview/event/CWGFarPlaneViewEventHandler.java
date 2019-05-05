package cwgfarplaneview.event;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import java.util.HashSet;
import java.util.Set;

import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import cwgfarplaneview.world.terrain.TerrainSurfaceBuilderWorker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;

public class CWGFarPlaneViewEventHandler {

	public static Set<TerrainSurfaceBuilderWorker> workers = new HashSet<TerrainSurfaceBuilderWorker>();

	@SubscribeEvent
	public void onWorldLoadEvent(EntityJoinWorldEvent event) {
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
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (!isRenderableWorld(world))
			return;
		for (TerrainSurfaceBuilderWorker worker : workers)
			worker.stop();
		network.sendCommandFlush();
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
}
