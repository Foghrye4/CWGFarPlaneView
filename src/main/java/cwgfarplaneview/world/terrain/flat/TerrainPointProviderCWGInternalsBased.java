package cwgfarplaneview.world.terrain.flat;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.ToIntFunction;

import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

public class TerrainPointProviderCWGInternalsBased implements TerrainPointProvider {
    private static final int CACHE_SIZE_2D = 16 * 16;
    private static final int CACHE_SIZE_3D = 16 * 16 * 16;
    private static final ToIntFunction<Vec3i> HASH_2D = (v) -> v.getX() + v.getZ() * 5;
    private static final ToIntFunction<Vec3i> HASH_3D = (v) -> v.getX() + v.getZ() * 5 + v.getY() * 25;
    
    private final Map<CustomGeneratorSettings.IntAABB, TerrainPointProviderCWGInternalsBased> areaGenerators = new HashMap<>();
    private IBuilder terrainBuilder;
    private final CustomGeneratorSettings conf;
    private final BiomeSource biomeSource;
    private final SurfaceNoiseConsumer noiseConsumer;
	private final World world;
	private int cubeHeightHint = 4;
    
    public TerrainPointProviderCWGInternalsBased(World worldIn, BiomeProvider biomeProvider, CustomGeneratorSettings settings, final long seed) {
        this.conf = settings;
        this.world = worldIn;
        this.biomeSource = new BiomeSource(world, conf.createBiomeBlockReplacerConfig(), biomeProvider, 2);
        this.noiseConsumer = new SurfaceNoiseConsumer(biomeSource);
        initGenerator(seed);

        if (settings.cubeAreas != null) {
            for (CustomGeneratorSettings.IntAABB aabb : settings.cubeAreas.keySet()) {
                this.areaGenerators.put(aabb, new TerrainPointProviderCWGInternalsBased(world, CustomCubicWorldType.makeBiomeProvider(world, settings), settings.cubeAreas.get(aabb), seed));
            }
        }
    }
	
	 private void initGenerator(long seed) {
	        Random rnd = new Random(seed);

	        IBuilder selector = NoiseSource.perlin()
	                .seed(rnd.nextLong())
	                .normalizeTo(-1, 1)
	                .frequency(conf.selectorNoiseFrequencyX, conf.selectorNoiseFrequencyY, conf.selectorNoiseFrequencyZ)
	                .octaves(conf.selectorNoiseOctaves)
	                .create()
	                .mul(conf.selectorNoiseFactor).add(conf.selectorNoiseOffset).clamp(0, 1);

	        IBuilder low = NoiseSource.perlin()
	                .seed(rnd.nextLong())
	                .normalizeTo(-1, 1)
	                .frequency(conf.lowNoiseFrequencyX, conf.lowNoiseFrequencyY, conf.lowNoiseFrequencyZ)
	                .octaves(conf.lowNoiseOctaves)
	                .create()
	                .mul(conf.lowNoiseFactor).add(conf.lowNoiseOffset);

	        IBuilder high = NoiseSource.perlin()
	                .seed(rnd.nextLong())
	                .normalizeTo(-1, 1)
	                .frequency(conf.highNoiseFrequencyX, conf.highNoiseFrequencyY, conf.highNoiseFrequencyZ)
	                .octaves(conf.highNoiseOctaves)
	                .create()
	                .mul(conf.highNoiseFactor).add(conf.highNoiseOffset);

	        IBuilder randomHeight2d = NoiseSource.perlin()
	                .seed(rnd.nextLong())
	                .normalizeTo(-1, 1)
	                .frequency(conf.depthNoiseFrequencyX, 0, conf.depthNoiseFrequencyZ)
	                .octaves(conf.depthNoiseOctaves)
	                .create()
	                .mul(conf.depthNoiseFactor).add(conf.depthNoiseOffset)
	                .mulIf(IBuilder.NEGATIVE, -0.3).mul(3).sub(2).clamp(-2, 1)
	                .divIf(IBuilder.NEGATIVE, 2 * 2 * 1.4).divIf(IBuilder.POSITIVE, 8)
	                .mul(0.2 * 17 / 64.0)
	                .cached2d(CACHE_SIZE_2D, HASH_2D);

	        IBuilder height = ((IBuilder) biomeSource::getHeight)
	                .mul(conf.heightFactor)
	                .add(conf.heightOffset);

	        double specialVariationFactor = conf.specialHeightVariationFactorBelowAverageY;
	        IBuilder volatility = ((IBuilder) biomeSource::getVolatility)
	                .mul((x, y, z) -> height.get(x, y, z) > y ? specialVariationFactor : 1)
	                .mul(conf.heightVariationFactor)
	                .add(conf.heightVariationOffset);

	        this.terrainBuilder = selector
	                .lerp(low, high).add(randomHeight2d).mul(volatility).add(height)
	                .sub(volatility.signum().mul((x, y, z) -> y))
	                .cached(CACHE_SIZE_3D, HASH_3D);
	    }
	 
	@Override
	public TerrainPoint getTerrainPointAt(int meshX, int meshZ) throws IncorrectTerrainDataException {
		int cubeX = meshX << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int cubeZ = meshZ << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		while (!noiseConsumer.surfaceDetected) {
			int cubeY = cubeHeightHint;
			BlockPos start = new BlockPos(cubeX * 16, cubeY * 2, cubeZ * 16);
			BlockPos end = start.add(1, 2, 1);
			boolean runInSubLayer = false;
			for (Entry<IntAABB, TerrainPointProviderCWGInternalsBased> entry : this.areaGenerators.entrySet()) {
				if (entry.getKey().contains(cubeX, cubeY, cubeZ)) {
					entry.getValue().terrainBuilder.forEachScaled(start, end, new Vec3i(1, 8, 1), noiseConsumer);
					runInSubLayer = true;
					break;
				}
			}
			if (!runInSubLayer)
				terrainBuilder.forEachScaled(start, end, new Vec3i(1, 8, 1), noiseConsumer);
			if (noiseConsumer.isSurface)
				cubeHeightHint++;
			else
				cubeHeightHint--;
		}
		noiseConsumer.reset();
		return new TerrainPoint(meshX, meshZ, noiseConsumer.surfaceHeight, noiseConsumer.blockState,
				getBiomeAt(cubeX, cubeZ));
	}
	
	private Biome getBiomeAt(int x, int z) {
		Biome[] biomes = new Biome[256];
		world.getBiomeProvider().getBiomes(biomes, x << 4, z << 4, 16, 16, false);
		return biomes[0];
	}

}
