package cwgfarplaneview.world.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.world.terrain.volumetric.TerrainCube;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class WorldSavedDataTerrainSurface3d {

	public static volatile WorldSavedDataTerrainSurface3d instance = null;
	private static final Object lock = new Object();
	private static final String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "3DData";
	private final XYZMap<TerrainCube> terrainMap = new XYZMap<TerrainCube>(0.8f, 10000);

	public WorldSavedDataTerrainSurface3d(String name) {
	}

	public WorldSavedDataTerrainSurface3d() {
		this(DATA_IDENTIFIER);
	}
	
	public void readFromNBT(World worldIn, NBTTagCompound nbt) {
		synchronized (lock) {
			NBTTagList list = nbt.getTagList("terrainCubeMap", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				TerrainCube cube;
				try {
					cube = TerrainCube.fromNBT(worldIn, list.getCompoundTagAt(i));
				} catch (Exception e) {
					return;
				}
				this.addToMap(cube);
			}
		}
	}

	public void addToMap(TerrainCube value) {
		synchronized (lock) {
			terrainMap.put(value);
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		synchronized (lock) {
			NBTTagList list = new NBTTagList();
			for (TerrainCube cube : getTerrainMap()) {
				list.appendTag(cube.toNBT());
			}
			compound.setTag("terrainCubeMap", list);
			return compound;
		}
	}

	public static WorldSavedDataTerrainSurface3d getOrCreateWorldSavedData(World worldIn) {
		synchronized (lock) {
			if (instance != null)
				return instance;
			WorldSavedDataTerrainSurface3d data = new WorldSavedDataTerrainSurface3d(DATA_IDENTIFIER);
			File file1 = worldIn.getSaveHandler().getMapFileFromName(DATA_IDENTIFIER);
			if (file1 != null && file1.exists()) {
				try (FileInputStream fileinputstream = new FileInputStream(file1)) {
					NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
					data.readFromNBT(worldIn, nbttagcompound.getCompoundTag("data"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			instance = data;
			return data;
		}
	}

	public void save(World world) {
		synchronized (lock) {
			File file1 = world.getSaveHandler().getMapFileFromName(DATA_IDENTIFIER);
			if (file1 != null) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setTag("data", this.writeToNBT(new NBTTagCompound()));
				try (FileOutputStream fileoutputstream = new FileOutputStream(file1)) {
					CompressedStreamTools.writeCompressed(nbttagcompound, fileoutputstream);
					fileoutputstream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void clear() {
		synchronized (lock) {
			getTerrainMap().clear();
		}
	}

	public TerrainCube get(int x, int y, int z) {
		synchronized (lock) {
			return getTerrainMap().get(x, y, z);
		}
	}

	public XYZMap<TerrainCube> getTerrainMap() {
		synchronized (lock) {
			return terrainMap;
		}
	}
}
