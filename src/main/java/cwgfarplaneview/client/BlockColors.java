package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.GameData;

public class BlockColors {

	public final Object2IntMap<IBlockState> map = new Object2IntOpenHashMap<IBlockState>();

	public void loadColors() {
		Minecraft mc = Minecraft.getMinecraft();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		byte[] pixels = new byte[width * height * 4];
		ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		buffer.get(pixels);
		for (IBlockState state : GameData.getBlockStateIDMap()) {
			int color = this.getBlockColor(state, width, height, pixels);
			map.put(state, color);
			logger.debug(String.format("Putting color %d for state %s", color, state));
		}
	}

	private int getBlockColor(IBlockState state, int width, int height, byte[] pixels) {
		Minecraft mc = Minecraft.getMinecraft();
		IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
		if (model == null) {
			logger.debug(String.format("No model for state %s", state));
			return 0;
		}
		if(!model.getClass().getName().startsWith("net.minecraft.") && !model.getClass().getName().startsWith("net.minecraftforge.")) {
			logger.debug(String.format("Model class of %s is instance of %s", state, model.getClass().getName()));
			return 0;
		}
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		for (EnumFacing enumfacing : EnumFacing.values()) {
			quads.addAll(model.getQuads(state, enumfacing, 0));
		}
		quads.addAll(model.getQuads(state, null, 0));
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int rSum = 0;
		int gSum = 0;
		int bSum = 0;
		int alphaSum = 0;
		for (BakedQuad quad : quads) {
			TextureAtlasSprite sprite = quad.getSprite();
			int u0 = (int) (width * sprite.getMinU());
			int u1 = (int) (width * sprite.getMaxU());
			int v0 = (int) (height * sprite.getMinV());
			int v1 = (int) (height * sprite.getMaxV());
			for (int u = u0; u < u1; u++) {
				for (int v = v0; v < v1; v++) {
					int index = (u + v * width) * 4;
					int r = pixels[index] & 0xFF;
					int g = pixels[index + 1] & 0xFF;
					int b = pixels[index + 2] & 0xFF;
					int a = pixels[index + 3] & 0xFF;
					r = r * a / 255;
					g = g * a / 255;
					b = b * a / 255;
					rSum += r;
					gSum += g;
					bSum += b;
					alphaSum += a;
				}
			}
		}
		if (alphaSum == 0) {
			return 0;
		}
		rSum = rSum * 255 / alphaSum;
		gSum = gSum * 255 / alphaSum;
		bSum = bSum * 255 / alphaSum;
		return rSum << 16 | gSum << 8 | bSum;
	}
	
	public int getBlockColor(IBlockState state, Biome biome, BlockPos pos) {
		Block block = state.getBlock();
		if (biome.isSnowyBiome() || biome.getTemperature(pos) < 0.15f)
			return 0xf0fbfb;
		if (block == Blocks.GRASS)
			return multiplyColors(0x979797, biome.getGrassColorAtPos(pos));
		if (block == Blocks.STONE)
			return 0x7d7d7d;
		if (block == Blocks.CLAY)
			return 0x9fa4b1;
		if (block == Blocks.DIRT)
			return 0x866043;
		if (block == Blocks.HARDENED_CLAY) {
			return 0x975d43;
		}
		if (block == Blocks.STAINED_HARDENED_CLAY) {
			switch (state.getValue(BlockStainedHardenedClay.COLOR)) {
			case BLACK:
				return 0x251710;
			case BLUE:
				return 0x4a3c5b;
			case BROWN:
				return 0x4d3324;
			case CYAN:
				return 0x575b5b;
			case GRAY:
				return 0x3a2a24;
			case GREEN:
				return 0x4c532a;
			case LIGHT_BLUE:
				return 0x716c8a;
			case LIME:
				return 0x677535;
			case MAGENTA:
				return 0x96586d;
			case ORANGE:
				return 0xa25426;
			case PINK:
				return 0xa24e4f;
			case PURPLE:
				return 0x764656;
			case RED:
				return 0x8f3d2f;
			case SILVER:
				return 0x876b61;
			case WHITE:
				return 0xd2b2a1;
			case YELLOW:
				return 0xba8523;
			default:
				return 0x975d43;
			}
		}
		if (block == Blocks.ICE)
			return 0x7dadff;
		if (block == Blocks.FROSTED_ICE)
			return 0x7dadff;
		if (block == Blocks.PACKED_ICE)
			return 0xa5c3f5;
		if (block == Blocks.OBSIDIAN)
			return 0x14121e;
		if (block == Blocks.SAND) {
			if (state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)
				return 0xa95821;
			return 0xdbd3a0;
		}
		if (block == Blocks.SNOW)
			return 0xf0fbfb;
		if(!map.containsKey(state))
			logger.warn("Unknow blockstate recieved:" + state);
		return map.getInt(state);
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
