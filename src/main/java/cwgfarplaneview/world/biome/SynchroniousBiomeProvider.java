package cwgfarplaneview.world.biome;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

public class SynchroniousBiomeProvider extends BiomeProvider {

	private BiomeProvider wrapped;
	private Object lock1;
	private Object lock2;
	private Object lock3;
	private Object lock4;
	private Object lock5;

	SynchroniousBiomeProvider(BiomeProvider wrappedIn) {
		wrapped = wrappedIn;
	}

	@Override
	public Biome getBiome(BlockPos pos, Biome defaultBiome) {
		synchronized (lock1) {
			return wrapped.getBiome(pos, defaultBiome);
		}
	}

	@Override
	public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
		synchronized (lock2) {
			return wrapped.getBiomesForGeneration(biomes, x, z, width, height);
		}
	}

	@Override
	public Biome[] getBiomes(@Nullable Biome[] biomes, int x, int z, int width, int height, boolean cacheFlag) {
		synchronized (lock3) {
			return wrapped.getBiomes(biomes, x, z, width, height, cacheFlag);
		}
	}

	@Override
	public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
		synchronized (lock4) {
			return wrapped.areBiomesViable(x, z, radius, allowed);
		}
	}

	public void cleanupCache() {
		synchronized (lock5) {
			wrapped.cleanupCache();
		}
	}
}
