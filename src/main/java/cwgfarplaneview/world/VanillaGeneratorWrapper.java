package cwgfarplaneview.world;

import java.util.List;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;

public class VanillaGeneratorWrapper implements ICubeGenerator {

	private final IChunkGenerator vanillaChunkGenerator;
	private int worldHeightBlocks;
	private int worldHeightCubes;
	private Chunk lastChunk;

	public VanillaGeneratorWrapper(ICubicWorld world, IChunkGenerator vanillaChunkGeneratorIn) {
		vanillaChunkGenerator = vanillaChunkGeneratorIn;
        worldHeightBlocks = world.getMaxGenerationHeight();
        worldHeightCubes = worldHeightBlocks / ICube.SIZE;
        lastChunk = vanillaChunkGenerator.generateChunk(0, 0);
	}

	@Override
	public void generateColumn(Chunk chunkIn) {}

	@Override
	public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
		CubePrimer primer = new CubePrimer();
        if (lastChunk.x != cubeX || lastChunk.z != cubeZ) {
    		lastChunk = vanillaChunkGenerator.generateChunk(cubeX, cubeZ);
        }
        if (cubeY < 0) {
        	return primer;
        }
        if (cubeY >= worldHeightCubes) {
        	return primer;
        }
		ExtendedBlockStorage storage = lastChunk.getBlockStorageArray()[cubeY];
        if (storage != null && !storage.isEmpty()) {
            for (int x = 0; x < ICube.SIZE; x++) {
                for (int y = 0; y < ICube.SIZE; y++) {
                    for (int z = 0; z < ICube.SIZE; z++) {
                        IBlockState state = storage.get(x, y, z);
                        primer.setBlockState(x, y, z, state);
                    }
                }
            }
        }
		return primer;
	}

	@Override
	public BlockPos getClosestStructure(String arg0, BlockPos arg1, boolean arg2) {
		return null;
	}

	@Override
	public Box getFullPopulationRequirements(ICube arg0) {
		return null;
	}

	@Override
	public Box getPopulationPregenerationRequirements(ICube arg0) {
		return null;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType arg0, BlockPos arg1) {
		return null;
	}

	@Override
	public void populate(ICube arg0) {	}

	@Override
	public void recreateStructures(ICube arg0) {
	}

	@Override
	public void recreateStructures(Chunk arg0) {
	}
}
