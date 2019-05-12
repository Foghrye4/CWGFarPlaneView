package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public interface TerrainPoint3DProvider {

	TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException;
}
