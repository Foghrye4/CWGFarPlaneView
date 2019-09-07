package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.util.AddressUtil.MAX_UPDATE_DISTANCE_CELLS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;
import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_CHUNKS;
import static java.lang.Math.*;

import org.lwjgl.opengl.GL11;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.util.Vec3f;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientTerrain3DShapeBufferBuilder implements Runnable {

	private final BufferBuilder buffer = new BufferBuilder(2097152);
	private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
	private final XYZMap<TerrainPoint3D> terrainMap = new XYZMap<TerrainPoint3D>(0.8f, 8000);
	private final ConcurrentLinkedQueue<TerrainPoint3D[]> pendingTerrainPointsUpdate = new ConcurrentLinkedQueue<TerrainPoint3D[]>();
	private static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F)
			.addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.TEX_2S)
			.addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.NORMAL_3B)
			.addElement(DefaultVertexFormats.PADDING_1B);

	int minimalXMesh = -4;
	int minimalYMesh = -4;
	int minimalZMesh = -4;
	int maximalXMesh = 4;
	int maximalYMesh = 4;
	int maximalZMesh = 4;
	volatile boolean isDrawning = false;
	volatile public boolean ready = false;
	volatile public boolean run = true;

	Object lock = new Object();

	private void addTriangles(BufferBuilder worldRendererIn, int x, int y, int z) {
		TerrainPoint3D tp000 = terrainMap.get(x, y, z);
		TerrainPoint3D tp100 = terrainMap.get(x + 1, y, z);
		TerrainPoint3D tp010 = terrainMap.get(x, y + 1, z);
		TerrainPoint3D tp001 = terrainMap.get(x, y, z + 1);
		TerrainPoint3D tp110 = terrainMap.get(x + 1, y + 1, z);
		TerrainPoint3D tp101 = terrainMap.get(x + 1, y, z + 1);
		TerrainPoint3D tp011 = terrainMap.get(x, y + 1, z + 1);
		TerrainPoint3D tp111 = terrainMap.get(x + 1, y + 1, z + 1);
		CubeRenderer.renderCube(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
	}
	
	@Override
	public void run() {
		while (run) {
			ready = false;
			for (TerrainPoint3D[] points : pendingTerrainPointsUpdate) {
				this.addToMap(points);
			}
			if (isDrawning) {
				buffer.finishDrawing();
			}
			isDrawning = true;
			buffer.begin(GL11.GL_TRIANGLES, VERTEX_FORMAT);
			int x0 = minimalXMesh;
			int x1 = maximalXMesh;
			int y0 = minimalYMesh;
			int y1 = maximalYMesh;
			int z0 = minimalZMesh;
			int z1 = maximalZMesh;
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player != null) {
				int pmccx = player.chunkCoordX >> MESH_SIZE_BIT_CHUNKS;
				int pmccy = player.chunkCoordY >> MESH_SIZE_BIT_CHUNKS;
				int pmccz = player.chunkCoordZ >> MESH_SIZE_BIT_CHUNKS;
				x0 = Math.max(x0, pmccx - MAX_UPDATE_DISTANCE_CELLS);
				x1 = Math.min(x1, pmccx + MAX_UPDATE_DISTANCE_CELLS);
				y0 = Math.max(y0, pmccy - MAX_UPDATE_DISTANCE_CELLS);
				y1 = Math.min(y1, pmccy + MAX_UPDATE_DISTANCE_CELLS);
				z0 = Math.max(z0, pmccz - MAX_UPDATE_DISTANCE_CELLS);
				z1 = Math.min(z1, pmccz + MAX_UPDATE_DISTANCE_CELLS);
			}
			a: for (int x = x0; x <= x1; x++) {
				for (int y = y0; y <= y1; y++) {
					for (int z = z0; z <= z1; z++) {
						WorldClient world = Minecraft.getMinecraft().world;
						if (world == null) {
							break a;
						}
						this.addTriangles(buffer, x, y, z);
					}
				}
			}
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
		if (!this.isDrawning) {
			return;
		}
		GL11.glNewList(terrainDisplayList, 4864);
		this.buffer.finishDrawing();
		this.isDrawning = false;
		this.ready = false;
		this.vboUploader.draw(this.buffer);
		GL11.glEndList();
	}

	public void schleduleAddToMap(TerrainPoint3D[] tps) {
		this.pendingTerrainPointsUpdate.add(tps);
		synchronized (lock) {
			lock.notify();
		}
	}

	public void addToMap(TerrainPoint3D[] tps) {
		for (TerrainPoint3D tp : tps) {
			terrainMap.put(tp);
			if (tp.getX() < this.minimalXMesh)
				this.minimalXMesh = tp.getX();
			if (tp.getY() < this.minimalYMesh)
				this.minimalYMesh = tp.getY();
			if (tp.getZ() < this.minimalZMesh)
				this.minimalZMesh = tp.getZ();
			if (tp.getX() > this.maximalXMesh)
				this.maximalXMesh = tp.getX();
			if (tp.getY() > this.maximalYMesh)
				this.maximalYMesh = tp.getY();
			if (tp.getZ() > this.maximalZMesh)
				this.maximalZMesh = tp.getZ();
		}
	}

	public void clear() {
		terrainMap.clear();
		minimalXMesh = 0;
		minimalYMesh = 0;
		minimalZMesh = 0;
		maximalXMesh = 0;
		maximalYMesh = 0;
		maximalZMesh = 0;
	}

	public void stop() {
		run = false;
		synchronized (lock) {
			lock.notify();
		}
	}
}
