package cwgfarplaneview.world.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.XYZMap;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class WorldSavedDataTerrainSurface3d extends WorldSavedData {

	public static volatile WorldSavedDataTerrainSurface3d instance = null;
	private static final Object lock = new Object();
	private static final String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "3DData";
	private final XYZMap<TerrainPoint3D> terrainMap = new XYZMap<TerrainPoint3D>(0.8f, 10000);

	public WorldSavedDataTerrainSurface3d(String name) {
		super(name);
	}

	public WorldSavedDataTerrainSurface3d() {
		this(DATA_IDENTIFIER);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		synchronized (lock) {
			NBTTagList tp = nbt.getTagList("terrainMap", 10);
			for (int i = 0; i < tp.tagCount(); i++) {
				TerrainPoint3D point;
				try {
					point = TerrainPoint3D.fromNBT(tp.getCompoundTagAt(i));
					this.addToMap(point);
				} catch (IncorrectTerrainDataException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	public void addToMap(TerrainPoint3D value) {
		synchronized (lock) {
			terrainMap.put(value);
			this.markDirty();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		synchronized (lock) {
			NBTTagList tp = new NBTTagList();
			for (TerrainPoint3D point : getTerrainMap()) {
				tp.appendTag(point.toNBT());
			}
			compound.setTag("terrainMap", tp);
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
					data.readFromNBT(nbttagcompound.getCompoundTag("data"));
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
			this.markDirty();
		}
	}

	public TerrainPoint3D get(int x, int y, int z) {
		synchronized (lock) {
			return getTerrainMap().get(x, y, z);
		}
	}

	public XYZMap<TerrainPoint3D> getTerrainMap() {
		synchronized (lock) {
			return terrainMap;
		}
	}
}
