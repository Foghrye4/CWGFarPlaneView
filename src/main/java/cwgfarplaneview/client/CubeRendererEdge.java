package cwgfarplaneview.client;

import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;

public class CubeRendererEdge {
	final TerrainPoint3D[] edgeTPs;
	final TerrainPoint3D[] adjoiningTPs;
	
	public CubeRendererEdge(TerrainPoint3D[] edgeTPsIn, TerrainPoint3D[] adjoiningTPsIn) {
		edgeTPs = edgeTPsIn;
		adjoiningTPs = adjoiningTPsIn;
	}

	public boolean haveNoVisiblePoints() {
		return TerrainUtil.isAirOrWater(edgeTPs[0].blockState) && TerrainUtil.isAirOrWater(edgeTPs[1].blockState);
	}
}
