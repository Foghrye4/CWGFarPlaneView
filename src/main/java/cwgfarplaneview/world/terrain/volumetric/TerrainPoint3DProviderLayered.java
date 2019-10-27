package cwgfarplaneview.world.terrain.volumetric;

import net.minecraft.world.WorldServer;

public abstract class TerrainPoint3DProviderLayered extends TerrainPoint3DProvider {
	
	public TerrainPoint3DProviderLayered(WorldServer worldIn) {
		super(worldIn);
	}

	abstract TerrainPoint3DProvider getGenerator(int cubeX, int cubeY, int cubeZ);
}
