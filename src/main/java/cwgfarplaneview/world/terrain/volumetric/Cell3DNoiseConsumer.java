package cwgfarplaneview.world.terrain.volumetric;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToLocal;

import java.util.List;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseConsumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class Cell3DNoiseConsumer implements NoiseConsumer {

	private final BiomeSource biomeSource;
	public IBlockState blockState;

	Cell3DNoiseConsumer(BiomeSource biomeSourceIn) {
		biomeSource = biomeSourceIn;
	}

	@Override
	public void accept(int x, int y, int z, double dx, double dy, double dz, double density) {
		int lx = blockToLocal(x);
		int ly = blockToLocal(y);
		int lz = blockToLocal(z);
		if (lx != 0 && ly != 0 && lz != 0)
			return;
		blockState = this.getBlock(x, y, z, dx, dy, dz, density);
	}

	private IBlockState getBlock(int x, int y, int z, double dx, double dy, double dz, double density) {
		List<IBiomeBlockReplacer> replacers = biomeSource.getReplacers(x, y, z);
		IBlockState block = Blocks.AIR.getDefaultState();
		int size = replacers.size();
		for (int i = 0; i < size; i++) {
			block = replacers.get(i).getReplacedBlock(block, x, y, z, dx, dy, dz, density);
		}
		return block;
	}
}
