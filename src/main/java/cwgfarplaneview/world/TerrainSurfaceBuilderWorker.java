package cwgfarplaneview.world;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;
import static cwgfarplaneview.CWGFarPlaneViewMod.proxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomTerrainGenerator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

public class TerrainSurfaceBuilderWorker implements Runnable {

	public final static int MAX_UPDATE_DISTANCE = 92;

	private final WorldServer worldServer;
	private final WorldSavedDataTerrainSurface data;
	private ICubeGenerator generator;
	private int heightHint = 64;
	private boolean run = true;
	private boolean flush = false;
	private final List<EntityPlayerMP> reciveAllPointsRequests = new ArrayList<EntityPlayerMP>();
	private final Set<EntityPlayerMP> players = new HashSet<EntityPlayerMP>();
			
	public TerrainSurfaceBuilderWorker(WorldServer worldServerIn) {
		worldServer = worldServerIn;
		data = WorldSavedDataTerrainSurface.getOrCreateWorldSavedData(worldServerIn);
		CustomGeneratorSettings settings = CustomGeneratorSettings.load(worldServerIn);
		generator = new CustomTerrainGenerator(worldServerIn, CustomCubicWorldType.makeBiomeProvider(worldServerIn, settings), settings, worldServerIn.getSeed());
	}

	public void tick() {
		if (flush) {
			data.clear();
			flush = false;
			return;
		}
		if (data.lock) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
			return;
		}
		List<TerrainPoint> pointsList = new ArrayList<TerrainPoint>();
		EnumFacing closestSide = getSideClosestToPlayer();
		while (closestSide != EnumFacing.UP && pointsList.size() < 4096) {
			if (closestSide.getAxis() == Axis.X) {
				int x = closestSide == EnumFacing.EAST ? data.maximalX + 1 : data.minimalX - 1;
				for (int z = data.minimalZ; z <= data.maximalZ; z++) {
					this.generatePoint(pointsList, x, z);
				}
			} else {
				int z = closestSide == EnumFacing.SOUTH ? data.maximalZ + 1 : data.minimalZ - 1;
				for (int x = data.minimalX; x <= data.maximalX; x++) {
					this.generatePoint(pointsList, x, z);
				}
			}
		}
		if (!reciveAllPointsRequests.isEmpty()) {
			for (EntityPlayerMP player : reciveAllPointsRequests) {
				network.sendAllTerrainPointsToClient(player, data.terrainMap);
			}
			players.addAll(reciveAllPointsRequests);
			reciveAllPointsRequests.clear();
		}
		if (!worldServer.playerEntities.isEmpty()) {
			network.sendTerrainPointsToAllClients(pointsList);
		}
	}
	
	private void generatePoint(List<TerrainPoint> pointsList, int x, int z) {
		TerrainPoint point = this.getTerrainPointAt(x, z, heightHint);
		heightHint = point.blockY;
		data.addToMap(point);
		pointsList.add(point);
	}

	private EnumFacing getSideClosestToPlayer() {
		int minXZ = Integer.MAX_VALUE;
		EnumFacing closestSide = EnumFacing.UP;
		EntityPlayer player = getPlayerClosestToSide();
		if (player != null) {
			int pccX = player.chunkCoordX;
			int pccZ = player.chunkCoordZ;
			if (data.maximalX - pccX < minXZ) {
				minXZ = data.maximalX - pccX;
				closestSide = EnumFacing.EAST;
			}
			if (data.maximalZ - pccZ < minXZ) {
				minXZ = data.maximalZ - pccZ;
				closestSide = EnumFacing.SOUTH;
			}
			if (pccX - data.minimalX < minXZ) {
				minXZ = pccX - data.minimalX;
				closestSide = EnumFacing.WEST;
			}
			if (pccZ - data.minimalZ < minXZ) {
				minXZ = pccX - data.minimalZ;
				closestSide = EnumFacing.NORTH;
			}
			if (minXZ > MAX_UPDATE_DISTANCE)
				return EnumFacing.UP;
		} else {
			int dx = data.maximalX - data.minimalX;
			int dz = data.maximalZ - data.minimalZ;
			if (dx > MAX_UPDATE_DISTANCE * 2 && dz > MAX_UPDATE_DISTANCE * 2)
				return EnumFacing.UP;
			if (dx < dz) {
				if (data.maximalX < -data.minimalX)
					return EnumFacing.EAST;
				return EnumFacing.WEST;
			} else {
				if (data.maximalZ < -data.minimalZ)
					return EnumFacing.SOUTH;
				return EnumFacing.NORTH;
			}
		}
		return closestSide;
	}

	@Nullable
	private EntityPlayer getPlayerClosestToSide() {
		int minXZ = Integer.MAX_VALUE;
		EntityPlayer closestPlayer = null;
		Iterator<EntityPlayerMP> pi = players.iterator();
		while(pi.hasNext()) {
			EntityPlayerMP player = pi.next();
			if (player == null || player.isDead || !(player instanceof EntityPlayerMP)) {
				pi.remove();
				continue;
			}
			int pccX = player.chunkCoordX;
			int pccZ = player.chunkCoordZ;
			if (data.maximalX - pccX < minXZ) {
				minXZ = data.maximalX - pccX;
				closestPlayer = player;
			}
			if (data.maximalZ - pccZ < minXZ) {
				minXZ = data.maximalZ - pccZ;
				closestPlayer = player;
			}
			if (pccX - data.minimalX < minXZ) {
				minXZ = pccX - data.minimalX;
				closestPlayer = player;
			}
			if (pccZ - data.minimalZ < minXZ) {
				minXZ = pccX - data.minimalZ;
				closestPlayer = player;
			}
		}
		return closestPlayer;
	}

	private TerrainPoint getTerrainPointAt(int x, int z, int heightHint) {
		int y = heightHint >> 4;
		CubePrimer primer = generator.generateCube(x, y, z);
		while (isAirOrWater(primer.getBlockState(0, 0, 0))) {
			primer = generator.generateCube(x, --y, z);
		}
		while (!isAirOrWater(primer.getBlockState(0, 15, 0))) {
			primer = generator.generateCube(x, ++y, z);
		}
		for (int iy = 0; iy < 16; iy++) {
			if (isAirOrWater(primer.getBlockState(0, iy, 0))) {
				if (iy == 0) {
					primer = generator.generateCube(x, --y, z);
					continue;
				}
				int height = (y << 4) + --iy + 1;
				return new TerrainPoint(x, z, height, primer.getBlockState(0, iy, 0), getBiomeAt(x, z));
			}
		}
		int height = (y + 1 << 4) + 1;
		return new TerrainPoint(x, z, height, primer.getBlockState(0, 15, 0), getBiomeAt(x, z));
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
		while (run) {
			tick();
		}
	}

	public void stop() {
		run = false;
	}

	public void flush() {
		flush = true;
	}

	public void sendAllDataToPlayer(EntityPlayerMP entity) {
		reciveAllPointsRequests.add(entity);
	}
}
