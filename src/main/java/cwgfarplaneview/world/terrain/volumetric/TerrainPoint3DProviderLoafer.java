package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;

public class TerrainPoint3DProviderLoafer extends TerrainPoint3DProviderLayered {

	public TerrainPoint3DProviderLoafer(WorldServer worldIn) {
		super(worldIn);
	}

	@Override
	TerrainPoint3DProvider getGenerator(int cubeX, int cubeY, int cubeZ) {
		return this;
	}

	@Override
	TerrainCube getTerrainCubeAt(TerrainCube cube, int tcX, int tcY, int tcZ) {
		if (cube == null)
			cube = new TerrainCube(world, tcX, tcY, tcZ);
		return cube;
	}

	@Override
	protected void getPointOf(TerrainCube cube, int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException {
	}

	@Override
	protected void reset(int cubeX, int cubeY, int cubeZ) {
	}

	@Override
	protected IBlockState getBlockStateAt(int x, int y, int z) {
		return Blocks.AIR.getDefaultState();
	}

	@Override
	protected int getBlockLightAt(int localX, int localY, int localZ) {
		return 0;
	}

	@Override
	protected int getSkyLightAt(int localX, int localY, int localZ) {
		return 0;
	}
}
