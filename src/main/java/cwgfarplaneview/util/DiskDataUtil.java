package cwgfarplaneview.util;

import java.nio.file.Path;

import cubicchunks.regionlib.impl.save.SaveSection3D;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class DiskDataUtil {
	
	public static SaveSection3D createCubeIO(World world) {
		WorldProvider prov = world.provider;
		Path path = world.getSaveHandler().getWorldDirectory().toPath();
		if (prov.getSaveFolder() != null) {
			path = path.resolve(prov.getSaveFolder());
		}
		Path part3d = path.resolve("region3d");
		return SaveSection3D.createAt(part3d);
	}
}
