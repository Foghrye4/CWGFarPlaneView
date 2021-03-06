package cwgfarplaneview.util;

public class TerrainConfig {
	public static TerrainConfig FLAT = new TerrainConfig();
	public static TerrainConfig VOLUMETRIC_HORIZONTAL = new TerrainConfig();
	public static TerrainConfig VOLUMETRIC_VERTICAL = new TerrainConfig();
	
	public static float closePlane = 64.0f;
	public static int MESH_SIZE_BIT_CHUNKS = 0;
	public static int MESH_SIZE_BIT_BLOCKS = MESH_SIZE_BIT_CHUNKS + 4;
	public static int CUBE_SIZE_BIT_MESH = 4;
	
	public int maxUpdateDistanceCells = 92;
	public int maxUpdateDistanceChunks = maxUpdateDistanceCells << MESH_SIZE_BIT_CHUNKS;
	public int horizontDistanceChunks = maxUpdateDistanceChunks - (32 << MESH_SIZE_BIT_CHUNKS);
	public int horizontDistanceBlocks = horizontDistanceChunks << 4;
	
	public float farPlane = horizontDistanceBlocks * 2;
	
	public void setMaxUpdateDistance(int maxUpdateDistance) {
		maxUpdateDistanceCells = maxUpdateDistance << CUBE_SIZE_BIT_MESH;
		maxUpdateDistanceChunks = maxUpdateDistanceCells << MESH_SIZE_BIT_CHUNKS;
		horizontDistanceChunks = maxUpdateDistanceChunks - (32 << MESH_SIZE_BIT_CHUNKS);
		horizontDistanceBlocks = horizontDistanceChunks << 4;
		farPlane = horizontDistanceBlocks * 2;
	}

	public static void setClosePlaneRange(float closePlaneDistance) {
		closePlane = closePlaneDistance;
	}
	
	public static float getFarClippingRange() {
		return Math.max(FLAT.farPlane,Math.max(VOLUMETRIC_HORIZONTAL.farPlane,VOLUMETRIC_VERTICAL.farPlane));
	}
	
	public static int meshToCube(int mesh) {
		return mesh >> CUBE_SIZE_BIT_MESH;
	}
}
