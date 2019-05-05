package cwgfarplaneview.util;

import java.nio.file.Path;
import java.util.Collections;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SharedCachedRegionProvider;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class DiskDataUtil {
	
	@SuppressWarnings("unchecked")
	public static SaveSection3D createCubeIO(World world) {
		WorldProvider prov = world.provider;
		Path path = world.getSaveHandler().getWorldDirectory().toPath();
		if (prov.getSaveFolder() != null) {
			path = path.resolve(prov.getSaveFolder());
		}
		Path part3d = path.resolve("region3d");
		return new SaveSection3D(
				new SharedCachedRegionProvider<>(
						SimpleRegionProvider.createDefault(new EntryLocation3D.Provider(), part3d, 512)),
				new SharedCachedRegionProvider<>(new SimpleRegionProvider<>(new EntryLocation3D.Provider(), part3d,
						(keyProvider, regionKey) -> new ExtRegion<>(part3d, Collections.emptyList(), keyProvider,
								regionKey))));

	}
}
