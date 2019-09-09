package cwgfarplaneview.client.block_color;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BlockColor {
	public static final BlockColor MISSING_BLOCK_COLOR = new BlockColor(0,0,0);
	protected static final int SNOW_COLOR = 0xf0fbfb;
	protected final int topSideBlockColor;
	protected final int horizontalSidesBlockColor;
	protected final int bottomSideBlockColor;
	
	public BlockColor(int topSideBlockColorIn,int horizontalSidesBlockColorIn,int bottomSideBlockColorIn) {
		topSideBlockColor = topSideBlockColorIn;
		horizontalSidesBlockColor = horizontalSidesBlockColorIn;
		bottomSideBlockColor = bottomSideBlockColorIn;
	}
	
	public int getColor(IBlockState state, Biome biome, BlockPos pos, float normalY) {
		int topSideColor = getTopColor(biome, pos);
		int horizontalSideColor = getSideColor(biome, pos);
		if(normalY>=1.0f) {
			return topSideColor;
		}
		else if(normalY<=-1.0f) {
			return bottomSideBlockColor;
		}
		else if (normalY >= 0.0f) {
			int sideRed = getRed(horizontalSideColor);
			int sideGreen = getGreen(horizontalSideColor);
			int sideBlue = getBlue(horizontalSideColor);
			int red = (int)((getRed(topSideColor) - sideRed) * normalY) + sideRed;
			int green = (int)((getGreen(topSideColor) - sideGreen) * normalY) + sideGreen;
			int blue = (int)((getBlue(topSideColor) - sideBlue) * normalY) + sideBlue;
			return red << 16 | (green & 255) << 8 | blue;
		} else {
			int sideRed = getRed(horizontalSideColor);
			int sideGreen = getGreen(horizontalSideColor);
			int sideBlue = getBlue(horizontalSideColor);
			int red = (int)((sideRed - getRed(bottomSideBlockColor)) * normalY) + sideRed;
			int green = (int)((sideGreen - getGreen(bottomSideBlockColor)) * normalY) + sideGreen;
			int blue = (int)((sideBlue - getBlue(bottomSideBlockColor)) * normalY) + sideBlue;
			return red << 16 | green << 8 | blue;
		}
	}
	
	protected int getSideColor(Biome biome, BlockPos pos) {
		return horizontalSidesBlockColor;
	}

	protected int getTopColor(Biome biome, BlockPos pos) {
		if (biome.isSnowyBiome() || biome.getTemperature(pos) < 0.15f)
			return SNOW_COLOR;
		return topSideBlockColor;
	}
	
	protected int getRed(int color) {
		return color >> 16 & 255;
	}

	protected int getGreen(int color) {
		return color >> 8 & 255;
	}

	protected int getBlue(int color) {
		return color & 255;
	}
}
