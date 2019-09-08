package cwgfarplaneview.util;

public class TerrainConfig {
	public static TerrainConfig FLAT = new TerrainConfig();
	public static TerrainConfig VOLUMETRIC_HORIZONTAL = new TerrainConfig();
	public static TerrainConfig VOLUMETRIC_VERTICAL = new TerrainConfig();
	
	public static float closePlane = 64.0f;
	public static int meshSizeBitChunks = 0;
	public static int meshSizeBitBlocks = meshSizeBitChunks + 4;
	
	public int maxUpdateDistanceCells = 92;
	public int maxUpdateDistanceChunks = maxUpdateDistanceCells << meshSizeBitChunks;
	public int horizontDistanceChunks = maxUpdateDistanceChunks - (32 << meshSizeBitChunks);
	public int horizontDistanceBlocks = horizontDistanceChunks << 4;
	
	public float farPlane = horizontDistanceBlocks * 2;
	
	public void setMaxUpdateDistance(int maxUpdateDistance) {
		maxUpdateDistanceCells = maxUpdateDistance;
		maxUpdateDistanceChunks = maxUpdateDistanceCells << meshSizeBitChunks;
		horizontDistanceChunks = maxUpdateDistanceChunks - (32 << meshSizeBitChunks);
		horizontDistanceBlocks = horizontDistanceChunks << 4;
		farPlane = horizontDistanceBlocks * 2;
	}

	public static void setClosePlaneRange(float closePlaneDistance) {
		closePlane = closePlaneDistance;
	}
	
	public static float getFarClippingRange() {
		return Math.max(FLAT.farPlane,Math.max(VOLUMETRIC_HORIZONTAL.farPlane,VOLUMETRIC_VERTICAL.farPlane));
	}
}
