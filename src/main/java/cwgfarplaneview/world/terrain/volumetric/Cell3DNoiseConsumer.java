package cwgfarplaneview.world.terrain.volumetric;

import java.util.List;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseConsumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class Cell3DNoiseConsumer implements NoiseConsumer {

	List<IBiomeBlockReplacer> replacers;
	private final BiomeSource biomeSource;
	public IBlockState blockState;

	Cell3DNoiseConsumer(BiomeSource biomeSourceIn) {
		biomeSource = biomeSourceIn;
	}

	@Override
	public void accept(int x, int y, int z, double dx, double dy, double dz, double density) {
		blockState = this.getBlock(x, y, z, dx, dy, dz, density);
	}

	private IBlockState getBlock(int x, int y, int z, double dx, double dy, double dz, double density) {
		IBlockState block = Blocks.AIR.getDefaultState();
		int size = replacers.size();
		for (int i = 0; i < size; i++) {
			block = replacers.get(i).getReplacedBlock(block, x, y, z, dx, dy, dz, density);
		}
		return block;
	}
	
	public void resetBiomeReplacers(BlockPos pos) {
		replacers = biomeSource.getReplacers(pos.getX(), pos.getY(), pos.getZ());
	}
}
