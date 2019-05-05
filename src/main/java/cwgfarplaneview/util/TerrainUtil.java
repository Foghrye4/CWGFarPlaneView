package cwgfarplaneview.util;

import cwgfarplaneview.world.terrain.TerrainPoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

public class TerrainUtil {

	public static TerrainPoint interpolateBetween(TerrainPoint tp1, TerrainPoint tp2, int x, int z) {
		IBlockState blockState = tp1.blockState;
		Biome biome = tp1.biome;
		int dx = tp2.chunkX - x;
		int dz = tp2.chunkZ - z;
		int d2 = dx * dx + dz * dz;
		dx = tp1.chunkX - x;
		dz = tp1.chunkZ - z;
		int d1 = dx * dx + dz * dz;
		int d = d1 + d2;
		int blockY = (tp1.blockY * d2 / d + tp2.blockY * d1 / d) / 2;
		return new TerrainPoint(x, z, blockY, blockState, biome);
	}
}
