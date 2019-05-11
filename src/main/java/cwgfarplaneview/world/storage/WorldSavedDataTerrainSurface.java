package cwgfarplaneview.world.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class WorldSavedDataTerrainSurface extends WorldSavedData {

	public static volatile WorldSavedDataTerrainSurface instance = null;
	private static final Object lock = new Object();
	private static final String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "Data";
	private final XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 10000);

	public WorldSavedDataTerrainSurface(String name) {
		super(name);
	}

	public WorldSavedDataTerrainSurface() {
		this(DATA_IDENTIFIER);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		synchronized (lock) {
			NBTTagList tp = nbt.getTagList("terrainMap", 10);
			for (int i = 0; i < tp.tagCount(); i++) {
				TerrainPoint point;
				try {
					point = TerrainPoint.fromNBT(tp.getCompoundTagAt(i));
					this.addToMap(point);
				} catch (IncorrectTerrainDataException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	public void addToMap(TerrainPoint value) {
		synchronized (lock) {
			terrainMap.put(value);
			this.markDirty();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		synchronized (lock) {
			NBTTagList tp = new NBTTagList();
			for (TerrainPoint point : getTerrainMap()) {
				tp.appendTag(point.toNBT());
			}
			compound.setTag("terrainMap", tp);
			return compound;
		}
	}

	public static WorldSavedDataTerrainSurface getOrCreateWorldSavedData(World worldIn) {
		if (instance != null)
			return instance;
		synchronized (lock) {
			WorldSavedDataTerrainSurface data = new WorldSavedDataTerrainSurface(DATA_IDENTIFIER);
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

	public void clear() {
		synchronized (lock) {
			getTerrainMap().clear();
			this.markDirty();
		}
	}

	public TerrainPoint get(int x, int z) {
		synchronized (lock) {
			return getTerrainMap().get(x, z);
		}
	}

	public XZMap<TerrainPoint> getTerrainMap() {
		synchronized (lock) {
			return terrainMap;
		}
	}
}
