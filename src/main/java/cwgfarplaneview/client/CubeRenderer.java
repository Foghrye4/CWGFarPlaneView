package cwgfarplaneview.client;

import static cwgfarplaneview.util.AddressUtil.MESH_SIZE_BIT_BLOCKS;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.ClientProxy;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.util.Vec3f;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CubeRenderer {

	private final CubeRendererEdge[] edges = new CubeRendererEdge[12]; 
	private final TerrainPoint3D[] allPoints = new TerrainPoint3D[8];
	private final TerrainPoint3D[][] pointsByFacing = new TerrainPoint3D[EnumFacing.values().length][4];
	private final TerrainPoint3D[][] pointsByNeighbor = new TerrainPoint3D[8][0];
	public CubeRenderer(TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		allPoints[0] = tp000;
		allPoints[1] = tp100;
		allPoints[2] = tp010;
		allPoints[3] = tp001;
		allPoints[4] = tp110;
		allPoints[5] = tp101;
		allPoints[6] = tp011;
		allPoints[7] = tp111;
		pointsByNeighbor[0] = new TerrainPoint3D[] {tp010,tp001,tp100};//Order matters. See "allPoints" above
		pointsByNeighbor[1] = new TerrainPoint3D[] {tp110,tp000,tp101};
		pointsByNeighbor[2] = new TerrainPoint3D[] {tp011,tp000,tp110};
		pointsByNeighbor[3] = new TerrainPoint3D[] {tp011,tp101,tp000};
		pointsByNeighbor[4] = new TerrainPoint3D[] {tp010,tp100,tp111};
		pointsByNeighbor[5] = new TerrainPoint3D[] {tp111,tp100,tp001};
		pointsByNeighbor[6] = new TerrainPoint3D[] {tp010,tp001,tp111};
		pointsByNeighbor[7] = new TerrainPoint3D[] {tp011,tp110,tp101};
		edges[0] = new CubeRendererEdge(new TerrainPoint3D[] {tp000,tp100},new TerrainPoint3D[] {tp010,tp001,tp101,tp110});
		edges[1] = new CubeRendererEdge(new TerrainPoint3D[] {tp000,tp010},new TerrainPoint3D[] {tp110,tp011,tp001,tp100});
		edges[2] = new CubeRendererEdge(new TerrainPoint3D[] {tp000,tp001},new TerrainPoint3D[] {tp011,tp101,tp100,tp010});
		edges[3] = new CubeRendererEdge(new TerrainPoint3D[] {tp100,tp101},new TerrainPoint3D[] {tp111,tp110,tp000,tp001});
		edges[4] = new CubeRendererEdge(new TerrainPoint3D[] {tp001,tp101},new TerrainPoint3D[] {tp011,tp111,tp100,tp000});
		edges[5] = new CubeRendererEdge(new TerrainPoint3D[] {tp010,tp110},new TerrainPoint3D[] {tp011,tp000,tp100,tp111});
		edges[6] = new CubeRendererEdge(new TerrainPoint3D[] {tp010,tp011},new TerrainPoint3D[] {tp110,tp111,tp001,tp000});
		edges[7] = new CubeRendererEdge(new TerrainPoint3D[] {tp011,tp111},new TerrainPoint3D[] {tp010,tp110,tp101,tp001});
		edges[8] = new CubeRendererEdge(new TerrainPoint3D[] {tp111,tp110},new TerrainPoint3D[] {tp011,tp010,tp100,tp101});
		edges[9] = new CubeRendererEdge(new TerrainPoint3D[] {tp111,tp101},new TerrainPoint3D[] {tp011,tp110,tp100,tp001});
		edges[10] = new CubeRendererEdge(new TerrainPoint3D[] {tp100,tp110},new TerrainPoint3D[] {tp010,tp000,tp101,tp111});
		edges[11] = new CubeRendererEdge(new TerrainPoint3D[] {tp011,tp001},new TerrainPoint3D[] {tp010,tp111,tp101,tp000});
		
		for(EnumFacing facing :EnumFacing.values()) {
			switch(facing) {
			case DOWN:
				pointsByFacing[facing.ordinal()][0] = tp000;
				pointsByFacing[facing.ordinal()][1] = tp001;
				pointsByFacing[facing.ordinal()][2] = tp101;
				pointsByFacing[facing.ordinal()][3] = tp100;
				break;
			case EAST:
				pointsByFacing[facing.ordinal()][0] = tp100;
				pointsByFacing[facing.ordinal()][1] = tp101;
				pointsByFacing[facing.ordinal()][2] = tp111;
				pointsByFacing[facing.ordinal()][3] = tp110;
				break;
			case NORTH:
				pointsByFacing[facing.ordinal()][0] = tp000;
				pointsByFacing[facing.ordinal()][1] = tp100;
				pointsByFacing[facing.ordinal()][2] = tp110;
				pointsByFacing[facing.ordinal()][3] = tp010;
				break;
			case SOUTH:
				pointsByFacing[facing.ordinal()][0] = tp001;
				pointsByFacing[facing.ordinal()][1] = tp011;
				pointsByFacing[facing.ordinal()][2] = tp111;
				pointsByFacing[facing.ordinal()][3] = tp101;
				break;
			case UP:
				pointsByFacing[facing.ordinal()][0] = tp010;
				pointsByFacing[facing.ordinal()][1] = tp110;
				pointsByFacing[facing.ordinal()][2] = tp111;
				pointsByFacing[facing.ordinal()][3] = tp011;
				break;
			case WEST:
				pointsByFacing[facing.ordinal()][0] = tp000;
				pointsByFacing[facing.ordinal()][1] = tp001;
				pointsByFacing[facing.ordinal()][2] = tp011;
				pointsByFacing[facing.ordinal()][3] = tp010;
				break;
			default:
				break;
			}
		}
	}
	
	public void render(BufferBuilder worldRendererIn) {
		int visible = countVisiblePoints();
		if (visible <= 2) {
			return;
		}
		this.renderAllVisibleTriangles(worldRendererIn);
		if (visible == 8) {
			return;
		} else if (visible == 7) {
			for(int i=0;i<allPoints.length;i++) {
				if(TerrainUtil.isAirOrWater(allPoints[i].blockState)) {
					this.renderTriangle(worldRendererIn, pointsByNeighbor[i][0], pointsByNeighbor[i][1], pointsByNeighbor[i][2]);
				}
			}
		} else if (visible == 6) {
			if(isCubeHaveInvisibleEdge()) {
				for(CubeRendererEdge edge:edges) {
					if(edge.haveNoVisiblePoints()) {
						this.renderQuad(worldRendererIn, edge.adjoiningTPs[0], edge.adjoiningTPs[1], edge.adjoiningTPs[2], edge.adjoiningTPs[3]);
					}
				}
			}
			else {
				for(int i=0;i<allPoints.length;i++) {
					if(TerrainUtil.isAirOrWater(allPoints[i].blockState)) {
						this.renderTriangle(worldRendererIn, pointsByNeighbor[i][0], pointsByNeighbor[i][1], pointsByNeighbor[i][2]);
					}
				}
			}
		} else if (visible == 5) {
			for (EnumFacing facing : EnumFacing.values()) {
				if (this.isSideFullyRenderable(facing)) {
					
				}
			}
			
		}
	}
	
	private void renderAllVisibleTriangles(BufferBuilder worldRendererIn) {
		for (EnumFacing facing : EnumFacing.values()) {
			if (this.isSideFullyRenderable(facing)) {
				TerrainPoint3D[] face = pointsByFacing[facing.ordinal()];
				renderQuad(worldRendererIn, face[0], face[1], face[2], face[3]);
			} else {
				this.renderFaceWithOneMissingPoint(worldRendererIn, facing);
			}
		}
	}
	
	private boolean isCubeHaveInvisibleEdge() {
		for(int i=0;i<allPoints.length;i++) {
			if(TerrainUtil.isAirOrWater(allPoints[i].blockState)) {
				for(int i1=0;i1<3;i1++) {
					if(TerrainUtil.isAirOrWater(pointsByNeighbor[i][i1].blockState)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isSideFullyRenderable(EnumFacing facing) {
		for(int i=0;i<4;i++) {
			if(TerrainUtil.isAirOrWater(pointsByFacing[facing.ordinal()][i].blockState)) {
				return false;
			}
		}
		return true;
	}
	
	private int countVisiblePoints() {
		int visible = 0;
		for(TerrainPoint3D point:allPoints) {
			if(!TerrainUtil.isAirOrWater(point.blockState)) {
				visible++;
			}
		}
		return visible;
	}
	
	private void renderQuad(BufferBuilder worldRendererIn, TerrainPoint3D tp00, TerrainPoint3D tp01, TerrainPoint3D tp11, TerrainPoint3D tp10) {
		this.renderTriangle(worldRendererIn, tp00, tp01, tp11);
		this.renderTriangle(worldRendererIn, tp11, tp10, tp00);
	}
	
	private void renderFaceWithOneMissingPoint(BufferBuilder worldRendererIn, EnumFacing facing) {
		TerrainPoint3D[] face = pointsByFacing[facing.ordinal()];
		int shift = 0;
		TerrainPoint3D tp0 = TerrainUtil.isAirOrWater(face[shift].blockState)?face[++shift]:face[shift];
		TerrainPoint3D tp1 = TerrainUtil.isAirOrWater(face[++shift].blockState)?face[++shift]:face[shift];
		if (shift == 3 || shift == 2 && TerrainUtil.isAirOrWater(face[shift + 1].blockState))
			return;
		TerrainPoint3D tp2 = TerrainUtil.isAirOrWater(face[++shift].blockState)?face[++shift]:face[shift];
		this.renderTriangle(worldRendererIn, tp0, tp1, tp2);
	}	
	
	private void renderTriangle(BufferBuilder worldRendererIn, TerrainPoint3D tp00, TerrainPoint3D tp01, TerrainPoint3D tp11) {
		Vec3f n1 = TerrainUtil.calculateNormal(tp11, tp01, tp00);
		this.addVector(worldRendererIn, tp00, n1, 0.0f, 0.0f);
		this.addVector(worldRendererIn, tp01, n1, 1.0f, 0.0f);
		this.addVector(worldRendererIn, tp11, n1, 1.0f, 1.0f);
	}


	private void addVector(BufferBuilder worldRendererIn, TerrainPoint3D point, Vec3f n1, float u, float v) {
		int bx = point.cubeX << MESH_SIZE_BIT_BLOCKS;
		int by = point.cubeY << MESH_SIZE_BIT_BLOCKS;
		int bz = point.cubeZ << MESH_SIZE_BIT_BLOCKS;
		BlockPos pos = new BlockPos(bx, by, bz);
		ClientProxy cp = (ClientProxy) CWGFarPlaneViewMod.proxy;
		int color = cp.blockColors.getBlockColor(point.blockState, point.biome, pos);
		float red = (color >> 16 & 255) / 256f;
		float green = (color >> 8 & 255) / 256f;
		float blue = (color & 255) / 256f;
		worldRendererIn.pos(bx, by, bz).tex(u, v).lightmap(240, 0).color(red, green, blue, 1.0f).normal(n1.getX(), n1.getY(), n1.getZ()).endVertex();
	}

}
