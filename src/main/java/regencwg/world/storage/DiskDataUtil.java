package regencwg.world.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.core.server.chunkio.SharedCachedRegionProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class DiskDataUtil {

	@SuppressWarnings("unchecked")
	public static void addAllSavedCubePosToSet(World world, Set<CubePos> posSet) {
		WorldProvider prov = world.provider;
		Path path = world.getSaveHandler().getWorldDirectory().toPath();
		if (prov.getSaveFolder() != null) {
			path = path.resolve(prov.getSaveFolder());
		}
		Path part3d = path.resolve("region3d");
        try (SaveSection3D cubeIO = new SaveSection3D(
                new SharedCachedRegionProvider<>(
                        SimpleRegionProvider.createDefault(new EntryLocation3D.Provider(), part3d, 512)),
                new SharedCachedRegionProvider<>(new SimpleRegionProvider<>(new EntryLocation3D.Provider(), part3d,
                        (keyProvider, regionKey) -> new ExtRegion<>(part3d, Collections.emptyList(), keyProvider,
                                regionKey))))) {
            cubeIO.forAllKeys(c -> {
            	posSet.add(new CubePos(c.getEntryX(),c.getEntryY(),c.getEntryZ()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
