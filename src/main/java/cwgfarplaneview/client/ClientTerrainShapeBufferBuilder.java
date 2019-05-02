package cwgfarplaneview.client;

import static cwgfarplaneview.util.AddressUtil.HORIZONT_DISTANCE_SQ;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_CHUNKS;

import org.lwjgl.opengl.GL11;

import cwgfarplaneview.world.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ClientTerrainShapeBufferBuilder implements Runnable {

	private final BufferBuilder buffer = new BufferBuilder(2097152);
	private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
	private final XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 8000);

	int minimalXMesh = -4;
	int minimalZMesh = -4;
	int maximalXMesh = 4;
	int maximalZMesh = 4;
	volatile boolean isDrawning = false;
	volatile public boolean ready = false;
	volatile public boolean run = true;

	Object lock = new Object();


	private void addQuad(BufferBuilder worldRendererIn, WorldClient world, int x, int z) {
		this.addVector(worldRendererIn, world, x, z, 0.0f, 0.0f);
		this.addVector(worldRendererIn, world, x, z + 1, 1.0f, 0.0f);
		this.addVector(worldRendererIn, world, x + 1, z + 1, 1.0f, 1.0f);
		this.addVector(worldRendererIn, world, x + 1, z, 0.0f, 1.0f);
	}

	@Override
	public void run() {
		while (run) {
			ready = false;
			synchronized (lock) {
				isDrawning = true;
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
				a: for (int x = minimalXMesh; x <= maximalXMesh; x++) {
					for (int z = minimalZMesh; z <= maximalZMesh; z++) {
						WorldClient world = Minecraft.getMinecraft().world;
						if (world == null) {
							break a;
						}
						this.addQuad(buffer, world, x, z);
					}
				}
				ready = true;
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void draw() {
		synchronized (lock) {
			this.buffer.finishDrawing();
			this.isDrawning = false;
			this.ready = false;
			this.vboUploader.draw(this.buffer);
		}
	}

	private void addVector(BufferBuilder worldRendererIn, WorldClient world, int x, int z, float u, float v) {
		int bx = x << MESH_SIZE_BIT_BLOCKS;
		int bz = z << MESH_SIZE_BIT_BLOCKS;
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
			switch(state.getValue(BlockStainedHardenedClay.COLOR)) {
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
		return multiplyColors(0x979797, biome.getGrassColorAtPos(BlockPos.ORIGIN));
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

	public void addToMap(TerrainPoint[] tps) {
		synchronized (lock) {
			boolean needUpdate = false;
			for (TerrainPoint tp : tps) {
				terrainMap.put(tp);
				if (tp.getX() < this.minimalXMesh)
					this.minimalXMesh = tp.getX();
				if (tp.getZ() < this.minimalZMesh)
					this.minimalZMesh = tp.getZ();
				if (tp.getX() > this.maximalXMesh)
					this.maximalXMesh = tp.getX();
				if (tp.getZ() > this.maximalZMesh)
					this.maximalZMesh = tp.getZ();

				EntityPlayerSP player = Minecraft.getMinecraft().player;
				int renderPosX = (int) (player.lastTickPosX);
				int renderPosZ = (int) (player.lastTickPosZ);
				renderPosX >>= 4;
				renderPosZ >>= 4;
				int dx = renderPosX - (tp.getX() << MESH_SIZE_BIT_CHUNKS);
				int dz = renderPosZ - (tp.getZ() << MESH_SIZE_BIT_CHUNKS);
				if (dx * dx < HORIZONT_DISTANCE_SQ || dz * dz < HORIZONT_DISTANCE_SQ) {
					needUpdate = true;
				}
			}
			if (needUpdate) {
				if (isDrawning) {
					buffer.finishDrawing();
					isDrawning = false;
				}
				lock.notify();
			}
		}
	}

	public void clear() {
		synchronized (lock) {
			terrainMap.clear();
			minimalXMesh = 0;
			minimalZMesh = 0;
			maximalXMesh = 0;
			maximalZMesh = 0;
		}
	}

	public void stop() {
		synchronized (lock) {
			run = false;
			lock.notify();
		}
	}
}
