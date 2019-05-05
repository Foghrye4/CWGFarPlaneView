package cwgfarplaneview.world.storage;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.world.terrain.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class WorldSavedDataTerrainSurface extends WorldSavedData {

	private static final Object lock = new Object();
	private static final String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "Data";
	private final XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 8000);

	public WorldSavedDataTerrainSurface(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		synchronized (lock) {
			NBTTagList tp = nbt.getTagList("terrainMap", 10);
			for (int i = 0; i < tp.tagCount(); i++) {
				TerrainPoint point = TerrainPoint.fromNBT(tp.getCompoundTagAt(i));
				this.addToMap(point);
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
		synchronized (lock) {
			WorldSavedDataTerrainSurface data = (WorldSavedDataTerrainSurface) worldIn.getPerWorldStorage()
					.getOrLoadData(WorldSavedDataTerrainSurface.class, WorldSavedDataTerrainSurface.DATA_IDENTIFIER);
			if (data == null) {
				data = new WorldSavedDataTerrainSurface(DATA_IDENTIFIER);
				worldIn.getPerWorldStorage().setData(DATA_IDENTIFIER, data);
			}
			return data;
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
