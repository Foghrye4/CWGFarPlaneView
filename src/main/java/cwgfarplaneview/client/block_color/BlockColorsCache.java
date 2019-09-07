package cwgfarplaneview.client.block_color;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class BlockColorsCache {

	public final Map<IBlockState, BlockColor> map = new HashMap<IBlockState, BlockColor>();
	int blockTextureWidth;
	int blockTextureHeight;
	byte[] pixels;
	
	public void loadBlockTextureMapToRAM() {
		Minecraft mc = Minecraft.getMinecraft();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		blockTextureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		blockTextureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		pixels = new byte[blockTextureWidth * blockTextureHeight * 4];
		ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
		GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		buffer.get(pixels);
	}

	private BlockColor createBlockColor(IBlockState state) {
		Minecraft mc = Minecraft.getMinecraft();
		IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
		if (model == null) {
			logger.debug(String.format("No model for state %s", state));
			return BlockColor.MISSING_BLOCK_COLOR;
		}
		if(!model.getClass().getName().startsWith("net.minecraft.") && !model.getClass().getName().startsWith("net.minecraftforge.")) {
			logger.debug(String.format("Model class of %s is instance of %s", state, model.getClass().getName()));
			return BlockColor.MISSING_BLOCK_COLOR;
		}
		List<BakedQuad> topQuads = new ArrayList<BakedQuad>();
		List<BakedQuad> horizontalQuads = new ArrayList<BakedQuad>();
		List<BakedQuad> bottomQuads = new ArrayList<BakedQuad>();
		List<BakedQuad> unspecifiedQuads = new ArrayList<BakedQuad>();
		for (EnumFacing enumfacing : EnumFacing.values()) {
			if(enumfacing.equals(EnumFacing.UP))
				topQuads.addAll(model.getQuads(state, enumfacing, 0));
			else if(enumfacing.equals(EnumFacing.DOWN))
				bottomQuads.addAll(model.getQuads(state, enumfacing, 0));
			else
				horizontalQuads.addAll(model.getQuads(state, enumfacing, 0));
		}
		unspecifiedQuads.addAll(model.getQuads(state, null, 0));
		int unpecifiedSideColor = getAverageColorOfQuads(unspecifiedQuads);
		
		int averageTopColor = getAverageColorOfQuads(topQuads);
		if(topQuads.isEmpty())
			averageTopColor = unpecifiedSideColor;
		
		int averageBottomColor = getAverageColorOfQuads(bottomQuads);
		if(bottomQuads.isEmpty())
			averageBottomColor = unpecifiedSideColor;
		
		int averageHorizontalSideColor = getAverageColorOfQuads(horizontalQuads);
		if(horizontalQuads.isEmpty())
			averageHorizontalSideColor = unpecifiedSideColor;
		if (state.getBlock() == Blocks.GRASS)
			return new GrassBlockColor(averageTopColor, averageHorizontalSideColor, averageBottomColor);
		return new BlockColor(averageTopColor, averageHorizontalSideColor, averageBottomColor);
	}
	
	private int getAverageColorOfQuads(List<BakedQuad> quads) {
		int rSum = 0;
		int gSum = 0;
		int bSum = 0;
		int alphaSum = 0;
		for (BakedQuad quad : quads) {
			TextureAtlasSprite sprite = quad.getSprite();
			int u0 = (int) (blockTextureWidth * sprite.getMinU());
			int u1 = (int) (blockTextureWidth * sprite.getMaxU());
			int v0 = (int) (blockTextureHeight * sprite.getMinV());
			int v1 = (int) (blockTextureHeight * sprite.getMaxV());
			for (int u = u0; u < u1; u++) {
				for (int v = v0; v < v1; v++) {
					int index = (u + v * blockTextureWidth) * 4;
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
	
	public int getBlockColor(IBlockState state, Biome biome, BlockPos pos, float normalY) {
		BlockColor color = map.get(state);
		if(color==null) {
			color = this.createBlockColor(state);
			map.put(state, color);
		}
		return color.getColor(state, biome, pos, normalY);
	}
	

}
