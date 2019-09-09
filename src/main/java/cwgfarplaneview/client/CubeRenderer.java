package cwgfarplaneview.client;

import static cwgfarplaneview.util.TerrainConfig.MESH_SIZE_BIT_BLOCKS;

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
	private static MutableWeightedNormal[] allNormals = new MutableWeightedNormal[8];
	private static TerrainPoint3D[][] neighbors = new TerrainPoint3D[8][3];
	public static final boolean DEBUG_MODE = false;
	
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
	
	public static void renderCube(BufferBuilder worldRendererIn, TerrainPoint3D tp000, 
			TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001, 
			TerrainPoint3D tp110, TerrainPoint3D tp101,	TerrainPoint3D tp011, 
			TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111,	boolean calculateNormals) {
		int amountPointsMissing = 0;
		if (tp000 == null || !tp000.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp000);
		if (tp100 == null || !tp100.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp100);
		if (tp010 == null || !tp010.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp010);
		if (tp001 == null || !tp001.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp001);
		if (tp110 == null || !tp110.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp110);
		if (tp101 == null || !tp101.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp101);
		if (tp011 == null || !tp011.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp011);
		if (tp111 == null || !tp111.isVisible())
			amountPointsMissing++;
		else if(DEBUG_MODE)
			addYellowDebugVectors(worldRendererIn,tp111);
		switch(amountPointsMissing) {
		case 0:
		case 6:
		case 7:
		case 8:
			return;
		case 2:
			renderCubeWithTwoPointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
			return;
		case 3:
			renderCubeWithThreePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
			return;
		case 4:
			renderCubeWithFourPointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
			return;
		default: //1,5
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
			return;
		}
	}
	
	private static void renderCubeWithTwoPointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111,	boolean calculateNormals) {
		boolean n000 = tp000 == null || !tp000.isVisible();
		boolean n100 = tp100 == null || !tp100.isVisible();
		boolean n010 = tp010 == null || !tp010.isVisible();
		boolean n001 = tp001 == null || !tp001.isVisible();
		boolean n110 = tp110 == null || !tp110.isVisible();
		boolean n101 = tp101 == null || !tp101.isVisible();
		boolean n011 = tp011 == null || !tp011.isVisible();
		boolean n111 = tp111 == null || !tp111.isVisible();
		if (n000 && n100) {
			addQuad(worldRendererIn, tp001, tp101, tp110, tp010, nw001, nw101, nw110, nw010, calculateNormals);
		}
		else if (n000 && n010) {
			addQuad(worldRendererIn, tp011, tp001, tp100, tp110, nw011, nw001, nw100, nw110, calculateNormals);
		}
		else if (n000 && n001) {
			addQuad(worldRendererIn, tp011, tp101, tp100, tp010, nw011, nw101, nw100, nw010, calculateNormals);
		}
		else if (n111 && n101) {
			addQuad(worldRendererIn, tp011, tp110, tp100, tp001, nw011, nw110, nw100, nw001, calculateNormals);
		}
		else if (n111 && n011) {
			addQuad(worldRendererIn, tp010, tp110, tp101, tp001, nw010, nw110, nw101, nw001, calculateNormals);
		}
		else if (n111 && n110) {
			addQuad(worldRendererIn, tp011, tp010, tp100, tp101, nw011, nw010, nw100, nw101, calculateNormals);
		}
		else if (n010 && n110) {
			addQuad(worldRendererIn, tp011, tp000, tp100, tp111, nw011, nw000, nw100, nw111, calculateNormals);
		}
		else if (n100 && n110) {
			addQuad(worldRendererIn, tp111, tp010, tp000, tp101, nw111, nw010, nw000, nw101, calculateNormals);
		}
		else if (n100 && n101) {
			addQuad(worldRendererIn, tp111, tp110, tp000, tp001, nw111, nw110, nw000, nw001, calculateNormals);
		}
		else if (n001 && n101) {
			addQuad(worldRendererIn, tp011, tp111, tp100, tp000, nw011, nw111, nw100, nw000, calculateNormals);
		}
		else if (n001 && n011) {
			addQuad(worldRendererIn, tp010, tp111, tp101, tp000, nw010, nw111, nw101, nw000, calculateNormals);
		}
		else if (n011 && n010) {
			addQuad(worldRendererIn, tp111, tp001, tp000, tp110, nw111, nw001, nw000, nw110, calculateNormals);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
		}
	}
	
	private static void renderCubeWithThreePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111,	boolean calculateNormals) {
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
			addQuad(worldRendererIn, tp001, tp010, tp100, tp101, nw001, nw010, nw100, nw101, calculateNormals);
		}
		else if (n010 && n111 && n011) {
			addQuad(worldRendererIn, tp110, tp101, tp001, tp000, nw110, nw101, nw001, nw000, calculateNormals);
		}
		else if (n010 && n110 && n011) {
			addQuad(worldRendererIn, tp111, tp001, tp000, tp100, nw111, nw001, nw000, nw100, calculateNormals);
		}
		else if (n010 && n110 && n111) {
			addQuad(worldRendererIn, tp011, tp000, tp100, tp101, nw011, nw000, nw100, nw101, calculateNormals);
		}
		// Positive X
		else if (n110 && n100 && n101) {
			addQuad(worldRendererIn, tp111, tp010, tp000, tp001, nw111, nw010, nw000, nw001, calculateNormals);
		}
		else if (n111 && n100 && n101) {
			addQuad(worldRendererIn, tp110, tp000, tp001, tp011, nw110, nw000, nw001, nw011, calculateNormals);
		}
		else if (n111 && n110 && n101) {
			addQuad(worldRendererIn, tp100, tp001, tp011, tp010, nw100, nw001, nw011, nw010, calculateNormals);
		}
		else if (n111 && n110 && n100) { //101
			addQuad(worldRendererIn, tp101, tp011, tp010, tp000, nw101, nw011, nw010, nw000, calculateNormals);
		}
		// Positive Z
		else if (n111 && n101 && n001) { //011
			addQuad(worldRendererIn, tp011, tp110, tp100, tp000, nw011, nw110, nw100, nw000, calculateNormals);
		}
		else if (n011 && n101 && n001) { //111
			addQuad(worldRendererIn, tp111, tp100, tp000, tp010, nw111, nw100, nw000, nw010, calculateNormals);
		}
		else if (n011 && n111 && n001) { //101
			addQuad(worldRendererIn, tp101, tp000, tp010, tp110, nw101, nw000, nw010, nw110, calculateNormals);
		}
		else if (n011 && n111 && n101) { //001
			addQuad(worldRendererIn, tp001, tp010, tp110, tp100, nw001, nw010, nw110, nw100, calculateNormals);
		}
		// Bottom side
		else if (n100 && n101 && n001) { //000
			addQuad(worldRendererIn, tp000, tp011, tp111, tp110, nw000, nw011, nw111, nw110, calculateNormals);
		}
		else if (n000 && n101 && n001) { //100
			addQuad(worldRendererIn, tp100, tp010, tp011, tp111, nw100, nw010, nw011, nw111, calculateNormals);
		}
		else if (n000 && n100 && n001) { //101
			addQuad(worldRendererIn, tp101, tp110, tp010, tp011, nw101, nw110, nw010, nw011, calculateNormals);
		}
		else if (n000 && n100 && n101) { //001
			addQuad(worldRendererIn, tp001, tp111, tp110, tp010, nw001, nw111, nw110, nw010, calculateNormals);
		}
		// Negative X
		else if (n010 && n000 && n001) { //011
			addQuad(worldRendererIn, tp011, tp101, tp100, tp110, nw011, nw101, nw100, nw110, calculateNormals);
		}
		else if (n011 && n000 && n001) { //010
			addQuad(worldRendererIn, tp010, tp111, tp101, tp100, nw010, nw111, nw101, nw100, calculateNormals);
		}
		else if (n011 && n010 && n001) { //000
			addQuad(worldRendererIn, tp000, tp110, tp111, tp101, nw000, nw110, nw111, nw101, calculateNormals);
		}
		else if (n011 && n010 && n000) { //001
			addQuad(worldRendererIn, tp001, tp100, tp110, tp111, nw001, nw100, nw110, nw111, calculateNormals);
		}
		// Negative Z
		else if (n110 && n100 && n000) { //010
			addQuad(worldRendererIn, tp010, tp001, tp101, tp111, nw010, nw001, nw101, nw111, calculateNormals);
		}
		else if (n010 && n100 && n000) { //110
			addQuad(worldRendererIn, tp110, tp011, tp001, tp101, nw110, nw011, nw001, nw101, calculateNormals);
		}
		else if (n010 && n110 && n000) { //100
			addQuad(worldRendererIn, tp100, tp111, tp011, tp001, nw100, nw111, nw011, nw001, calculateNormals);
		}
		else if (n010 && n110 && n100) { //000
			addQuad(worldRendererIn, tp000, tp101, tp111, tp011, nw000, nw101, nw111, nw011, calculateNormals);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
		}
	}
	
	private static void renderCubeWithFourPointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111,	boolean calculateNormals) {
		boolean n000 = tp000 == null || !tp000.isVisible();
		boolean n100 = tp100 == null || !tp100.isVisible();
		boolean n010 = tp010 == null || !tp010.isVisible();
		boolean n001 = tp001 == null || !tp001.isVisible();
		boolean n110 = tp110 == null || !tp110.isVisible();
		boolean n101 = tp101 == null || !tp101.isVisible();
		boolean n011 = tp011 == null || !tp011.isVisible();
		boolean n111 = tp111 == null || !tp111.isVisible();
		if (n010 && n110 && n111 && n011) { //Top side
			addQuad(worldRendererIn, tp000, tp100, tp101, tp001, nw000, nw100, nw101, nw001, calculateNormals);
		}
		else if (n000 && n100 && n101 && n001) {
			addQuad(worldRendererIn, tp010, tp011, tp111, tp110, nw010, nw011, nw111, nw110, calculateNormals);
		}
		else if (n111 && n110 && n100 && n101) {
			addQuad(worldRendererIn, tp011, tp010, tp000, tp001, nw011, nw010, nw000, nw001, calculateNormals);
		}
		else if (n011 && n010 && n000 && n001) {
			addQuad(worldRendererIn, tp111, tp101, tp100, tp110, nw111, nw101, nw100, nw110, calculateNormals);
		}
		else if (n011 && n111 && n101 && n001) {
			addQuad(worldRendererIn, tp010, tp110, tp100, tp000, nw010, nw110, nw100, nw000, calculateNormals);
		}
		else if (n010 && n110 && n100 && n000) {
			addQuad(worldRendererIn, tp011, tp001, tp101, tp111, nw011, nw001, nw101, nw111, calculateNormals);
		}
		else {
			renderCubeWithSomePointsMissing(worldRendererIn, tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111, calculateNormals);
		}
	}
	
	private static void renderCubeWithSomePointsMissing(BufferBuilder worldRendererIn, TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111,	boolean calculateNormals) {
		refreshArrays(tp000, tp100, tp010, tp001, tp110, tp101, tp011, tp111, nw000, nw100, nw010, nw001, nw110, nw101, nw011, nw111);
		int lastRenderedI0 = -1;
		int lastRenderedI1 = -1;
		int lastRenderedI2 = -1;
		for(int i0=0;i0<8;i0++) {
			for(int i1=i0+1;i1<8;i1++) {
				next_triangle:for(int i2=i1+1;i2<8;i2++) {
					TerrainPoint3D tp1 = allPoints[i0];
					TerrainPoint3D tp2 = allPoints[i1];
					TerrainPoint3D tp3 = allPoints[i2];
					MutableWeightedNormal nw1 = allNormals[i0];
					MutableWeightedNormal nw2 = allNormals[i1];
					MutableWeightedNormal nw3 = allNormals[i2];
					if (tp1 == null || tp2 == null || tp3 == null)
						continue;
					if (!tp1.isVisible() || !tp2.isVisible() || !tp3.isVisible())
						continue;
					if(isAllNeighborsVisible(i0) || isAllNeighborsVisible(i1) || isAllNeighborsVisible(i2))
						continue;
					
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
					
					addTriangle(worldRendererIn, tp1, tp2, tp3, nw1, nw2, nw3, calculateNormals);
					addTriangle(worldRendererIn, tp3, tp2, tp1, nw3, nw2, nw1, calculateNormals);
				}
			}
		}
	}
	
	private static void refreshArrays(TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111, MutableWeightedNormal nw000, 
			MutableWeightedNormal nw100, MutableWeightedNormal nw010, MutableWeightedNormal nw001, 
			MutableWeightedNormal nw110, MutableWeightedNormal nw101, MutableWeightedNormal nw011, 
			MutableWeightedNormal nw111) {
		allPoints[0] = tp000;
		allPoints[1] = tp100;
		allPoints[2] = tp010;
		allPoints[3] = tp001;
		allPoints[4] = tp110;
		allPoints[5] = tp101;
		allPoints[6] = tp011;
		allPoints[7] = tp111;
		
		allNormals[0] = nw000;
		allNormals[1] = nw100;
		allNormals[2] = nw010;
		allNormals[3] = nw001;
		allNormals[4] = nw110;
		allNormals[5] = nw101;
		allNormals[6] = nw011;
		allNormals[7] = nw111;
		
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
			TerrainPoint3D tp2, TerrainPoint3D tp3, TerrainPoint3D tp4, MutableWeightedNormal nw1, 
			MutableWeightedNormal nw2, MutableWeightedNormal nw3, MutableWeightedNormal nw4, boolean calculateNormal) {
		addTriangle(worldRendererIn, tp3, tp2, tp1, nw3, nw2, nw1, calculateNormal);
		addTriangle(worldRendererIn, tp4, tp3, tp1, nw4, nw3, nw1, calculateNormal);
	}

	private static void addTriangle(BufferBuilder worldRendererIn, TerrainPoint3D tp1,
			TerrainPoint3D tp2, TerrainPoint3D tp3, MutableWeightedNormal nw1, 
			MutableWeightedNormal nw2, MutableWeightedNormal nw3, boolean calculateNormal) {
		Vec3f n = TerrainUtil.calculateNormal(tp3, tp2, tp1);
		if(calculateNormal) {
			nw1.add(n);
			nw2.add(n);
			nw3.add(n);
		}
		else {
			final float ap = 0.8f;
			final float sp = 1.0f-ap;
			addVector(worldRendererIn, tp1, nw1.x * ap + n.getX() * sp, nw1.y * ap + n.getY() * sp,	nw1.z * ap + n.getZ() * sp, 1.0f, 1.0f);
			addVector(worldRendererIn, tp2, nw2.x * ap + n.getX() * sp, nw2.y * ap + n.getY() * sp,	nw2.z * ap + n.getZ() * sp, 0.0f, 1.0f);
			addVector(worldRendererIn, tp3, nw3.x * ap + n.getX() * sp, nw3.y * ap + n.getY() * sp, nw3.z * ap + n.getZ() * sp, 0.0f, 0.0f);
			if (DEBUG_MODE) {
				addDebugVectors(worldRendererIn, tp1, nw1);
				addDebugVectors(worldRendererIn, tp2, nw2);
				addDebugVectors(worldRendererIn, tp3, nw3);
			}
		}
	}
	
	private static void addVector(BufferBuilder worldRendererIn, TerrainPoint3D point, float nx,float ny, float nz, float u, float v) {
		int bx = (point.meshX << MESH_SIZE_BIT_BLOCKS) + point.localX;
		int by = (point.meshY << MESH_SIZE_BIT_BLOCKS) + point.localY;
		int bz = (point.meshZ << MESH_SIZE_BIT_BLOCKS) + point.localZ;
		if (DEBUG_MODE) {
			bx = point.meshX << MESH_SIZE_BIT_BLOCKS;
			by = point.meshY << MESH_SIZE_BIT_BLOCKS;
			bz = point.meshZ << MESH_SIZE_BIT_BLOCKS;
		}

		BlockPos pos = new BlockPos(bx, by, bz);
		ClientProxy cp = (ClientProxy) CWGFarPlaneViewMod.proxy;
		int color = cp.blockColors.getBlockColor(point.blockState, point.biome, pos, ny);
		float red = (color >> 16 & 255) / 256f;
		float green = (color >> 8 & 255) / 256f;
		float blue = (color & 255) / 256f;
		int skyLight = 0;
		if (point.skyLight == -1)
			skyLight = TerrainUtil.getSubstituteSkyLightValue(ny);
		else
			skyLight = point.skyLight * 16;
		worldRendererIn.pos(bx, by, bz).tex(u, v).lightmap(skyLight, point.blockLight * 16)
				.color(red, green, blue, 1.0f).normal(nx, ny, nz).endVertex();
	}
	
	private static void addDebugVectors(BufferBuilder worldRendererIn, TerrainPoint3D point, MutableWeightedNormal n1) {
		int bx = point.meshX << MESH_SIZE_BIT_BLOCKS;
		int by = point.meshY << MESH_SIZE_BIT_BLOCKS;
		int bz = point.meshZ << MESH_SIZE_BIT_BLOCKS;
		addRedVector(worldRendererIn, bx + 0.5, by, bz, 0f, 0f);
		addRedVector(worldRendererIn, bx - 0.5, by, bz, 0f, 1.0f);
		addRedVector(worldRendererIn, bx + n1.x * 16, by + n1.y * 16, bz + n1.z * 16, 1.0f, 1.0f);

		addRedVector(worldRendererIn, bx, by, bz + 0.5, 0f, 0f);
		addRedVector(worldRendererIn, bx, by, bz - 0.5, 1.0f, 1.0f);
		addRedVector(worldRendererIn, bx + n1.x * 16, by + n1.y * 16, bz + n1.z * 16, 0f, 1.0f);
	}
	
	private static void addYellowDebugVectors(BufferBuilder worldRendererIn, TerrainPoint3D point) {
		int bx = point.meshX << MESH_SIZE_BIT_BLOCKS;
		int by = point.meshY << MESH_SIZE_BIT_BLOCKS;
		int bz = point.meshZ << MESH_SIZE_BIT_BLOCKS;
		addYellowVector(worldRendererIn, bx + 0.5, by, bz - 0.5, 0f, 0f);
		addYellowVector(worldRendererIn, bx + 0.5, by, bz + 0.5, 0f, 1.0f);
		addYellowVector(worldRendererIn, bx - 0.5, by, bz - 0.5, 1.0f, 1.0f);

		addYellowVector(worldRendererIn, bx - 0.5, by, bz - 0.5, 1.0f, 1.0f);
		addYellowVector(worldRendererIn, bx + 0.5, by, bz + 0.5, 0f, 1.0f);
		addYellowVector(worldRendererIn, bx - 0.5, by, bz + 0.5, 0f, 0f);
		
		
		addYellowVector(worldRendererIn, bx + 0.5, by, bz - 0.5, 0f, 0f);
		addYellowVector(worldRendererIn, bx - 0.5, by, bz - 0.5, 1.0f, 1.0f);
		addYellowVector(worldRendererIn, bx + 0.5, by, bz + 0.5, 0f, 1.0f);

		addYellowVector(worldRendererIn, bx + 0.5, by, bz + 0.5, 0f, 1.0f);
		addYellowVector(worldRendererIn, bx - 0.5, by, bz - 0.5, 1.0f, 1.0f);
		addYellowVector(worldRendererIn, bx - 0.5, by, bz + 0.5, 0f, 0f);

	}

	private static void addRedVector(BufferBuilder worldRendererIn, double x, double y, double z, float u, float v) {
		worldRendererIn.pos(x, y, z).tex(u, v).lightmap(240, 240)
		.color(1.0f, 0.0f, 0.0f, 1.0f).normal(0.0f, 1.0f, 0.0f).endVertex();
	}
	
	private static void addYellowVector(BufferBuilder worldRendererIn, double x, double y, double z, float u, float v) {
		worldRendererIn.pos(x, y, z).tex(u, v).lightmap(240, 240)
		.color(1.0f, 1.0f, 0.0f, 1.0f).normal(0.0f, 1.0f, 0.0f).endVertex();
	}
}
