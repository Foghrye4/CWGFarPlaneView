package cwgfarplaneview.client;

import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.util.FPVMathUtil;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.util.Vec3f;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.MathUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

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
		if (tp000 == null || !tp000.isVisible())
			amountPointsMissing++;
		if (tp100 == null || !tp100.isVisible())
			amountPointsMissing++;
		if (tp010 == null || !tp010.isVisible())
			amountPointsMissing++;
		if (tp001 == null || !tp001.isVisible())
			amountPointsMissing++;
		if (tp110 == null || !tp110.isVisible())
			amountPointsMissing++;
		if (tp101 == null || !tp101.isVisible())
			amountPointsMissing++;
		if (tp011 == null || !tp011.isVisible())
			amountPointsMissing++;
		if (tp111 == null || !tp111.isVisible())
			amountPointsMissing++;
		switch(amountPointsMissing) {
		case 0:
		case 6:
		case 7:
		case 8:
			return;
		case 2:
			renderCubeWithTwoPointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
			return;
		case 3:
			renderCubeWithThreePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
			return;
		case 4:
			renderCubeWithFourPointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
			return;
		default:
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
			return;
		}
	}
	
	private static void renderCubeWithTwoPointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		boolean n000 = tp000 == null || !tp000.isVisible();
		boolean n100 = tp100 == null || !tp100.isVisible();
		boolean n010 = tp010 == null || !tp010.isVisible();
		boolean n001 = tp001 == null || !tp001.isVisible();
		boolean n110 = tp110 == null || !tp110.isVisible();
		boolean n101 = tp101 == null || !tp101.isVisible();
		boolean n011 = tp011 == null || !tp011.isVisible();
		boolean n111 = tp111 == null || !tp111.isVisible();
		if (n000 && n100) {
			addQuad(worldRendererIn, tp001, tp101, tp110, tp010);
		}
		else if (n000 && n010) {
			addQuad(worldRendererIn, tp011, tp001, tp100, tp110);
		}
		else if (n000 && n001) {
			addQuad(worldRendererIn, tp011, tp101, tp100, tp010);
		}
		else if (n111 && n101) {
			addQuad(worldRendererIn, tp011, tp110, tp100, tp001);
		}
		else if (n111 && n011) {
			addQuad(worldRendererIn, tp010, tp110, tp101, tp001);
		}
		else if (n111 && n110) {
			addQuad(worldRendererIn, tp011, tp010, tp100, tp101);
		}
		else if (n010 && n110) {
			addQuad(worldRendererIn, tp011, tp000, tp100, tp111);
		}
		else if (n100 && n110) {
			addQuad(worldRendererIn, tp111, tp010, tp000, tp101);
		}
		else if (n100 && n101) {
			addQuad(worldRendererIn, tp111, tp110, tp000, tp001);
		}
		else if (n001 && n101) {
			addQuad(worldRendererIn, tp011, tp111, tp100, tp000);
		}
		else if (n001 && n011) {
			addQuad(worldRendererIn, tp010, tp111, tp101, tp000);
		}
		else if (n011 && n010) {
			addQuad(worldRendererIn, tp111, tp001, tp000, tp110);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
		}
	}
	
	private static void renderCubeWithThreePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		boolean n000 = tp000 == null || !tp000.isVisible();
		boolean n100 = tp100 == null || !tp100.isVisible();
		boolean n010 = tp010 == null || !tp010.isVisible();
		boolean n001 = tp001 == null || !tp001.isVisible();
		boolean n110 = tp110 == null || !tp110.isVisible();
		boolean n101 = tp101 == null || !tp101.isVisible();
		boolean n011 = tp011 == null || !tp011.isVisible();
		boolean n111 = tp111 == null || !tp111.isVisible();
		// Top side
		if (n110 && n111 && n011) {
			addQuad(worldRendererIn, tp001, tp010, tp100, tp101);
		}
		else if (n010 && n111 && n011) {
			addQuad(worldRendererIn, tp110, tp101, tp001, tp000);
		}
		else if (n010 && n110 && n011) {
			addQuad(worldRendererIn, tp111, tp001, tp000, tp100);
		}
		else if (n010 && n110 && n111) {
			addQuad(worldRendererIn, tp011, tp000, tp100, tp101);
		}
		// Positive X
		else if (n110 && n100 && n101) {
			addQuad(worldRendererIn, tp111, tp010, tp000, tp001);
		}
		else if (n111 && n100 && n101) {
			addQuad(worldRendererIn, tp110, tp000, tp001, tp011);
		}
		else if (n111 && n110 && n101) {
			addQuad(worldRendererIn, tp100, tp001, tp011, tp010);
		}
		else if (n111 && n110 && n100) { //101
			addQuad(worldRendererIn, tp101, tp011, tp010, tp000);
		}
		// Positive Z
		else if (n111 && n101 && n001) { //011
			addQuad(worldRendererIn, tp011, tp110, tp100, tp000);
		}
		else if (n011 && n101 && n001) { //111
			addQuad(worldRendererIn, tp111, tp100, tp000, tp010);
		}
		else if (n011 && n111 && n001) { //101
			addQuad(worldRendererIn, tp101, tp000, tp010, tp110);
		}
		else if (n011 && n111 && n101) { //001
			addQuad(worldRendererIn, tp001, tp010, tp110, tp100);
		}
		// Bottom side
		else if (n100 && n101 && n001) { //000
			addQuad(worldRendererIn, tp000, tp011, tp111, tp110);
		}
		else if (n000 && n101 && n001) { //100
			addQuad(worldRendererIn, tp100, tp010, tp011, tp111);
		}
		else if (n000 && n100 && n001) { //101
			addQuad(worldRendererIn, tp101, tp110, tp010, tp011);
		}
		else if (n000 && n100 && n101) { //001
			addQuad(worldRendererIn, tp001, tp111, tp110, tp010);
		}
		// Negative X
		else if (n010 && n000 && n001) { //011
			addQuad(worldRendererIn, tp011, tp101, tp100, tp110);
		}
		else if (n011 && n000 && n001) { //010
			addQuad(worldRendererIn, tp010, tp111, tp101, tp100);
		}
		else if (n011 && n010 && n001) { //000
			addQuad(worldRendererIn, tp000, tp110, tp111, tp101);
		}
		else if (n011 && n010 && n000) { //001
			addQuad(worldRendererIn, tp001, tp100, tp110, tp111);
		}
		// Negative Z
		else if (n110 && n100 && n000) { //010
			addQuad(worldRendererIn, tp010, tp001, tp101, tp111);
		}
		else if (n010 && n100 && n000) { //110
			addQuad(worldRendererIn, tp110, tp011, tp001, tp101);
		}
		else if (n010 && n110 && n000) { //100
			addQuad(worldRendererIn, tp100, tp111, tp011, tp001);
		}
		else if (n010 && n110 && n100) { //000
			addQuad(worldRendererIn, tp000, tp101, tp111, tp011);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
		}
	}
	
	private static void renderCubeWithFourPointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		boolean n000 = tp000 == null || !tp000.isVisible();
		boolean n100 = tp100 == null || !tp100.isVisible();
		boolean n010 = tp010 == null || !tp010.isVisible();
		boolean n001 = tp001 == null || !tp001.isVisible();
		boolean n110 = tp110 == null || !tp110.isVisible();
		boolean n101 = tp101 == null || !tp101.isVisible();
		boolean n011 = tp011 == null || !tp011.isVisible();
		boolean n111 = tp111 == null || !tp111.isVisible();
		if (n010 && n110 && n111 && n011) { //Top side
			addQuad(worldRendererIn, tp000, tp100, tp101, tp001);
		}
		else if (n000 && n100 && n101 && n001) {
			addQuad(worldRendererIn, tp010, tp011, tp111, tp110);
		}
		else if (n111 && n110 && n100 && n101) {
			addQuad(worldRendererIn, tp011, tp010, tp000, tp001);
		}
		else if (n011 && n010 && n000 && n001) {
			addQuad(worldRendererIn, tp111, tp101, tp100, tp110);
		}
		else if (n011 && n111 && n101 && n001) {
			addQuad(worldRendererIn, tp010, tp110, tp100, tp000);
		}
		else if (n010 && n110 && n100 && n000) {
			addQuad(worldRendererIn, tp011, tp001, tp101, tp111);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
		}
	}
	
	private static void renderCubeWithSomePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		refreshArrays(tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111);
		int lastRenderedI0 = -1;
		int lastRenderedI1 = -1;
		int lastRenderedI2 = -1;
		for(int i0=0;i0<8;i0++) {
			for(int i1=i0+1;i1<8;i1++) {
				next_triangle:for(int i2=i1+1;i2<8;i2++) {
					TerrainPoint3D tp1 = allPoints[i0];
					TerrainPoint3D tp2 = allPoints[i1];
					TerrainPoint3D tp3 = allPoints[i2];
					if (tp1 == null || tp2 == null || tp3 == null)
						continue;
					if (!tp1.isVisible() || !tp2.isVisible() || !tp3.isVisible())
						continue;
					if(isAllNeighborsVisible(i0) || isAllNeighborsVisible(i1) || isAllNeighborsVisible(i2))
						continue;
					Vec3i straightN = TerrainUtil.calculateNonNormalized(tp3, tp2, tp1);
					Vec3i invertedN = TerrainUtil.calculateNonNormalized(tp1, tp2, tp3);
					boolean straightSideShouldBeRendered = false;
					boolean invertedSideShouldBeRendered = false;
					for (int j = 0; !straightSideShouldBeRendered && !invertedSideShouldBeRendered
							&& j < neighbors[i0].length + neighbors[i1].length + neighbors[i2].length; j++) {
						TerrainPoint3D n = null;
						if (j < neighbors[i0].length) {
							n = neighbors[i0][j];
						} else if (j < neighbors[i0].length + neighbors[i1].length) {
							n = neighbors[i1][j - neighbors[i0].length];
						} else {
							n = neighbors[i2][j - neighbors[i0].length - neighbors[i1].length];
						}
						if (n != null && !n.isVisible()) {
							int dx = n.cubeX - tp1.cubeX;
							int dy = n.cubeY - tp1.cubeY;
							int dz = n.cubeZ - tp1.cubeZ;
							int dotProductStraight = straightN.getX() * dx + straightN.getY() * dy
									+ straightN.getZ() * dz;
							int dotProductInverted = invertedN.getX() * dx + invertedN.getY() * dy
									+ invertedN.getZ() * dz;
							if (dotProductStraight >= 0)
								straightSideShouldBeRendered = true;
							if (dotProductInverted >= 0)
								invertedSideShouldBeRendered = true;
						}
					}
					
					int firstMatch = getFirstMatch(lastRenderedI0, lastRenderedI1, lastRenderedI2, i0, i1, i2);
					if (firstMatch != -1) {
						int secondMatch = getSecondMatch(lastRenderedI0, lastRenderedI1, lastRenderedI2, i0, i1, i2,
								firstMatch);
						if (secondMatch != -1) {
							int lastMiss = getLastMiss(lastRenderedI0, lastRenderedI1, lastRenderedI2, i0, i1, i2,
									firstMatch, secondMatch);
							int currentMiss = getCurrentMiss(lastRenderedI0, lastRenderedI1, lastRenderedI2, i0, i1, i2,
									firstMatch, secondMatch);
							TerrainPoint3D firstMatchTP = allPoints[firstMatch];
							TerrainPoint3D secondMatchTP = allPoints[secondMatch];
							TerrainPoint3D lastMissTP = allPoints[lastMiss];
							TerrainPoint3D currentMissTP = allPoints[currentMiss];
							int crossNDotResult = TerrainUtil.crossAndDot(currentMissTP, firstMatchTP, secondMatchTP,
									lastMissTP);
							boolean isOnASamePlane = TerrainUtil.isOnASamePlane(currentMissTP, firstMatchTP, secondMatchTP,
									lastMissTP);
							if (crossNDotResult > 0 && isOnASamePlane) {
								continue next_triangle;
							}
						}
					}
					
					if(straightSideShouldBeRendered) {
						lastRenderedI0 = i0;
						lastRenderedI1 = i1;
						lastRenderedI2 = i2;
						addTriangle(worldRendererIn, tp1, tp2, tp3);
					}
					if(invertedSideShouldBeRendered) {
						lastRenderedI0 = i0;
						lastRenderedI1 = i1;
						lastRenderedI2 = i2;
						addTriangle(worldRendererIn, tp3, tp2, tp1);
					}
				}
			}
		}
	}
	
	private static void refreshArrays(TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
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
	}

	private static int getFirstMatch(int last0, int last1, int last2, int current0, int current1,
			int current2) {
		for (int i0 = 0; i0 < 3; i0++) {
			int v0 = iterateThru(i0, last0, last1, last2);
			for (int i1 = 0; i1 < 3; i1++) {
				int v1 = iterateThru(i1, current0, current1, current2);
				if (v0 == v1)
					return v0;
			}
		}
		return -1;
	}
	
	private static int getSecondMatch(int last0, int last1, int last2, int current0, int current1,
			int current2, int firstMatch) {
		for (int i0 = 0; i0 < 3; i0++) {
			int v0 = iterateThru(i0, last0, last1, last2);
			for (int i1 = 0; i1 < 3; i1++) {
				int v1 = iterateThru(i1, current0, current1, current2);
				if (v0 == v1 && firstMatch != v0)
					return v0;
			}
		}
		return -1;
	}
	
	private static int getLastMiss(int last0, int last1, int last2, int current0, int current1,
			int current2, int firstMatch, int secondMatch) {
		for (int i0 = 0; i0 < 3; i0++) {
			int v0 = iterateThru(i0, last0, last1, last2);
			if (v0 != firstMatch && v0 != secondMatch)
					return v0;
		}
		return -1;
	}
	
	private static int getCurrentMiss(int last0, int last1, int last2, int current0, int current1,
			int current2, int firstMatch, int secondMatch) {
		for (int i0 = 0; i0 < 3; i0++) {
			int v0 = iterateThru(i0, current0, current1, current2);
			if (v0 != firstMatch && v0 != secondMatch)
					return v0;
		}
		return -1;
	}

	
	private static int iterateThru(int index, int v0, int v1, int v2) {
		switch (index) {
		case 0:
			return v0;
		case 1:
			return v1;
		case 2:
			return v2;
		}
		throw new Error();
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
	
	private static void addQuad(BufferBuilder worldRendererIn, TerrainPoint3D tp1,
			TerrainPoint3D tp2, TerrainPoint3D tp3, TerrainPoint3D tp4) {
		addTriangle(worldRendererIn, tp3, tp2, tp1);
		addTriangle(worldRendererIn, tp4, tp3, tp1);
	}

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
		int color = cp.blockColors.getBlockColor(point.blockState, point.biome, pos, n1.getY());
		float red = (color >> 16 & 255) / 256f;
		float green = (color >> 8 & 255) / 256f;
		float blue = (color & 255) / 256f;
		int skyLight = 0;
		if (point.skyLight == -1)
			skyLight = TerrainUtil.getSubstituteSkyLightValue(n1.getY());
		else
			skyLight = point.skyLight * 16;
		worldRendererIn.pos(bx, by, bz).tex(u, v).lightmap(skyLight, point.blockLight * 16)
				.color(red, green, blue, 1.0f).normal(n1.getX(), n1.getY(), n1.getZ()).endVertex();
	}
}
