package cwgfarplaneview.util;

import static cwgfarplaneview.util.TerrainConfig.MESH_SIZE_BIT_BLOCKS;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
	
	public static boolean isAirOrWater(IBlockState state) {
		return state == Blocks.AIR.getDefaultState() || state.getMaterial() == Material.WATER;
	}

	public static Vec3f calculateNormal(TerrainPoint3D tp1, TerrainPoint3D tp2, TerrainPoint3D tp3) {
		int bx1 = tp1.meshX*16 + tp1.localX;
		int bz1 = tp1.meshZ*16 + tp1.localZ;
		int by1 = tp1.meshY*16 + tp1.localY;
		int bx2 = tp2.meshX*16 + tp2.localX;
		int bz2 = tp2.meshZ*16 + tp2.localZ;
		int by2 = tp2.meshY*16 + tp2.localY;
		int bx3 = tp3.meshX*16 + tp3.localX;
		int bz3 = tp3.meshZ*16 + tp3.localZ;
		int by3 = tp3.meshY*16 + tp3.localY;
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
	
	/**Normal non normalized*/
	public static Vec3i calculateNonNormalized(TerrainPoint3D tp1, TerrainPoint3D tp2, TerrainPoint3D tp3) {
		int bx1 = tp1.meshX;
		int bz1 = tp1.meshZ;
		int by1 = tp1.meshY;
		int bx2 = tp2.meshX;
		int bz2 = tp2.meshZ;
		int by2 = tp2.meshY;
		int bx3 = tp3.meshX;
		int bz3 = tp3.meshZ;
		int by3 = tp3.meshY;
		int v1x = bx1 - bx2;
		int v1y = by1 - by2;
		int v1z = bz1 - bz2;
		int v2x = bx3 - bx2;
		int v2y = by3 - by2;
		int v2z = bz3 - bz2;
		int nx = v1y * v2z - v1z * v2y;
		int ny = v1z * v2x - v1x * v2z;
		int nz = v1x * v2y - v1y * v2z;
		return new Vec3i(nx, ny, nz);
	}


	public static Vec3i vec3i(TerrainPoint3D from, TerrainPoint3D to) {
		return new Vec3i(to.meshX-from.meshX, to.meshY-from.meshY, to.meshZ-from.meshZ);
	}

	public static int crossAndDot(TerrainPoint3D base, TerrainPoint3D firstPoint,
			TerrainPoint3D secondPoint, TerrainPoint3D bissectPoint) {
		int v1x = firstPoint.getX() - base.getX();
		int v1y = firstPoint.getY() - base.getY();
		int v1z = firstPoint.getZ() - base.getZ();
		int v2x = secondPoint.getX() - base.getX();
		int v2y = secondPoint.getY() - base.getY();
		int v2z = secondPoint.getZ() - base.getZ();
		int v3x = bissectPoint.getX() - base.getX();
		int v3y = bissectPoint.getY() - base.getY();
		int v3z = bissectPoint.getZ() - base.getZ();

		int cx1 = v1y * v3z - v1z * v3y;
		int cy1 = v1z * v3x - v1x * v3z;
		int cz1 = v1x * v3y - v1y * v3z;
		int cx2 = v2y * v3z - v2z * v3y;
		int cy2 = v2z * v3x - v2x * v3z;
		int cz2 = v2x * v3y - v2y * v3z;
		
		return cx1*cx2 + cy1*cy2 + cz1*cz2;
	}

	public static boolean isOnASamePlane(TerrainPoint3D tp0, TerrainPoint3D tp1,
			TerrainPoint3D tp2, TerrainPoint3D tp3) {
		int v1x = tp1.getX() - tp0.getX();
		int v1y = tp1.getY() - tp0.getY();
		int v1z = tp1.getZ() - tp0.getZ();
		int v2x = tp2.getX() - tp0.getX();
		int v2y = tp2.getY() - tp0.getY();
		int v2z = tp2.getZ() - tp0.getZ();
		int v3x = tp3.getX() - tp0.getX();
		int v3y = tp3.getY() - tp0.getY();
		int v3z = tp3.getZ() - tp0.getZ();

		int cx1 = v1y * v3z - v1z * v3y;
		int cy1 = v1z * v3x - v1x * v3z;
		int cz1 = v1x * v3y - v1y * v3z;
		return cx1*v2x + cy1*v2y + cz1*v2z == 0;
	}

	public static int getSubstituteSkyLightValue(float normalY) {
		if(normalY>=0.0f)
			return 240;
		if(normalY<=-1.0f)
			return 0;
		return (int) (240*(normalY+1.0f));
	}
}
