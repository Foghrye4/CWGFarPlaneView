package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import cwgfarplaneview.world.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientTerrainRenderer extends IRenderHandler implements IBlockAccess {
	private static final ResourceLocation TERRAIN_TEXTURE = new ResourceLocation(MODID,
			"textures/terrain/white_noise.png");
	private static final int HORIZONT_DISTANCE = 64;
	private static final int HORIZONT_DISTANCE_BLOCKS = HORIZONT_DISTANCE << 4;
	private static final int HORIZONT_DISTANCE_SQ = HORIZONT_DISTANCE * HORIZONT_DISTANCE;
	private static final float CLOSE_PLANE = 16.0f;
	private static final float FAR_PLANE = HORIZONT_DISTANCE_BLOCKS * MathHelper.SQRT_2;

	private XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 8000);
	int minimalX = -4;
	int minimalZ = -4;
	int maximalX = 4;
	int maximalZ = 4;
	private float fov = 70.0f;
	private int seaLevel = 64;
	private boolean needUpdate = true;
	private int terrainDisplayList = -1;
	private int seaDisplayList = -1;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		Project.gluPerspective(fov, (float) mc.displayWidth / (float) mc.displayHeight, CLOSE_PLANE, FAR_PLANE);
		GlStateManager.matrixMode(5888);
		EntityPlayerSP player = mc.player;
		float renderPosX = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
		float renderPosY = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
		float renderPosZ = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glPushMatrix();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		mc.entityRenderer.enableLightmap();
		mc.getTextureManager().bindTexture(TERRAIN_TEXTURE);
		if (this.seaDisplayList == -1) {
			this.compileSeaDisplayList();
		}
		GL11.glTranslatef(0.0f, -renderPosY, 0.0f);
		GL11.glCallList(this.seaDisplayList);
		GL11.glTranslatef(-renderPosX, 0.5f, -renderPosZ);
		if (this.needUpdate)
			compileDisplayList(world);
		GL11.glCallList(this.terrainDisplayList);
		GL11.glPopMatrix();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void compileSeaDisplayList() {
		this.seaDisplayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(this.seaDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		worldRendererIn.pos(HORIZONT_DISTANCE_BLOCKS, seaLevel, HORIZONT_DISTANCE_BLOCKS).tex(0.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(HORIZONT_DISTANCE_BLOCKS, seaLevel, -HORIZONT_DISTANCE_BLOCKS).tex(1.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-HORIZONT_DISTANCE_BLOCKS, seaLevel, -HORIZONT_DISTANCE_BLOCKS).tex(1.0f, 1.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-HORIZONT_DISTANCE_BLOCKS, seaLevel, HORIZONT_DISTANCE_BLOCKS).tex(0.0f, 1.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		tessellator.draw();
		GL11.glEndList();
	}

	private void compileDisplayList(WorldClient world) {
		if (this.terrainDisplayList == -1) {
			this.terrainDisplayList = GLAllocation.generateDisplayLists(1);
		}
		GL11.glNewList(this.terrainDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		for (int x = minimalX; x <= maximalX; x++) {
			for (int z = minimalZ; z <= maximalZ; z++) {
				this.addQuad(worldRendererIn, world, x, z);
			}
		}
		tessellator.draw();
		GL11.glEndList();
		this.needUpdate = false;
	}

	private void addQuad(BufferBuilder worldRendererIn, WorldClient world, int x, int z) {
		this.addVector(worldRendererIn, world, x, z, 0.0f, 0.0f);
		this.addVector(worldRendererIn, world, x, z + 1, 1.0f, 0.0f);
		this.addVector(worldRendererIn, world, x + 1, z + 1, 1.0f, 1.0f);
		this.addVector(worldRendererIn, world, x + 1, z, 0.0f, 1.0f);
	}

	private void addVector(BufferBuilder worldRendererIn, WorldClient world, int x, int z, float u, float v) {
		int bx = x << 4;
		int bz = z << 4;
		int height = 0;
		int color = 0x00FF00;
		float red = 0.0f;
		float green = 0.5f;
		float blue = 0.0f;
		TerrainPoint point = terrainMap.get(x, z);
		int skyLight = 240;
		int blockLight = 0;
		if (point != null) {
			height = point.blockY;
			BlockPos pos = new BlockPos(bx, height, bz);
			color = this.getBlockColor(point.blockState, point.biome, pos);
			red = (color >> 16 & 255) / 256f;
			green = (color >> 8 & 255) / 256f;
			blue = (color & 255) / 256f;
		}
		worldRendererIn.pos(bx, height, bz).tex(u, v).lightmap(skyLight, blockLight).color(red, green, blue, 1.0f)
				.endVertex();
	}

	private int getBlockColor(IBlockState state, Biome biome, BlockPos pos) {
		Block block = state.getBlock();
		if (block == Blocks.GRASS)
			return biome.getGrassColorAtPos(pos);
		if (block == Blocks.STONE)
			return 0x7d7d7d;
		if (block == Blocks.CLAY)
			return 0x9fa4b1;
		if (block == Blocks.DIRT)
			return 0x866043;
		if (block == Blocks.HARDENED_CLAY)
			return 0x975d43;
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
		return biome.getGrassColorAtPos(BlockPos.ORIGIN);
	}

	@SubscribeEvent
	public void fovHook(EntityViewRenderEvent.FOVModifier event) {
		this.fov = event.getFOV();
	}

	public void addToMap(TerrainPoint value) {
		terrainMap.put(value);
		if (value.getX() < this.minimalX)
			this.minimalX = value.getX();
		if (value.getZ() < this.minimalZ)
			this.minimalZ = value.getZ();
		if (value.getX() > this.maximalX)
			this.maximalX = value.getX();
		if (value.getZ() > this.maximalZ)
			this.maximalZ = value.getZ();

		EntityPlayerSP player = Minecraft.getMinecraft().player;
		int renderPosX = (int) (player.lastTickPosX);
		int renderPosZ = (int) (player.lastTickPosZ);
		renderPosX >>= 4;
		renderPosZ >>= 4;
		int dx = renderPosX - value.getX();
		int dz = renderPosZ - value.getZ();
		if (dx * dx < HORIZONT_DISTANCE_SQ || dz * dz < HORIZONT_DISTANCE_SQ) {
			needUpdate = true;
		}
	}

	public void clear() {
		terrainMap.clear();
		minimalX = 0;
		minimalZ = 0;
		maximalX = 0;
		maximalZ = 0;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 0;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return null;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return false;
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		TerrainPoint point = terrainMap.get(pos.getX() >> 4, pos.getZ() >> 4);
		if (point != null)
			return point.biome;
		return Biome.getBiome(0);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return null;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return false;
	}

	public void setSeaLevel(int seaLevelIn) {
		seaLevel = seaLevelIn;
	}
}
