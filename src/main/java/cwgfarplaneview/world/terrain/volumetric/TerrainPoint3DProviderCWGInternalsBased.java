package cwgfarplaneview.world.terrain.volumetric;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.ToIntFunction;

import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomCubicWorldType;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.BiomeSource;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.IBuilder;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.builder.NoiseSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

public class TerrainPoint3DProviderCWGInternalsBased extends TerrainPoint3DProvider {
	private static final int CACHE_SIZE_2D = 16 * 16;
	private static final int CACHE_SIZE_3D = 16 * 16 * 16;
	private static final ToIntFunction<Vec3i> HASH_2D = (v) -> v.getX() + v.getZ() * 5;
	private static final ToIntFunction<Vec3i> HASH_3D = (v) -> v.getX() + v.getZ() * 5 + v.getY() * 25;

	private final Map<CustomGeneratorSettings.IntAABB, TerrainPoint3DProviderCWGInternalsBased> areaGenerators = new HashMap<>();
	private IBuilder terrainBuilder;
	private final CustomGeneratorSettings conf;
	private final BiomeSource biomeSource;
	private final Cell3DNoiseConsumer noiseConsumer;
	private final BlockPos.MutableBlockPos start = new BlockPos.MutableBlockPos();
	private final BlockPos.MutableBlockPos end = new BlockPos.MutableBlockPos();
	private final static Vec3i SCALE = new Vec3i(1, 1, 1);


	public TerrainPoint3DProviderCWGInternalsBased(WorldServer worldIn, BiomeProvider biomeProvider,
			CustomGeneratorSettings settings, final long seed) {
		super(worldIn);
		this.conf = settings;
		this.biomeSource = new BiomeSource(world, conf.createBiomeBlockReplacerConfig(), biomeProvider, 2);
		this.noiseConsumer = new Cell3DNoiseConsumer(biomeSource);
		initGenerator(seed);

		if (settings.cubeAreas != null) {
			for (CustomGeneratorSettings.IntAABB aabb : settings.cubeAreas.keySet()) {
				CustomGeneratorSettings subSettings = settings.cubeAreas.get(aabb);
				this.areaGenerators.put(aabb, new TerrainPoint3DProviderCWGInternalsBased(world,
						CustomCubicWorldType.makeBiomeProvider(world, subSettings), subSettings, seed));
			}
		}
	}

	private void initGenerator(long seed) {
		Random rnd = new Random(seed);

		IBuilder selector = NoiseSource.perlin().seed(rnd.nextLong()).normalizeTo(-1, 1)
				.frequency(conf.selectorNoiseFrequencyX, conf.selectorNoiseFrequencyY, conf.selectorNoiseFrequencyZ)
				.octaves(conf.selectorNoiseOctaves).create().mul(conf.selectorNoiseFactor).add(conf.selectorNoiseOffset)
				.clamp(0, 1);

		IBuilder low = NoiseSource.perlin().seed(rnd.nextLong()).normalizeTo(-1, 1)
				.frequency(conf.lowNoiseFrequencyX, conf.lowNoiseFrequencyY, conf.lowNoiseFrequencyZ)
				.octaves(conf.lowNoiseOctaves).create().mul(conf.lowNoiseFactor).add(conf.lowNoiseOffset);

		IBuilder high = NoiseSource.perlin().seed(rnd.nextLong()).normalizeTo(-1, 1)
				.frequency(conf.highNoiseFrequencyX, conf.highNoiseFrequencyY, conf.highNoiseFrequencyZ)
				.octaves(conf.highNoiseOctaves).create().mul(conf.highNoiseFactor).add(conf.highNoiseOffset);

		IBuilder randomHeight2d = NoiseSource.perlin().seed(rnd.nextLong()).normalizeTo(-1, 1)
				.frequency(conf.depthNoiseFrequencyX, 0, conf.depthNoiseFrequencyZ).octaves(conf.depthNoiseOctaves)
				.create().mul(conf.depthNoiseFactor).add(conf.depthNoiseOffset).mulIf(IBuilder.NEGATIVE, -0.3).mul(3)
				.sub(2).clamp(-2, 1).divIf(IBuilder.NEGATIVE, 2 * 2 * 1.4).divIf(IBuilder.POSITIVE, 8)
				.mul(0.2 * 17 / 64.0).cached2d(CACHE_SIZE_2D, HASH_2D);

		IBuilder height = ((IBuilder) biomeSource::getHeight).mul(conf.heightFactor).add(conf.heightOffset);

		double specialVariationFactor = conf.specialHeightVariationFactorBelowAverageY;
		IBuilder volatility = ((IBuilder) biomeSource::getVolatility)
				.mul((x, y, z) -> height.get(x, y, z) > y ? specialVariationFactor : 1).mul(conf.heightVariationFactor)
				.add(conf.heightVariationOffset);

		this.terrainBuilder = selector.lerp(low, high).add(randomHeight2d).mul(volatility).add(height)
				.sub(volatility.signum().mul((x, y, z) -> y)).cached(CACHE_SIZE_3D, HASH_3D);
	}

	@Override
	public TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException {
		int cubeX = meshX << TerrainConfig.meshSizeBitChunks;
		int cubeZ = meshZ << TerrainConfig.meshSizeBitChunks;
		int cubeY = meshY << TerrainConfig.meshSizeBitChunks;
		for (Entry<IntAABB, TerrainPoint3DProviderCWGInternalsBased> entry : this.areaGenerators.entrySet()) {
			if (entry.getKey().contains(cubeX, cubeY, cubeZ)) {
				return entry.getValue().getTerrainPointAt(meshX, meshY, meshZ);
			}
		}
		return getPointOf(meshX, meshY, meshZ);
	}

	
	@Override
	protected void reset(int cubeX, int cubeY, int cubeZ) {
		end.setPos(cubeX * 16 + 7, cubeY * 16 + 7, cubeZ * 16 + 7);
		noiseConsumer.resetBiomeReplacers(end);
	}
	
	@Override
	protected IBlockState getBlockStateAt(int x,int y, int z) {
		start.setPos(x, y, z);
		end.setPos(x+1, y+1, z+1);
		terrainBuilder.forEachScaled(start, end, SCALE, noiseConsumer);
		return noiseConsumer.blockState;
	}
}
