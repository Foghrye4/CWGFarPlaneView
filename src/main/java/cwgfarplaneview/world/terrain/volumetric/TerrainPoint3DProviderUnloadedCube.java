package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

public class TerrainPoint3DProviderUnloadedCube extends TerrainPoint3DProvider {
	private MutableBlockPos mbpos = new MutableBlockPos();
	private ICube cachedCube;

	public TerrainPoint3DProviderUnloadedCube() {
		super(null);
	}

	@Override
	public TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException {
		int cubeX = meshX << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int cubeZ = meshZ << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int cubeY = meshY << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		TerrainPoint3D tp = getPointOf(meshX, meshY, meshZ);
		mbpos.setPos(Coords.cubeToMinBlock(cubeX) + tp.localX, Coords.cubeToMinBlock(cubeY) + tp.localY,
				Coords.cubeToMinBlock(cubeZ) + tp.localZ);
		tp.blockLight = (byte) cachedCube.getLightFor(EnumSkyBlock.BLOCK, mbpos);
   		tp.skyLight = (byte) cachedCube.getLightFor(EnumSkyBlock.SKY, mbpos);
   		cachedCube = null;
		return tp;
	}
	
	public void setCube(ICube iCube) {
		cachedCube = iCube;
	}
	
	@Override
	protected void reset(int cubeX, int cubeY, int cubeZ) {	}
		
	@Override
	protected IBlockState getBlockStateAt(int x, int y, int z) {
		return cachedCube.getBlockState(x, y, z);
	}
	
	@Override
	protected Biome getBiomeAt(int blockX, int blockY, int blockZ) {
		mbpos.setPos(blockX, blockY, blockZ);
		return cachedCube.getBiome(mbpos);
	}
}
