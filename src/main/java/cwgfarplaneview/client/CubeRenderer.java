package cwgfarplaneview.client;

import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.util.Vec3f;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;

/** Not a parallel thread safe */
public class CubeRenderer {
	
	private static TerrainPoint3D[] allPoints = new TerrainPoint3D[8];
	private static TerrainPoint3D[][] neighbors = new TerrainPoint3D[8][3];
	
	static {
		neighbors[0] = new TerrainPoint3D[3];
		neighbors[1] = new TerrainPoint3D[3];
		neighbors[2] = new TerrainPoint3D[3];
		neighbors[3] = new TerrainPoint3D[3];
		neighbors[4] = new TerrainPoint3D[3];
		neighbors[5] = new TerrainPoint3D[3];
		neighbors[6] = new TerrainPoint3D[3];
		neighbors[7] = new TerrainPoint3D[3];
	}
	
	public static void renderCube(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		int amountPointsMissing = 0;
		if(tp000==null || !tp000.isVisible()) {
			amountPointsMissing++;
		} else if (tp100 == null || !tp100.isVisible()) {
			amountPointsMissing++;
		} else if (tp010 == null || !tp010.isVisible()) {
			amountPointsMissing++;
		} else if (tp001 == null || !tp001.isVisible()) {
			amountPointsMissing++;
		} else if (tp110 == null || !tp110.isVisible()) {
			amountPointsMissing++;
		} else if (tp101 == null || !tp101.isVisible()) {
			amountPointsMissing++;
		} else if (tp011 == null || !tp011.isVisible()) {
			amountPointsMissing++;
		} else if (tp111 == null || !tp111.isVisible()) {
			amountPointsMissing++;
		}
		switch(amountPointsMissing) {
		case 0:
		case 6:
		case 7:
		case 8:
			return;
		case 1:
			renderCubeWithOnePointMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
			return;
		}
	}
	
	private static void renderCubeWithOnePointMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		if(tp000==null || !tp000.isVisible()) {
			addTriangle(worldRendererIn, tp010, tp001,tp100);
		} else if (tp100 == null || !tp100.isVisible()) {
			addTriangle(worldRendererIn, tp110, tp000, tp101);
		} else if (tp010 == null || !tp010.isVisible()) {
			addTriangle(worldRendererIn, tp110, tp011, tp000);
		} else if (tp001 == null || !tp001.isVisible()) {
			addTriangle(worldRendererIn, tp011, tp101, tp000);
		} else if (tp110 == null || !tp110.isVisible()) {
			addTriangle(worldRendererIn, tp111, tp010, tp100);
		} else if (tp101 == null || !tp101.isVisible()) {
			addTriangle(worldRendererIn, tp111, tp100, tp001);
		} else if (tp011 == null || !tp011.isVisible()) {
			addTriangle(worldRendererIn, tp010, tp111, tp001);
		} else if (tp111 == null || !tp111.isVisible()) {
			addTriangle(worldRendererIn, tp011, tp110, tp101);
		}
	}
	
	private static void renderCubeWithSomePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		allPoints[0] = tp000;
		allPoints[1] = tp100;
		allPoints[2] = tp010;
		allPoints[3] = tp001;
		allPoints[4] = tp110;
		allPoints[5] = tp101;
		allPoints[6] = tp011;
		allPoints[7] = tp111;
		neighbors[0][0] = tp010;
		neighbors[0][1] = tp001;
		neighbors[0][2] = tp100;
		neighbors[1][0] = tp110;
		neighbors[1][1] = tp101;
		neighbors[1][2] = tp000;
		neighbors[2][0] = tp110;//tp010
		neighbors[2][1] = tp011;
		neighbors[2][2] = tp000;
		neighbors[3][0] = tp011;//tp001
		neighbors[3][1] = tp101;
		neighbors[3][2] = tp000;
		neighbors[4][0] = tp010;//tp110
		neighbors[4][1] = tp100;
		neighbors[4][2] = tp111;
		neighbors[5][0] = tp111;//tp101
		neighbors[5][1] = tp100;
		neighbors[5][2] = tp001;
		neighbors[6][0] = tp010;//tp011
		neighbors[6][1] = tp111;
		neighbors[6][2] = tp001;
		neighbors[7][0] = tp110;//tp111
		neighbors[7][1] = tp011;
		neighbors[7][2] = tp101;
		int trianglesRendered = 0;
		for(int i0=0;i0<8;i0++) {
			for(int i1=i0+1;i1<8;i1++) {
				for(int i2=i1+1;i2<8;i2++) {
					if(!isVisible(i0)||!isVisible(i1)||!isVisible(i2))
						continue;
					if(isAllNeighborsVisible(i0) || isAllNeighborsVisible(i1) || isAllNeighborsVisible(i2))
						continue;
				}
			}
		}
	}
	
	private static boolean isVisible(int i) {
		return allPoints[i]!=null && allPoints[i].isVisible();
	}
	
	private static boolean isAllNeighborsVisible(int i) {
		TerrainPoint3D[] nbrs = neighbors[i];
		if(nbrs[0]==null || !nbrs[0].isVisible())
			return false;
		if(nbrs[1]==null || !nbrs[1].isVisible())
			return false;
		if(nbrs[2]==null || !nbrs[2].isVisible())
			return false;
		return true;
	}

	/*
	private static void renderCubeWithFivePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		if(tp000!=null && tp000.isVisible()) {
			if(tp010!=null && tp010.isVisible()) {
				if(tp001!=null && tp001.isVisible()) {
					addTriangle(worldRendererIn, tp000, tp001,tp010);
				}
				else if(tp011!=null && tp011.isVisible()) {
					addTriangle(worldRendererIn, tp000, tp011,tp010);
				}
			} else if(tp100!=null && tp100.isVisible()) {
				if(tp110!=null && tp110.isVisible()) {
					addTriangle(worldRendererIn, tp000, tp110,tp100);
				}
				
			}
			
		} else if (tp100 == null || !tp100.isVisible()) {
			addTriangle(worldRendererIn, tp110, tp000, tp101);
		} else if (tp010 == null || !tp010.isVisible()) {
			addTriangle(worldRendererIn, tp110, tp011, tp000);
		} else if (tp001 == null || !tp001.isVisible()) {
			addTriangle(worldRendererIn, tp011, tp101, tp000);
		} else if (tp110 == null || !tp110.isVisible()) {
			addTriangle(worldRendererIn, tp111, tp010, tp100);
		} else if (tp101 == null || !tp101.isVisible()) {
			addTriangle(worldRendererIn, tp111, tp100, tp001);
		} else if (tp011 == null || !tp011.isVisible()) {
			addTriangle(worldRendererIn, tp010, tp111, tp001);
		} else if (tp111 == null || !tp111.isVisible()) {
			addTriangle(worldRendererIn, tp011, tp110, tp101);
		}
	}
	*/
	private static void addTriangle(BufferBuilder worldRendererIn, TerrainPoint3D tp1,
			TerrainPoint3D tp2, TerrainPoint3D tp3) {
		Vec3f n = TerrainUtil.calculateNormal(tp3, tp2, tp1);
		addVector(worldRendererIn, tp1, n, 1.0f, 1.0f);
		addVector(worldRendererIn, tp2, n, 0.0f, 1.0f);
		addVector(worldRendererIn, tp3, n, 0.0f, 0.0f);
	}
	
	private static void addVector(BufferBuilder worldRendererIn, TerrainPoint3D point, Vec3f n1, float u, float v) {
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
	
	private void getNeighbors(int pointIndex) {
	}
}
