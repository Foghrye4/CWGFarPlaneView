package cwgfarplaneview.world.biome;

import net.minecraft.world.gen.layer.GenLayer;

public class SeedProducerGenLayer extends GenLayer {

	public SeedProducerGenLayer(long worldSeed) {
		super(1L);
		this.initWorldGenSeed(worldSeed);
	}

	@Override
	public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
		return null;
	}
	
	public int getChunkSpecificRandom(int areaX, int areaY) {
		this.initChunkSeed(areaX, areaY);
		return this.nextInt(Integer.MAX_VALUE);
	}

}
