package regencwg.world;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cubicchunks.regionlib.lib.ExtRegion;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.core.server.chunkio.SharedCachedRegionProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldSavedData;
import regencwg.ReGenCWGMod;

public class WorldSavedDataReGenCWG extends WorldSavedData {

	final static String DATA_IDENTIFIER = ReGenCWGMod.MODID+"Data";
	public final Set<CubePos> remainingCP = new HashSet<CubePos>();
	private boolean isInitialized = false;
	
	public WorldSavedDataReGenCWG(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	public void initialize(World world) {
		remainingCP.clear();
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
            	remainingCP.add(new CubePos(c.getEntryX(),c.getEntryY(),c.getEntryZ()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
		isInitialized = true;
		this.markDirty();
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		isInitialized = nbt.getBoolean("isInitialized");
		int[] remainingCPIA = nbt.getIntArray("remainingCP");
		for (int i = 0; i < remainingCPIA.length / 3; i += 3) {
			remainingCP.add(new CubePos(remainingCPIA[i], remainingCPIA[i + 1], remainingCPIA[i + 2]));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isInitialized",isInitialized);
		int[] remainingCPIA = new int[remainingCP.size()*3];
		int i=-1;
		for(CubePos pos:remainingCP) {
			remainingCPIA[++i]=pos.getX();
			remainingCPIA[++i]=pos.getY();
			remainingCPIA[++i]=pos.getZ();
		}
		compound.setIntArray("remainingCP", remainingCPIA);
		return compound;
	}

	public static WorldSavedDataReGenCWG getOrCreateWorldSavedData(World worldIn) {
		WorldSavedDataReGenCWG data = (WorldSavedDataReGenCWG) worldIn.getPerWorldStorage().getOrLoadData(WorldSavedDataReGenCWG.class, WorldSavedDataReGenCWG.DATA_IDENTIFIER);
		if(data == null){
			data = new WorldSavedDataReGenCWG(DATA_IDENTIFIER);
			worldIn.getPerWorldStorage().setData(DATA_IDENTIFIER, data);
		}
		return data;
	}

	public int getRemaining() {
		return remainingCP.size();
	}
}
