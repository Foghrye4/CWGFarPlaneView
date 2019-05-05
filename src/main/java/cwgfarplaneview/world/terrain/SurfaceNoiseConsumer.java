package cwgfarplaneview.world.terrain;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.blockToLocal;

import java.util.List;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.IBiomeBlockReplacer;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseConsumer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class SurfaceNoiseConsumer implements NoiseConsumer {

	public boolean surfaceDetected = false;
	public boolean isSurface = false;
	private final BiomeSource biomeSource;
	public IBlockState blockState;
	public int surfaceHeight;

	SurfaceNoiseConsumer(BiomeSource biomeSourceIn) {
		biomeSource = biomeSourceIn;
	}

	@Override
	public void accept(int x, int y, int z, double dx, double dy, double dz, double density) {
		if (surfaceDetected)
			return;
		IBlockState blockState1 = this.getBlock(x, y, z, dx, dy, dz, density);
		int lx = blockToLocal(x);
		int lz = blockToLocal(z);
		if (lx != 0 && lz != 0)
			return;
		if (isSurface && isAirOrWater(blockState1)) {
			surfaceDetected = true;
			return;
		}
		surfaceHeight = y;
		isSurface = !isAirOrWater(blockState1);
		blockState = blockState1;
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

	boolean isAirOrWater(IBlockState state) {
		return state == Blocks.AIR.getDefaultState() || state.getMaterial() == Material.WATER;
	}

	public void reset() {
		surfaceDetected = false;
		isSurface = false;
	}
}
