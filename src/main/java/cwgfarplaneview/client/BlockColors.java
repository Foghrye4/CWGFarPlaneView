package cwgfarplaneview.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.registries.GameData;

import static cwgfarplaneview.CWGFarPlaneViewMod.*;

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
		logger.debug(String.format("Allocated %d x %d pixel buffer", width, height));
		for (IBlockState state : GameData.getBlockStateIDMap()) {
			int color = this.getBlockColor(state, width, height, pixels);
			map.put(state, color);
		}
	}

	private int getBlockColor(IBlockState state, int width, int height, byte[] pixels) {
		Minecraft mc = Minecraft.getMinecraft();
		IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
		if (model == null)
			return 0;
		if(!(model instanceof MultipartBakedModel) 
				&&!(model instanceof WeightedBakedModel) 
				&&!(model instanceof SimpleBakedModel))
			return 0;
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
		if (alphaSum == 0)
			return 0;
		rSum = rSum * 255 / alphaSum;
		gSum = gSum * 255 / alphaSum;
		bSum = bSum * 255 / alphaSum;
		return rSum << 16 | gSum << 8 | bSum;
	}
}
