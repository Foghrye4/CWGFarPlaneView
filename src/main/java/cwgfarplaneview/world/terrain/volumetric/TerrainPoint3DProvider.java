package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

public abstract class TerrainPoint3DProvider {

	protected final WorldServer world;
	
	public TerrainPoint3DProvider(WorldServer worldIn) {
		world = worldIn;
	}

	abstract TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException;
	abstract protected void reset(int cubeX, int cubeY, int cubeZ);
	abstract protected IBlockState getBlockStateAt(int x,int y, int z);
	
	protected TerrainPoint3D getPointOf(int meshX, int meshY, int meshZ)
			throws IncorrectTerrainDataException {
		int cubeX = meshX << TerrainConfig.meshSizeBitChunks;
		int cubeZ = meshZ << TerrainConfig.meshSizeBitChunks;
		int cubeY = meshY << TerrainConfig.meshSizeBitChunks;
		int x0 = cubeX*16;
		int y0 = cubeY*16;
		int z0 = cubeZ*16;
		reset(cubeX,cubeY,cubeZ);
		
		IBlockState bs000 = getBlockStateAt(x0, y0, z0);
		IBlockState bsfff = getBlockStateAt(x0 + 15, y0 + 15, z0 + 15);
		boolean t000isAirOrWater = TerrainUtil.isAirOrWater(bs000);
		boolean tfffisAirOrWater = TerrainUtil.isAirOrWater(bsfff);
		if(t000isAirOrWater && tfffisAirOrWater)
			return new TerrainPoint3D(meshX, meshY, meshZ, (byte) 0, (byte) 0, (byte) 0, bs000,
				getBiomeAt(cubeX, cubeZ));
		if(!t000isAirOrWater && !tfffisAirOrWater)
			return new TerrainPoint3D(meshX, meshY, meshZ, (byte) 15, (byte) 15, (byte) 15, bsfff,
					getBiomeAt(cubeX, cubeZ));
		if(t000isAirOrWater && !tfffisAirOrWater) {
			boolean search = true;
			int ix =15;
			int iy =15;
			int iz =15;
			IBlockState bs = bsfff;
			IBlockState bsi = bsfff;
			while(search) {
				search = false;
				if (ix > 0 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix - 1, y0 + iy, z0 + iz))) {
					ix--;
					bs = bsi;
					search = true;
				}
				if (iy > 0 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix, y0 + iy - 1, z0 + iz))) {
					iy--;
					bs = bsi;
					search = true;
				}
				if (iz > 0 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix, y0 + iy, z0 + iz - 1))) {
					iz--;
					bs = bsi;
					search = true;
				}
			}
			return new TerrainPoint3D(meshX, meshY, meshZ, (byte) ix, (byte) iy, (byte) iz, bs,
					getBiomeAt(cubeX, cubeZ));
		}
		if(!t000isAirOrWater && tfffisAirOrWater) {
			boolean search = true;
			int ix = 0;
			int iy = 0;
			int iz = 0;
			IBlockState bs = bsfff;
			IBlockState bsi = bsfff;
			while(search) {
				search = false;
				if (ix < 15 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix + 1, y0 + iy, z0 + iz))) {
					ix++;
					bs = bsi;
					search = true;
				}
				if (iy < 15 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix, y0 + iy + 1, z0 + iz))) {
					iy++;
					bs = bsi;
					search = true;
				}
				if (iz < 15 && !TerrainUtil.isAirOrWater(bsi = getBlockStateAt(x0 + ix, y0 + iy, z0 + iz + 1))) {
					iz++;
					bs = bsi;
					search = true;
				}
			}
			return new TerrainPoint3D(meshX, meshY, meshZ, (byte) ix, (byte) iy, (byte) iz, bs,
					getBiomeAt(cubeX, cubeZ));
		}
		throw new IllegalStateException();
	}
	

	protected Biome getBiomeAt(int x, int z) {
		Biome[] biomes = new Biome[256];
		world.getBiomeProvider().getBiomes(biomes, x << 4, z << 4, 16, 16, false);
		return biomes[0];
	}
}
