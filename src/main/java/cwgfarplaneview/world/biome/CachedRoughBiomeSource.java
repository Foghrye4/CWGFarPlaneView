package cwgfarplaneview.world.biome;

import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.BiomeBlockReplacerConfig;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeProvider;

public class CachedRoughBiomeSource extends BiomeSource {
	
	private final Int2DoubleMap heightCache = new Int2DoubleOpenHashMap();
	private final Int2DoubleMap heightVariationCache = new Int2DoubleOpenHashMap();
	private final SeedProducerGenLayer seedProducer;
	
	public CachedRoughBiomeSource(World world, BiomeBlockReplacerConfig conf, BiomeProvider biomeGen, int smoothRadius) {
		super(world, conf, biomeGen, smoothRadius);
		seedProducer = new SeedProducerGenLayer(world.getSeed());
	}
	
	public double getHeight(int x, int y, int z) {
		int seed = seedProducer.getChunkSpecificRandom(x & 0xFFFFFFF0, z & 0xFFFFFFF0);
		if (heightCache.containsKey(seed)) {
			return heightCache.get(seed);
		} else {
			double height = super.getHeight(x, y, z);
			heightCache.put(seed, height);
			return height;
		}
	}

    public double getVolatility(int x, int y, int z) {
		int seed = seedProducer.getChunkSpecificRandom(x & 0xFFFFFFF0, z & 0xFFFFFFF0);
		if (heightVariationCache.containsKey(seed)) {
			return heightVariationCache.get(seed);
		} else {
			double heightVariation = super.getHeight(x, y, z);
			heightVariationCache.put(seed, heightVariation);
			return heightVariation;
		}
    }
}
