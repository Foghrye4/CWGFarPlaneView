package cwgfarplaneview.util;

import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class AddressUtil {
	public final static int MESH_SIZE_BIT_CHUNKS = 0;
	public final static int MESH_SIZE_BIT_BLOCKS = MESH_SIZE_BIT_CHUNKS + 4;
	public final static int MAX_UPDATE_DISTANCE_CHUNKS = 92 << MESH_SIZE_BIT_CHUNKS;

	public static void meshCoordsToBlockPos(MutableBlockPos pos, int meshX, int meshZ) {
		pos.setPos(meshX << MESH_SIZE_BIT_BLOCKS, 64, meshZ << MESH_SIZE_BIT_BLOCKS);
	}
}
