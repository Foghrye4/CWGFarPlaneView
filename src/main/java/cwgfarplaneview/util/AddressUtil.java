package cwgfarplaneview.util;

import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class AddressUtil {
	public static int MESH_SIZE_BIT_CHUNKS = 0;
	public static int MESH_SIZE_BIT_BLOCKS = MESH_SIZE_BIT_CHUNKS + 4;
	public static int MAX_UPDATE_DISTANCE_CELLS = 92;
	public static int MAX_UPDATE_DISTANCE_CHUNKS = MAX_UPDATE_DISTANCE_CELLS << MESH_SIZE_BIT_CHUNKS;
	public static int HORIZONT_DISTANCE_CHUNKS = MAX_UPDATE_DISTANCE_CHUNKS - (32 << MESH_SIZE_BIT_CHUNKS);
	public static int HORIZONT_DISTANCE_BLOCKS = HORIZONT_DISTANCE_CHUNKS << 4;
	public static int HORIZONT_DISTANCE_SQ = HORIZONT_DISTANCE_CHUNKS * HORIZONT_DISTANCE_CHUNKS;
	
	public static float CLOSE_PLANE = 64.0f;
	public static float FAR_PLANE = HORIZONT_DISTANCE_BLOCKS * 2;
	
	public static void meshCoordsToBlockPos(MutableBlockPos pos, int meshX, int meshZ) {
		pos.setPos(meshX << MESH_SIZE_BIT_BLOCKS, 64, meshZ << MESH_SIZE_BIT_BLOCKS);
	}
	
	public static void setMaxUpdateDistance(int maxUpdateDistance) {
		MAX_UPDATE_DISTANCE_CELLS = maxUpdateDistance;
		MAX_UPDATE_DISTANCE_CHUNKS = MAX_UPDATE_DISTANCE_CELLS << MESH_SIZE_BIT_CHUNKS;
		HORIZONT_DISTANCE_CHUNKS = MAX_UPDATE_DISTANCE_CHUNKS - (32 << MESH_SIZE_BIT_CHUNKS);
		HORIZONT_DISTANCE_BLOCKS = HORIZONT_DISTANCE_CHUNKS << 4;
		HORIZONT_DISTANCE_SQ = HORIZONT_DISTANCE_CHUNKS * HORIZONT_DISTANCE_CHUNKS;
		FAR_PLANE = HORIZONT_DISTANCE_BLOCKS * 2;
	}

	public static void setClosePlaneRange(float closePlaneDistance) {
		CLOSE_PLANE = closePlaneDistance;
	}
}
