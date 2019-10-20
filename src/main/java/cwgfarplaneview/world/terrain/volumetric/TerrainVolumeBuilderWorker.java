package cwgfarplaneview.world.terrain.volumetric;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.CWGFarPlaneViewMod.network;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_HORIZONTAL;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_VERTICAL;

import java.util.concurrent.ConcurrentLinkedQueue;

import cwgfarplaneview.event.CWGFarPlaneViewEventHandler;
import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface3d;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TerrainVolumeBuilderWorker implements Runnable {

	private EntityPlayerMP player;
	private final WorldServer world;
	private WorldSavedDataTerrainSurface3d data;
	private TerrainPoint3DProviderDiskData tpProvider;
	public final ConcurrentLinkedQueue<TerrainPoint3D> offthreadTerrainPointsUpdate = new ConcurrentLinkedQueue<TerrainPoint3D>();
	
	private volatile boolean run = true;
	public volatile boolean dumpProgressInfo = false;
	public int minimalX = Integer.MAX_VALUE;
	public int minimalY = Integer.MAX_VALUE;
	public int minimalZ = Integer.MAX_VALUE;
	public int maximalX = Integer.MAX_VALUE;
	public int maximalY = Integer.MAX_VALUE;
	public int maximalZ = Integer.MAX_VALUE;
	private final Object lock = new Object();
	private Thread thread;

	public TerrainVolumeBuilderWorker(EntityPlayerMP playerIn, WorldServer worldServerIn) {
		player = playerIn;
		world = worldServerIn;
		tpProvider = new TerrainPoint3DProviderDiskData(world, world.getBiomeProvider(),
				CustomGeneratorSettings.load(world), world.getSeed());
		logger.debug("3D builder worker for player " + player.getName() + " initialized.");
	}

	public void tick() throws IncorrectTerrainDataException {
		int px = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int py = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int pz = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		TerrainCube cube = null;
		if (px < minimalX || px > maximalX || py < minimalY || py > maximalY || pz < minimalZ || pz > maximalZ) {
			reset();
			cube = this.addCubeAt(px, py, pz);
			network.sendTerrainCubeToPlayer(player, cube);
		}
		EnumFacing closestSide = getSideClosestToPlayer(px, py, pz);
		if (closestSide == null) {
			try {
				synchronized (lock) {
					lock.wait(2500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		if (closestSide.getAxis() == Axis.X) {
			int x = closestSide == EnumFacing.EAST ? ++maximalX : --minimalX;
			for (int y = minimalY; y <= maximalY && run; y++) {
				for (int z = minimalZ; z <= maximalZ && run; z++) {
					cube = this.addCubeAt(x, y, z);
					network.sendTerrainCubeToPlayer(player, cube);
				}
			}
		} else if (closestSide.getAxis() == Axis.Z) {
			int z = closestSide == EnumFacing.SOUTH ? ++maximalZ : --minimalZ;
			for (int y = minimalY; y <= maximalY && run; y++) {
				for (int x = minimalX; x <= maximalX && run; x++) {
					cube = this.addCubeAt(x, y, z);
					network.sendTerrainCubeToPlayer(player, cube);
				}
			}
		} else if (closestSide.getAxis() == Axis.Y) {
			int y = closestSide == EnumFacing.UP ? ++maximalY : --minimalY;
			for (int z = minimalZ; z <= maximalZ && run; z++) {
				for (int x = minimalX; x <= maximalX && run; x++) {
					cube = this.addCubeAt(x, y, z);
					network.sendTerrainCubeToPlayer(player, cube);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void dumpProgressInfo(int generated, int overall) {
		player.sendMessage(new TextComponentString(String.format("Volumetric generated: %d, Overall: %d", generated, overall)));
		dumpProgressInfo = false;
	}

	private void reset() {
		minimalX = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		minimalY = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		minimalZ = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalX = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalY = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalZ = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
	}

	private TerrainCube addCubeAt(int x, int y, int z)
			throws IncorrectTerrainDataException {
		boolean newlyGenerated = false;
		TerrainCube cube = data.get(x, y, z);
		if(cube == null) {
			cube = this.tpProvider.getTerrainCubeAt(cube, x, y, z);
			newlyGenerated = true;
		}
		if(newlyGenerated) {
			data.addToMap(cube);
		}
		return cube;
	}

	private EnumFacing getSideClosestToPlayer(int px, int py, int pz) {
		int minXYZ = Integer.MAX_VALUE;
		EnumFacing closestSide = null;
		if (maximalX - px < minXYZ && maximalX - px < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXYZ = maximalX - px;
			closestSide = EnumFacing.EAST;
		}
		if (maximalZ - pz < minXYZ && maximalZ - pz < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXYZ = maximalZ - pz;
			closestSide = EnumFacing.SOUTH;
		}
		if (px - minimalX < minXYZ && px - minimalX < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXYZ = px - minimalX;
			closestSide = EnumFacing.WEST;
		}
		if (pz - minimalZ < minXYZ && pz - minimalZ < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXYZ = px - minimalZ;
			closestSide = EnumFacing.NORTH;
		}
		if (py - minimalY < minXYZ && py - minimalY < VOLUMETRIC_VERTICAL.maxUpdateDistanceChunks) {
			minXYZ = py - minimalY;
			closestSide = EnumFacing.DOWN;
		}
		if (maximalY - py < minXYZ && maximalY - py < VOLUMETRIC_VERTICAL.maxUpdateDistanceChunks) {
			minXYZ = maximalY - py;
			closestSide = EnumFacing.UP;
		}
		return closestSide;
	}

	@Override
	public void run() {
		try {
			data = WorldSavedDataTerrainSurface3d.getOrCreateWorldSavedData(world);
		} catch (ReportedException e) {
			logger.catching(e);
			data = new WorldSavedDataTerrainSurface3d();
		}
		try {
			synchronized (lock) {
				lock.wait(15000);
			}
			while (run && !player.isDead) {
				tick();
			}
			logger.info("Finishing 3D terrain builder thread");
			data.save(world);
			logger.info("3D Terrain data saved");
		} catch (IncorrectTerrainDataException | ReportedException | InterruptedException e) {
			logger.catching(e);
		} finally {
			run = false;
			CWGFarPlaneViewEventHandler.volumeWorkers.remove(this);
		}
	}

	public void stop() {
		thread.setPriority(Thread.MAX_PRIORITY);
		run = false;
	}

	public boolean canRun() {
		return run;
	}
	
	public World getWorld() {
		return world;
	}

	public void setThread(Thread threadIn) {
		thread = threadIn;
	}

	public void setPriority(int newPriority) {
		thread.setPriority(newPriority);
	}
}
