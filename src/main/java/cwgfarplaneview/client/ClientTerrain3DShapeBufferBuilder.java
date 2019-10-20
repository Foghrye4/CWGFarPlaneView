package cwgfarplaneview.client;

import static cwgfarplaneview.util.TerrainConfig.CUBE_SIZE_BIT_MESH;
import static cwgfarplaneview.util.TerrainConfig.MESH_SIZE_BIT_CHUNKS;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_HORIZONTAL;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_VERTICAL;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL11;

import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.world.terrain.volumetric.TerrainCube;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;

public class ClientTerrain3DShapeBufferBuilder implements Runnable {

	private static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F)
			.addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.TEX_2S)
			.addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.NORMAL_3B)
			.addElement(DefaultVertexFormats.PADDING_1B);
	
	private final BufferBuilder buffer = new BufferBuilder(2097152);
	private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
	private final XYZMap<TerrainCube> terrainMap = new XYZMap<TerrainCube>(0.8f, 8000);
	private final ConcurrentLinkedQueue<TerrainCube> pendingTerrainCubesUpdate = new ConcurrentLinkedQueue<TerrainCube>();
	private final TerrainPoint3D[][][] terrainPointsCache = new TerrainPoint3D[4][4][4];
	private final MutableWeightedNormal[][][] normalsCache = new MutableWeightedNormal[4][4][4];

	int minimalXMesh = -4;
	int minimalYMesh = -4;
	int minimalZMesh = -4;
	int maximalXMesh = 4;
	int maximalYMesh = 4;
	int maximalZMesh = 4;
	volatile public boolean ready = false;
	volatile public boolean run = true;

	Object lock = new Object();
	
	public ClientTerrain3DShapeBufferBuilder() {
		for (int ix = 0; ix < 4; ix++)
			for (int iy = 0; iy < 4; iy++)
				for (int iz = 0; iz < 4; iz++) {
					normalsCache[ix][iy][iz] = new MutableWeightedNormal();
					terrainPointsCache[ix][iy][iz] = new TerrainPoint3D();
				}
	}

	private void addTriangles(BufferBuilder worldRendererIn, int x, int y, int z) {
		for (int ix = 0; ix < 4; ix++)
			for (int iy = 0; iy < 4; iy++)
				for (int iz = 0; iz < 4; iz++) {
					int x0 = x-1+ix;
					int y0 = y-1+iy;
					int z0 = z-1+iz;
					TerrainCube cube = terrainMap.get(TerrainConfig.meshToCube(x0), TerrainConfig.meshToCube(y0), TerrainConfig.meshToCube(z0));
					if (cube != null)
						cube.getTerrainPoint(terrainPointsCache[ix][iy][iz], x0, y0, z0);
					else
						terrainPointsCache[ix][iy][iz].blockState = Blocks.AIR.getDefaultState();
					normalsCache[ix][iy][iz].reset();
				}
		for (int ix = 0; ix < 3; ix++)
			for (int iy = 0; iy < 3; iy++)
				for (int iz = 0; iz < 3; iz++) {
					CubeRenderer.renderCube(worldRendererIn, 
							terrainPointsCache[ix][iy][iz], 
							terrainPointsCache[ix+1][iy][iz], 
							terrainPointsCache[ix][iy+1][iz], 
							terrainPointsCache[ix][iy][iz+1],
							terrainPointsCache[ix+1][iy+1][iz],
							terrainPointsCache[ix+1][iy][iz+1],
							terrainPointsCache[ix][iy+1][iz+1],
							terrainPointsCache[ix+1][iy+1][iz+1], 
							normalsCache[ix][iy][iz], 
							normalsCache[ix+1][iy][iz], 
							normalsCache[ix][iy+1][iz], 
							normalsCache[ix][iy][iz+1],
							normalsCache[ix+1][iy+1][iz],
							normalsCache[ix+1][iy][iz+1],
							normalsCache[ix][iy+1][iz+1],
							normalsCache[ix+1][iy+1][iz+1],
									true);
				}
		int ix = 1;
		int iy = 1;
		int iz = 1;
		CubeRenderer.renderCube(worldRendererIn, 
				terrainPointsCache[ix][iy][iz], 
				terrainPointsCache[ix+1][iy][iz], 
				terrainPointsCache[ix][iy+1][iz], 
				terrainPointsCache[ix][iy][iz+1],
				terrainPointsCache[ix+1][iy+1][iz],
				terrainPointsCache[ix+1][iy][iz+1],
				terrainPointsCache[ix][iy+1][iz+1],
				terrainPointsCache[ix+1][iy+1][iz+1], 
				normalsCache[ix][iy][iz], 
				normalsCache[ix+1][iy][iz], 
				normalsCache[ix][iy+1][iz], 
				normalsCache[ix][iy][iz+1],
				normalsCache[ix+1][iy+1][iz],
				normalsCache[ix+1][iy][iz+1],
				normalsCache[ix][iy+1][iz+1],
				normalsCache[ix+1][iy+1][iz+1],
						false);
	}
	
	@Override
	public void run() {
		while (run) {
			ready = false;
			if(pendingTerrainCubesUpdate.isEmpty()) {
				synchronized (lock) {
					try {
						lock.wait(2500);
					} catch (InterruptedException e) {
					}
				}
				continue;
			}
			for (TerrainCube cube : pendingTerrainCubesUpdate) {
				this.addToMap(cube);
			}
			synchronized (lock) {
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
					x0 = Math.max(x0, pmccx - VOLUMETRIC_HORIZONTAL.maxUpdateDistanceCells);
					x1 = Math.min(x1, pmccx + VOLUMETRIC_HORIZONTAL.maxUpdateDistanceCells);
					y0 = Math.max(y0, pmccy - VOLUMETRIC_VERTICAL.maxUpdateDistanceCells);
					y1 = Math.min(y1, pmccy + VOLUMETRIC_VERTICAL.maxUpdateDistanceCells);
					z0 = Math.max(z0, pmccz - VOLUMETRIC_HORIZONTAL.maxUpdateDistanceCells);
					z1 = Math.min(z1, pmccz + VOLUMETRIC_HORIZONTAL.maxUpdateDistanceCells);
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
				try {
					lock.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void addToMap(TerrainCube cube) {
		terrainMap.put(cube);
		int minX = cube.x << CUBE_SIZE_BIT_MESH;
		int minY = cube.y << CUBE_SIZE_BIT_MESH;
		int minZ = cube.z << CUBE_SIZE_BIT_MESH;
		
		int maxX = minX + 15;
		int maxY = minY + 15;
		int maxZ = minZ + 15;
		
		if (minX < this.minimalXMesh)
			this.minimalXMesh = minX;
		if (minY < this.minimalYMesh)
			this.minimalYMesh = minY;
		if (minZ < this.minimalZMesh)
			this.minimalZMesh = minZ;
		if (maxX > this.maximalXMesh)
			this.maximalXMesh = maxX;
		if (maxY > this.maximalYMesh)
			this.maximalYMesh = maxY;
		if (maxZ > this.maximalZMesh)
			this.maximalZMesh = maxZ;
	}

	public void draw(int terrainDisplayList) {
		if (!this.ready) {
			return;
		}
		synchronized (lock) {
			GL11.glNewList(terrainDisplayList, 4864);
			this.buffer.finishDrawing();
			this.ready = false;
			this.vboUploader.draw(this.buffer);
			GL11.glEndList();
			lock.notify();
		}
	}

	public void schleduleAddToMap(TerrainCube cube) {
		pendingTerrainCubesUpdate.add(cube);
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
