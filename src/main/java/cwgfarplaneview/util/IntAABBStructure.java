package cwgfarplaneview.util;

import net.minecraft.block.state.IBlockState;

public class IntAABBStructure {
	public final int minX, minY, minZ, maxX, maxY, maxZ;
	public final IBlockState bstate;
	public IntAABBStructure(IBlockState bstateIn, int minXIn, int minYIn, int minZIn, int maxXIn, int maxYIn, int maxZIn) {
		minX = minXIn;
		minY = minYIn;
		minZ = minZIn;
		maxX = maxXIn;
		maxY = maxYIn;
		maxZ = maxZIn;
		bstate = bstateIn;
	}
}
