package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.util.AddressUtil.MAX_UPDATE_DISTANCE_CELLS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_CHUNKS;

import org.lwjgl.opengl.GL11;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.util.Vec3f;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
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
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientTerrainSurfaceBufferBuilder implements Runnable {

	private final BufferBuilder buffer = new BufferBuilder(2097152);
	private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
	private final XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 8000);
	private final ConcurrentLinkedQueue<TerrainPoint[]> pendingTerrainPointsUpdate = new ConcurrentLinkedQueue<TerrainPoint[]>();
    private static final VertexFormat VERTEX_FORMAT = (new VertexFormat())
    		.addElement(DefaultVertexFormats.POSITION_3F)
    		.addElement(DefaultVertexFormats.TEX_2F)
    		.addElement(DefaultVertexFormats.TEX_2S)
    		.addElement(DefaultVertexFormats.COLOR_4UB)
    		.addElement(DefaultVertexFormats.NORMAL_3B)
    		.addElement(DefaultVertexFormats.PADDING_1B);

	int minimalXMesh = -4;
	int minimalZMesh = -4;
	int maximalXMesh = 4;
	int maximalZMesh = 4;
	volatile boolean isDrawning = false;
	volatile public boolean ready = false;
	volatile public boolean run = true;

	Object lock = new Object();

	private void addQuad(BufferBuilder worldRendererIn, int x, int z) {
		TerrainPoint tp00 = terrainMap.get(x, z);
		if (tp00 == null)
			return;
		TerrainPoint tp10 = terrainMap.get(x + 1, z);
		TerrainPoint tp01 = terrainMap.get(x, z + 1);
		TerrainPoint tp11 = terrainMap.get(x + 1, z + 1);
		if (tp10 != null && tp01 != null && tp11 != null) {
			this.addQuad(worldRendererIn, tp00, tp01, tp11, tp10);
			return;
		}
	}

	private void addQuad(BufferBuilder worldRendererIn, TerrainPoint tp00, TerrainPoint tp01, TerrainPoint tp11, TerrainPoint tp10) {
		Vec3f n1 = TerrainUtil.calculateNormal(tp11, tp01, tp00);
		Vec3f n2 = TerrainUtil.calculateNormal(tp00, tp10, tp11);
		
		this.addVector(worldRendererIn, tp00, n1, 0.0f, 0.0f);
		this.addVector(worldRendererIn, tp01, n1, 1.0f, 0.0f);
		this.addVector(worldRendererIn, tp11, n1, 1.0f, 1.0f);
		
		this.addVector(worldRendererIn, tp11, n2, 1.0f, 1.0f);
		this.addVector(worldRendererIn, tp10, n2, 0.0f, 1.0f);
		this.addVector(worldRendererIn, tp00, n2, 0.0f, 0.0f);
	}

	@Override
	public void run() {
		while (run) {
			ready = false;
			for (TerrainPoint[] points : pendingTerrainPointsUpdate) {
				this.addToMap(points);
			}
			if (isDrawning) {
				buffer.finishDrawing();
			}
			isDrawning = true;
			buffer.begin(GL11.GL_TRIANGLES, VERTEX_FORMAT);
			int x0 = minimalXMesh;
			int x1 = maximalXMesh;
			int z0 = minimalZMesh;
			int z1 = maximalZMesh;
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player != null) {
				int pmccx = player.chunkCoordX >> MESH_SIZE_BIT_CHUNKS;
				int pmccz = player.chunkCoordZ >> MESH_SIZE_BIT_CHUNKS;
				x0 = Math.max(x0, pmccx - MAX_UPDATE_DISTANCE_CELLS);
				x1 = Math.min(x1, pmccx + MAX_UPDATE_DISTANCE_CELLS);
				z0 = Math.max(z0, pmccz - MAX_UPDATE_DISTANCE_CELLS);
				z1 = Math.min(z1, pmccz + MAX_UPDATE_DISTANCE_CELLS);
			}
			a: for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					WorldClient world = Minecraft.getMinecraft().world;
					if (world == null) {
						break a;
					}
					this.addQuad(buffer, x, z);
				}
			}
			logger.debug("Ready-waiting");
			ready = true;
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void draw(int terrainDisplayList) {
		synchronized (lock) {
			if (!this.isDrawning) {
				logger.debug("Draw-no drawing");
				return;
			}
			GL11.glNewList(terrainDisplayList, 4864);
			this.buffer.finishDrawing();
			this.isDrawning = false;
			this.ready = false;
			this.vboUploader.draw(this.buffer);
			logger.debug("Drawing");
			GL11.glEndList();
		}
	}

	private void addVector(BufferBuilder worldRendererIn, TerrainPoint point, Vec3f n1, float u, float v) {
		int bx = point.chunkX << MESH_SIZE_BIT_BLOCKS;
		int bz = point.chunkZ << MESH_SIZE_BIT_BLOCKS;
		int height = point.blockY;
		BlockPos pos = new BlockPos(bx, height, bz);
		ClientProxy cp = (ClientProxy) CWGFarPlaneViewMod.proxy;
		int color = cp.blockColors.getBlockColor(point.blockState, point.biome, pos, n1.getY());
		float red = (color >> 16 & 255) / 256f;
		float green = (color >> 8 & 255) / 256f;
		float blue = (color & 255) / 256f;
		worldRendererIn.pos(bx, height, bz).tex(u, v).lightmap(240, 0).color(red, green, blue, 1.0f).normal(n1.getX(), n1.getY(), n1.getZ()).endVertex();
	}

	public void schleduleAddToMap(TerrainPoint[] tps) {
		this.pendingTerrainPointsUpdate.add(tps);
		synchronized (lock) {
			lock.notify();
		}
	}
	
	public void addToMap(TerrainPoint[] tps) {
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
