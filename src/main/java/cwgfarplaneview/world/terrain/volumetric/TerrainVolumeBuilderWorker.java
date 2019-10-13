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
	private TerrainPoint3DProviderCWGInternalsBased tpProvider;
	public final ConcurrentLinkedQueue<TerrainPoint3D> offthreadTerrainPointsUpdate = new ConcurrentLinkedQueue<TerrainPoint3D>();
	
	private volatile boolean run = true;
	public volatile boolean dumpProgressInfo = false;
	public int minimalX;
	public int minimalY;
	public int minimalZ;
	public int maximalX;
	public int maximalY;
	public int maximalZ;
	private final Object lock = new Object();

	public TerrainVolumeBuilderWorker(EntityPlayerMP playerIn, WorldServer worldServerIn) {
		player = playerIn;
		world = worldServerIn;
		tpProvider = new TerrainPoint3DProviderCWGInternalsBased(world, world.getBiomeProvider(),
				CustomGeneratorSettings.load(world), world.getSeed());
		minimalX = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		minimalY = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		minimalZ = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalX = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalY = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		maximalZ = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		logger.debug("3D builder worker for player " + player.getName() + " initialized.");
	}

	public void tick() throws IncorrectTerrainDataException {
		int px = player.chunkCoordX >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int py = player.chunkCoordY >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int pz = player.chunkCoordZ >> TerrainConfig.CUBE_SIZE_BIT_MESH + TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		if (px < minimalX || px > maximalX || py < minimalY || py > maximalY || pz < minimalZ || pz > maximalZ) {
			reset();
		}
		TerrainCube cube = null;
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
			for (int y = minimalY; y <= maximalY; y++) {
				for (int z = minimalZ; z <= maximalZ; z++) {
					cube = this.addCubeAt(x, y, z);
					network.sendTerrainCubeToPlayer(player, cube);
				}
			}
		} else if (closestSide.getAxis() == Axis.Z) {
			int z = closestSide == EnumFacing.SOUTH ? ++maximalZ : --minimalZ;
			for (int y = minimalY; y <= maximalY; y++) {
				for (int x = minimalX; x <= maximalX; x++) {
					cube = this.addCubeAt(x, y, z);
					network.sendTerrainCubeToPlayer(player, cube);
				}
			}
		} else if (closestSide.getAxis() == Axis.Y) {
			int y = closestSide == EnumFacing.UP ? ++maximalY : --minimalY;
			for (int z = minimalZ; z <= maximalZ; z++) {
				for (int x = minimalX; x <= maximalX; x++) {
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
		int minXZ = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		EnumFacing closestSide = null;
		if (maximalX - px < minXZ && maximalX - px < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXZ = maximalX - px;
			closestSide = EnumFacing.EAST;
		}
		if (maximalY - py < minY && maximalY - py < VOLUMETRIC_VERTICAL.maxUpdateDistanceChunks) {
			minY = maximalY - py;
			closestSide = EnumFacing.UP;
		}
		if (maximalZ - pz < minXZ && maximalZ - pz < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXZ = maximalZ - pz;
			closestSide = EnumFacing.SOUTH;
		}
		if (px - minimalX < minXZ && px - minimalX < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXZ = px - minimalX;
			closestSide = EnumFacing.WEST;
		}
		if (py - minimalY < minY && py - minimalY < VOLUMETRIC_VERTICAL.maxUpdateDistanceChunks) {
			minY = py - minimalY;
			closestSide = EnumFacing.DOWN;
		}
		if (pz - minimalZ < minXZ && pz - minimalZ < VOLUMETRIC_HORIZONTAL.maxUpdateDistanceChunks) {
			minXZ = px - minimalZ;
			closestSide = EnumFacing.NORTH;
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
			logger.info("Finishing terrain builder thread");
			data.save(world);
			logger.info("Terrain data saved");
		} catch (IncorrectTerrainDataException | ReportedException | InterruptedException e) {
			logger.catching(e);
		} finally {
			run = false;
			CWGFarPlaneViewEventHandler.volumeWorkers.remove(this);
		}
	}

	public void stop() {
		run = false;
	}

	public World getWorld() {
		return world;
	}
}
