package cwgfarplaneview.util;

import net.minecraft.util.math.Vec3i;

public class FPVMathUtil {

	public static int dotProduct(Vec3i vec1, Vec3i vec2) {
		return vec1.getX()*vec2.getX()+vec1.getY()*vec2.getY()+vec1.getZ()*vec2.getZ();
	}
}
