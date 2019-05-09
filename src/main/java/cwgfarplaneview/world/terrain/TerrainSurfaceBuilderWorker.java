package cwgfarplaneview.world.terrain;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.CWGFarPlaneViewMod.network;
import static cwgfarplaneview.util.AddressUtil.MAX_UPDATE_DISTANCE_CHUNKS;

import java.util.ArrayList;
import java.util.List;

import cwgfarplaneview.event.CWGFarPlaneViewEventHandler;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomTerrainGenerator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;

public class TerrainSurfaceBuilderWorker implements Runnable {

	private EntityPlayerMP player;
	private final WorldServer world;
	private WorldSavedDataTerrainSurface data;
	private TerrainPointProvider tpProvider;
	private volatile boolean run = true;
	public int minimalX;
	public int minimalZ;
	public int maximalX;
	public int maximalZ;

	public TerrainSurfaceBuilderWorker(EntityPlayerMP playerIn, WorldServer worldServerIn) {
		player = playerIn;
		world = worldServerIn;
		CubeProviderServer cubeProvider = (CubeProviderServer) worldServerIn.getChunkProvider();
		ICubeGenerator generator = cubeProvider.getCubeGenerator();
		if(generator instanceof CustomTerrainGenerator)
			tpProvider = new TerrainPointProviderCWGInternalsBased(world,world.getBiomeProvider(),CustomGeneratorSettings.load(world), world.getSeed());
		else
			tpProvider = new TerrainPointProviderGeneratorBased(world,generator);
		minimalX = player.chunkCoordX;
		minimalZ = player.chunkCoordZ;
		maximalX = player.chunkCoordX;
		maximalZ = player.chunkCoordZ;
		logger.debug("Surface builder worker for player " + player.getName() + " initialized.");
	}

	public void tick() throws IncorrectTerrainDataException {
		int px = player.chunkCoordX;
		int pz = player.chunkCoordZ;
		if (px < minimalX || px > maximalX 
				|| pz < minimalZ || pz > maximalZ) {
			reset();
		}
		List<TerrainPoint> pointsList = new ArrayList<TerrainPoint>();
		EnumFacing closestSide = getSideClosestToPlayer(px,pz);
		int pointsGeneratedThisTick = 0;
		while (run && !player.isDead && closestSide != EnumFacing.UP && pointsGeneratedThisTick < 2048 && pointsList.size() < 4096) {
			if (closestSide.getAxis() == Axis.X) {
				int x = closestSide == EnumFacing.EAST ?++maximalX : --minimalX;
				for (int z = minimalZ; z <= maximalZ; z ++) {
					if (this.addPointAt(pointsList, x, z))
						pointsGeneratedThisTick++;
				}
			} else {
				int z = closestSide == EnumFacing.SOUTH ? ++maximalZ : --minimalZ;
				for (int x = minimalX; x <= maximalX; x ++) {
					if (this.addPointAt(pointsList, x, z))
						pointsGeneratedThisTick++;
				}
			}
			closestSide = getSideClosestToPlayer(px,pz);
		}
		if (!world.playerEntities.isEmpty() && !pointsList.isEmpty()) {
			logger.debug("Sending points");
			network.sendTerrainPointsToAllClients(pointsList);
		}
	}

	private void reset() {
		minimalX = player.chunkCoordX;
		minimalZ = player.chunkCoordZ;
		maximalX = player.chunkCoordX;
		maximalZ = player.chunkCoordZ;
	}

	private boolean addPointAt(List<TerrainPoint> pointsList, int x, int z) throws IncorrectTerrainDataException {
		boolean newlyGenerated = false;
		TerrainPoint point = data.get(x, z);
		if (point == null) {
			point = this.tpProvider.getTerrainPointAt(x, z);
			data.addToMap(point);
			newlyGenerated = true;
		}
		pointsList.add(point);
		return newlyGenerated;
	}

	private EnumFacing getSideClosestToPlayer(int px,int pz) {
		int minXZ = Integer.MAX_VALUE;
		EnumFacing closestSide = EnumFacing.UP;
		if (maximalX - px < minXZ) {
			minXZ = maximalX - px;
			closestSide = EnumFacing.EAST;
		}
		if (maximalZ - pz < minXZ) {
			minXZ = maximalZ - pz;
			closestSide = EnumFacing.SOUTH;
		}
		if (px - minimalX < minXZ) {
			minXZ = px - minimalX;
			closestSide = EnumFacing.WEST;
		}
		if (pz - minimalZ < minXZ) {
			minXZ = px - minimalZ;
			closestSide = EnumFacing.NORTH;
		}
		if (minXZ > MAX_UPDATE_DISTANCE_CHUNKS)
			return EnumFacing.UP;
		return closestSide;
	}

	@Override
	public void run() {
		try {
			data = WorldSavedDataTerrainSurface.getOrCreateWorldSavedData(world);
		} catch (ReportedException e) {
			logger.catching(e);
			data = new WorldSavedDataTerrainSurface();
		}
		try {
			while (run && !player.isDead) {
				tick();
			}
			logger.info("Finishing terrain builder thread");
			data.save(world);
			logger.info("Terrain data saved");
		} catch (IncorrectTerrainDataException | ReportedException e) {
			logger.catching(e);
		} finally {
			run = false;
			CWGFarPlaneViewEventHandler.workers.remove(this);
		}
	}
	
	public void stop() {
		run = false;
	}
}
