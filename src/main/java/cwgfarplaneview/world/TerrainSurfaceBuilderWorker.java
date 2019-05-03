package cwgfarplaneview.world;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.CWGFarPlaneViewMod.network;
import static cwgfarplaneview.util.AddressUtil.MAX_UPDATE_DISTANCE_CHUNKS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_CHUNKS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.core.worldgen.generator.vanilla.VanillaCompatibilityGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

public class TerrainSurfaceBuilderWorker implements Runnable {

	private final static int MESH_CELL_SIZE_BIT = 0;
	private EntityPlayerMP player;
	private final WorldServer worldServer;
	private final WorldSavedDataTerrainSurface data;
	private ICubeGenerator generator;
	private int heightHint = 64;
	private boolean flush = false;
	public int minimalX;
	public int minimalZ;
	public int maximalX;
	public int maximalZ;

	public TerrainSurfaceBuilderWorker(EntityPlayerMP playerIn, WorldServer worldServerIn, WorldSavedDataTerrainSurface dataIn) {
		player = playerIn;
		worldServer = worldServerIn;
		data = dataIn;
		CubeProviderServer cubeProvider = (CubeProviderServer) worldServerIn.getChunkProvider();
		generator = cubeProvider.getCubeGenerator();
		minimalX = player.chunkCoordX;
		minimalZ = player.chunkCoordZ;
		maximalX = player.chunkCoordX;
		maximalZ = player.chunkCoordZ;
		logger.info("Surface builder worker for player " + player.getName() + " initialized.");
	}

	public void tick() {
		if (flush) {
			data.clear();
			flush = false;
			return;
		}
		if(player.chunkCoordX < minimalX || player.chunkCoordX > maximalX || player.chunkCoordZ < minimalZ || player.chunkCoordZ > maximalZ) {
			minimalX = player.chunkCoordX;
			minimalZ = player.chunkCoordZ;
			maximalX = player.chunkCoordX;
			maximalZ = player.chunkCoordZ;
		}
		List<TerrainPoint> pointsList = new ArrayList<TerrainPoint>();
		EnumFacing closestSide = getSideClosestToPlayer();
		while (closestSide != EnumFacing.UP && pointsList.size() < 4096) {
			if (closestSide.getAxis() == Axis.X) {
				int x = closestSide == EnumFacing.EAST ? maximalX + 1 : minimalX - 1;
				for (int z = minimalZ; z <= maximalZ; z++) {
					this.generatePoint(pointsList, x, z);
				}
			} else {
				int z = closestSide == EnumFacing.SOUTH ? maximalZ + 1 : minimalZ - 1;
				for (int x = minimalX; x <= maximalX; x++) {
					this.generatePoint(pointsList, x, z);
				}
			}
			closestSide = getSideClosestToPlayer();
		}
		if (!worldServer.playerEntities.isEmpty() && !pointsList.isEmpty()) {
			network.sendTerrainPointsToAllClients(pointsList);
		}
	}

	private void generatePoint(List<TerrainPoint> pointsList, int x, int z) {
		TerrainPoint point = data.get(x, z);
		if (point == null) {
			point = this.getTerrainPointAt(x, z, heightHint);
			heightHint = point.blockY;
			data.addToMap(point);
		}
		if (point.getX() < this.minimalX)
			this.minimalX = point.getX();
		if (point.getZ() < this.minimalZ)
			this.minimalZ = point.getZ();
		if (point.getX() > this.maximalX)
			this.maximalX = point.getX();
		if (point.getZ() > this.maximalZ)
			this.maximalZ = point.getZ();
		pointsList.add(point);
	}

	private EnumFacing getSideClosestToPlayer() {
		int minXZ = Integer.MAX_VALUE;
		EnumFacing closestSide = EnumFacing.UP;
			int pccX = player.chunkCoordX;
			int pccZ = player.chunkCoordZ;
			if (maximalX - pccX < minXZ) {
				minXZ = maximalX - pccX;
				closestSide = EnumFacing.EAST;
			}
			if (maximalZ - pccZ < minXZ) {
				minXZ = maximalZ - pccZ;
				closestSide = EnumFacing.SOUTH;
			}
			if (pccX - minimalX < minXZ) {
				minXZ = pccX - minimalX;
				closestSide = EnumFacing.WEST;
			}
			if (pccZ - minimalZ < minXZ) {
				minXZ = pccX - minimalZ;
				closestSide = EnumFacing.NORTH;
			}
			if (minXZ > MAX_UPDATE_DISTANCE_CHUNKS)
				return EnumFacing.UP;
		return closestSide;
	}

	private TerrainPoint getTerrainPointAt(int meshX, int meshZ, int heightHint) {
		int cubeY = heightHint >> 4;
		int cubeX = meshX << MESH_SIZE_BIT_CHUNKS;
		int cubeZ = meshZ << MESH_SIZE_BIT_CHUNKS;
		CubePrimer primer = generator.generateCube(cubeX, cubeY, cubeZ);
		while (isAirOrWater(primer.getBlockState(0, 0, 0))) {
			primer = generator.generateCube(cubeX, --cubeY, cubeZ);
		}
		while (!isAirOrWater(primer.getBlockState(0, 15, 0))) {
			primer = generator.generateCube(cubeX, ++cubeY, cubeZ);
		}
		for (int iy = 0; iy < 16; iy++) {
			if (isAirOrWater(primer.getBlockState(0, iy, 0))) {
				if (iy == 0) {
					primer = generator.generateCube(cubeX, --cubeY, cubeZ);
					continue;
				}
				int height = (cubeY << 4) + --iy + 1;
				return new TerrainPoint(meshX, meshZ, height, primer.getBlockState(0, iy, 0), getBiomeAt(cubeX, cubeZ));
			}
		}
		int height = (cubeY + 1 << 4) + 1;
		return new TerrainPoint(meshX, meshZ, height, primer.getBlockState(0, 15, 0), getBiomeAt(cubeX, cubeZ));
	}

	private boolean isAirOrWater(IBlockState state) {
		return state == Blocks.AIR.getDefaultState() || state.getMaterial() == Material.WATER;
	}

	private Biome getBiomeAt(int x, int z) {
		Biome[] biomes = new Biome[256];
		worldServer.getBiomeProvider().getBiomes(biomes, x << 4, z << 4, 16, 16, false);
		return biomes[0];
	}

	@Override
	public void run() {
		while (!player.isDead) {
			tick();
		}
	}
}
