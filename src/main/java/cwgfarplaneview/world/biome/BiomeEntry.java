package cwgfarplaneview.world.biome;

import io.github.opencubicchunks.cubicchunks.api.util.XZAddressable;
import net.minecraft.world.biome.Biome;

public class BiomeEntry implements XZAddressable {

	public final Biome biome;
	private final int x;
	private final int z;

	public BiomeEntry(Biome biomeIn, int xIn, int zIn) {
		biome = biomeIn;
		x = xIn;
		z = zIn;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getZ() {
		return z;
	}
}
