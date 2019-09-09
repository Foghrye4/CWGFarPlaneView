package cwgfarplaneview.world.terrain.volumetric;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.CWGFarPlaneViewMod.network;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_HORIZONTAL;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_VERTICAL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cwgfarplaneview.event.CWGFarPlaneViewEventHandler;
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
	private TerrainPoint3DProvider diskTPProvider;
	private TerrainPoint3DProvider generatorTPProvider;
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
		generatorTPProvider = new TerrainPoint3DProviderCWGInternalsBased(world, world.getBiomeProvider(),
				CustomGeneratorSettings.load(world), world.getSeed());
		diskTPProvider = new TerrainPoint3DProviderDiskData(world);
		minimalX = player.chunkCoordX;
		minimalY = player.chunkCoordY;
		minimalZ = player.chunkCoordZ;
		maximalX = player.chunkCoordX;
		maximalY = player.chunkCoordY;
		maximalZ = player.chunkCoordZ;
		logger.debug("3D builder worker for player " + player.getName() + " initialized.");
	}

	public void tick() throws IncorrectTerrainDataException {
		int px = player.chunkCoordX;
		int py = player.chunkCoordY;
		int pz = player.chunkCoordZ;
		if (px < minimalX || px > maximalX || py < minimalY || py > maximalY || pz < minimalZ || pz > maximalZ) {
			reset();
		}
		List<TerrainPoint3D> pointsList = new ArrayList<TerrainPoint3D>();
		EnumFacing closestSide = getSideClosestToPlayer(px, py, pz);
		int pointsGeneratedThisTick = 0;
		a: while (closestSide != null && pointsGeneratedThisTick < 204 && pointsList.size() < 409) {
			if (closestSide.getAxis() == Axis.X) {
				int x = closestSide == EnumFacing.EAST ? ++maximalX : --minimalX;
				for (int y = minimalY; y <= maximalY; y++) {
					for (int z = minimalZ; z <= maximalZ; z++) {
						if (this.addPointAt(pointsList, x, y, z))
							pointsGeneratedThisTick++;
						if (!run || player.isDead)
							break a;
						if (dumpProgressInfo)
							this.dumpProgressInfo(pointsGeneratedThisTick, pointsList.size());
					}
				}
			} else if (closestSide.getAxis() == Axis.Z) {
				int z = closestSide == EnumFacing.SOUTH ? ++maximalZ : --minimalZ;
				for (int y = minimalY; y <= maximalY; y++) {
					for (int x = minimalX; x <= maximalX; x++) {
						if (this.addPointAt(pointsList, x, y, z))
							pointsGeneratedThisTick++;
						if (!run || player.isDead)
							break a;
						if (dumpProgressInfo)
							this.dumpProgressInfo(pointsGeneratedThisTick, pointsList.size());
					}
				}
			} else {
				int y = closestSide == EnumFacing.UP ? ++maximalY : --minimalY;
				for (int z = minimalZ; z <= maximalZ; z++) {
					for (int x = minimalX; x <= maximalX; x++) {
						if (this.addPointAt(pointsList, x, y, z))
							pointsGeneratedThisTick++;
						if (!run || player.isDead)
							break a;
						if (dumpProgressInfo)
							this.dumpProgressInfo(pointsGeneratedThisTick, pointsList.size());
					}
				}
			}
			closestSide = getSideClosestToPlayer(px, py, pz);
		}
		while(!offthreadTerrainPointsUpdate.isEmpty()) {
			TerrainPoint3D tp = offthreadTerrainPointsUpdate.poll();
			if (tp.meshX >= minimalX && tp.meshX <= maximalX && tp.meshY >= minimalY && tp.meshY <= maximalY && tp.meshZ >= minimalZ && tp.meshZ <= maximalZ) {
				pointsList.add(tp);
			}
			// Terrain points outside of borders will be retrieved from world saved data.
		}
		if (!player.isDead && !pointsList.isEmpty()) {
			network.send3DTerrainPointsToClient(player, pointsList);
		}
		if (closestSide == null) {
			try {
				synchronized (lock) {
					lock.wait(2500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void dumpProgressInfo(int generated, int overall) {
		player.sendMessage(new TextComponentString(String.format("Volumetric generated: %d, Overall: %d", generated, overall)));
		dumpProgressInfo = false;
	}

	private void reset() {
		minimalX = player.chunkCoordX;
		minimalY = player.chunkCoordY;
		minimalZ = player.chunkCoordZ;
		maximalX = player.chunkCoordX;
		maximalY = player.chunkCoordY;
		maximalZ = player.chunkCoordZ;
	}

	private boolean addPointAt(List<TerrainPoint3D> pointsList, int x, int y, int z)
			throws IncorrectTerrainDataException {
		boolean newlyGenerated = false;
		TerrainPoint3D point = data.get(x, y, z);
		if (point == null) {
			newlyGenerated = true;
			point = this.diskTPProvider.getTerrainPointAt(x, y, z);
		}
		if (point == null) {
			newlyGenerated = true;
			point = this.generatorTPProvider.getTerrainPointAt(x, y, z);
		}
		if(newlyGenerated)
			data.addToMap(point);
		pointsList.add(point);
		return newlyGenerated;
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
