package cwgfarplaneview.world.terrain.flat;

import static cwgfarplaneview.util.TerrainConfig.MESH_SIZE_BIT_CHUNKS;
import static cwgfarplaneview.util.TerrainUtil.*;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

public class TerrainPointProviderGeneratorBased implements TerrainPointProvider {
	private ICubeGenerator generator;
	private WorldServer worldServer;
	private int heightHint;

	TerrainPointProviderGeneratorBased(WorldServer worldServerIn, ICubeGenerator generatorIn) {
		generator = generatorIn;
		worldServer = worldServerIn;
	}
	
	@Override
	public TerrainPoint getTerrainPointAt(int meshX, int meshZ) throws IncorrectTerrainDataException {
		TerrainPoint point = this.getTerrainPointAt(meshX, meshZ, heightHint);
		heightHint = point.blockY;
		return point;
	} 
	
	private TerrainPoint getTerrainPointAt(int meshX, int meshZ, int heightHint) throws IncorrectTerrainDataException {
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
	
	private Biome getBiomeAt(int x, int z) {
		Biome[] biomes = new Biome[256];
		worldServer.getBiomeProvider().getBiomes(biomes, x << 4, z << 4, 16, 16, false);
		return biomes[0];
	}
}
