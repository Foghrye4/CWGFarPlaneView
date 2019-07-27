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
		if (tp000 == null || !tp000.isVisible())
			return;
		TerrainPoint3D tp100 = terrainMap.get(x + 1, y, z);
		if (tp100 == null || !tp100.isVisible())
			return;
		TerrainPoint3D tp010 = terrainMap.get(x, y + 1, z);
		if (tp010 == null || !tp010.isVisible())
			return;
		TerrainPoint3D tp001 = terrainMap.get(x, y, z + 1);
		if (tp001 == null || !tp001.isVisible())
			return;
		TerrainPoint3D tp110 = terrainMap.get(x + 1, y + 1, z);
		if (tp110 == null || !tp110.isVisible())
			return;
		TerrainPoint3D tp101 = terrainMap.get(x + 1, y, z + 1);
		if (tp101 == null || !tp101.isVisible())
			return;
		TerrainPoint3D tp011 = terrainMap.get(x, y + 1, z + 1);
		if (tp011 == null || !tp011.isVisible())
			return;
		TerrainPoint3D tp111 = terrainMap.get(x + 1, y + 1, z + 1);
		if (tp111 == null || !tp111.isVisible())
			return;

		boolean cubeExpanded = false;
		if (!this.quadHaveAllPointsVisible(x, y + 2, z, x + 1, y + 2, z + 1)) {
			tp011 = this.searchUp(tp011);
			tp111 = this.searchUp(tp111);
			tp110 = this.searchUp(tp110);
			tp010 = this.searchUp(tp010);
			if (tp011.cubeY != y + 1 || tp111.cubeY != y + 1 || tp110.cubeY != y + 1 || tp010.cubeY != y + 1)
				cubeExpanded = true;
			this.addQuad(worldRendererIn, tp011, tp111, tp110, tp010);
		}
		if (!this.quadHaveAllPointsVisible(x, y - 1, z, x + 1, y - 1, z + 1) || cubeExpanded) {
			if (!cubeExpanded) {
				tp100 = this.searchDown(tp100);
				tp101 = this.searchDown(tp101);
				tp001 = this.searchDown(tp001);
				tp000 = this.searchDown(tp000);
				if (tp100.cubeY != y || tp101.cubeY != y || tp001.cubeY != y || tp000.cubeY != y)
					cubeExpanded = true;
			}
			this.addQuad(worldRendererIn, tp100, tp101, tp001, tp000);
		}
		if (!this.quadHaveAllPointsVisible(x, y, z + 2, x + 1, y + 1, z + 2) || cubeExpanded) {
			this.addQuad(worldRendererIn, tp101, tp111, tp011, tp001);
		}
		if (!this.quadHaveAllPointsVisible(x + 2, y, z, x + 2, y + 1, z + 1) || cubeExpanded) {
			this.addQuad(worldRendererIn, tp111, tp101, tp100, tp110);
		}
		if (!this.quadHaveAllPointsVisible(x, y, z - 1, x + 1, y + 1, z - 1) || cubeExpanded) {
			this.addQuad(worldRendererIn, tp010, tp110, tp100, tp000);
		}
		if (!this.quadHaveAllPointsVisible(x - 1, y, z, x - 1, y + 1, z + 1) || cubeExpanded) {
			this.addQuad(worldRendererIn, tp001, tp011, tp010, tp000);
		}
	}

	private boolean quadHaveAllPointsVisible(int x1, int y1, int z1, int x2, int y2, int z2) {
		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++)
				for (int z = z1; z <= z2; z++) {
					TerrainPoint3D tp = terrainMap.get(x, y, z);
					if (tp == null || !tp.isVisible())
						return false;
				}
		return true;
	}

	private TerrainPoint3D searchUp(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int x = original.cubeX;
		int z = original.cubeZ;
		for (int y = original.cubeY + 1; y <= original.cubeY + 3; y++) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	private TerrainPoint3D searchDown(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int x = original.cubeX;
		int z = original.cubeZ;
		for (int y = original.cubeY - 1; y >= original.cubeY - 3; y--) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	@SuppressWarnings("unused")
	private TerrainPoint3D searchNorth(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int x = original.cubeX;
		int y = original.cubeY;
		for (int z = original.cubeZ - 1; z >= original.cubeZ - 3; z--) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	@SuppressWarnings("unused")
	private TerrainPoint3D searchSouth(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int x = original.cubeX;
		int y = original.cubeY;
		for (int z = original.cubeZ + 1; z <= original.cubeZ + 3; z++) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	@SuppressWarnings("unused")
	private TerrainPoint3D searchEast(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int z = original.cubeZ;
		int y = original.cubeY;
		for (int x = original.cubeX + 1; x <= original.cubeX + 3; x++) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	@SuppressWarnings("unused")
	private TerrainPoint3D searchWest(TerrainPoint3D original) {
		TerrainPoint3D toReturn = original;
		int z = original.cubeZ;
		int y = original.cubeY;
		for (int x = original.cubeX - 1; x >= original.cubeX - 3; x--) {
			TerrainPoint3D tp = terrainMap.get(x, y, z);
			if (tp == null || !tp.isVisible())
				return toReturn;
			toReturn = tp;
		}
		return toReturn;
	}

	private void addQuad(BufferBuilder worldRendererIn, TerrainPoint3D tp00, TerrainPoint3D tp01, TerrainPoint3D tp11,
			TerrainPoint3D tp10) {
		Vec3f n1 = TerrainUtil.calculateNormal(tp11, tp01, tp00);
		Vec3f n2 = TerrainUtil.calculateNormal(tp00, tp10, tp11);

		this.addVector(worldRendererIn, tp00, n1, 0.0f, 0.0f);
		this.addVector(worldRendererIn, tp01, n1, 1.0f, 0.0f);
		this.addVector(worldRendererIn, tp11, n1, 1.0f, 1.0f);

		this.addVector(worldRendererIn, tp11, n2, 1.0f, 1.0f);
		this.addVector(worldRendererIn, tp10, n2, 0.0f, 1.0f);
		this.addVector(worldRendererIn, tp00, n2, 0.0f, 0.0f);
	}

	private void addVector(BufferBuilder worldRendererIn, TerrainPoint3D point, Vec3f n1, float u, float v) {
		int bx = (point.cubeX << MESH_SIZE_BIT_BLOCKS) + point.localX;
		int by = (point.cubeY << MESH_SIZE_BIT_BLOCKS) + point.localY;
		int bz = (point.cubeZ << MESH_SIZE_BIT_BLOCKS) + point.localZ;
		BlockPos pos = new BlockPos(bx, by, bz);
		ClientProxy cp = (ClientProxy) CWGFarPlaneViewMod.proxy;
		int color = cp.blockColors.getBlockColor(point.blockState, point.biome, pos);
		float red = (color >> 16 & 255) / 256f;
		float green = (color >> 8 & 255) / 256f;
		float blue = (color & 255) / 256f;
		worldRendererIn.pos(bx, by, bz).tex(u, v).lightmap(240, 0).color(red, green, blue, 1.0f)
				.normal(n1.getX(), n1.getY(), n1.getZ()).endVertex();
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
