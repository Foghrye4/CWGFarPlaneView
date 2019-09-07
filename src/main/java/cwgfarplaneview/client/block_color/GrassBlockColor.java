package cwgfarplaneview.client.block_color;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class GrassBlockColor extends BlockColor {

	public GrassBlockColor(int topSideBlockColorIn, int horizontalSidesBlockColorIn, int bottomSideBlockColorIn) {
		super(topSideBlockColorIn, horizontalSidesBlockColorIn, bottomSideBlockColorIn);
	}

	@Override
	protected int getTopColor(Biome biome, BlockPos pos) {
		if (biome.isSnowyBiome() || biome.getTemperature(pos) < 0.15f)
			return SNOW_COLOR;
		return multiplyColors(topSideBlockColor,biome.getGrassColorAtPos(pos));
	}

	private int multiplyColors(int color1, int color2) {
		int red1 = color1 >>> 16;
		int green1 = color1 >>> 8 & 0xFF;
		int blue1 = color1 & 0xFF;
		int red2 = color2 >>> 16;
		int green2 = color2 >>> 8 & 0xFF;
		int blue2 = color2 & 0xFF;
		int red = red1 * red2 / 255;
		int green = green1 * green2 / 255;
		int blue = blue1 * blue2 / 255;
		return red << 16 | green << 8 | blue;
	}
}
