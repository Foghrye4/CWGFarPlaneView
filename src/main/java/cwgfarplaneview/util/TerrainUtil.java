package cwgfarplaneview.util;

import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.TerrainPoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
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
		try {
			return new TerrainPoint(x, z, blockY, blockState, biome);
		} catch (IncorrectTerrainDataException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	public static Vec3f calculateNormal(TerrainPoint tp1, TerrainPoint tp2, TerrainPoint tp3) {
		int bx1 = tp1.chunkX << MESH_SIZE_BIT_BLOCKS;
		int bz1 = tp1.chunkZ << MESH_SIZE_BIT_BLOCKS;
		int by1 = tp1.blockY;
		int bx2 = tp2.chunkX << MESH_SIZE_BIT_BLOCKS;
		int bz2 = tp2.chunkZ << MESH_SIZE_BIT_BLOCKS;
		int by2 = tp2.blockY;
		int bx3 = tp3.chunkX << MESH_SIZE_BIT_BLOCKS;
		int bz3 = tp3.chunkZ << MESH_SIZE_BIT_BLOCKS;
		int by3 = tp3.blockY;
		int v1x = bx1 - bx2;
		int v1y = by1 - by2;
		int v1z = bz1 - bz2;
		int v2x = bx3 - bx2;
		int v2y = by3 - by2;
		int v2z = bz3 - bz2;
		int nx = v1y * v2z - v1z * v2y;
		int ny = v1z * v2x - v1x * v2z;
		int nz = v1x * v2y - v1y * v2z;
		float d = nx * nx + ny * ny + nz * nz;
		d = (float) Math.sqrt(d);
		return new Vec3f(nx / d, ny / d, nz / d);
	}
}
