package cwgfarplaneview.world.storage;

import cwgfarplaneview.CWGFarPlaneViewMod;
import cwgfarplaneview.world.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class WorldSavedDataTerrainSurface extends WorldSavedData {

	final static String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "Data";
	public XZMap<TerrainPoint> terrainMap = new XZMap<TerrainPoint>(0.8f, 8000);
	public int minimalX = 0;
	public int minimalZ = 0;
	public int maximalX = 0;
	public int maximalZ = 0;
	private Object lock = new Object();

	public WorldSavedDataTerrainSurface(String name) {
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList tp = nbt.getTagList("terrainMap", 10);
		synchronized (lock) {
			for (int i = 0; i < tp.tagCount(); i++) {
				TerrainPoint point = TerrainPoint.fromNBT(tp.getCompoundTagAt(i));
				this.addToMap(point);
			}
		}
	}

	public void addToMap(TerrainPoint value) {
		synchronized (lock) {
			terrainMap.put(value);
		}
		if (value.getX() < this.minimalX)
			this.minimalX = value.getX();
		if (value.getZ() < this.minimalZ)
			this.minimalZ = value.getZ();
		if (value.getX() > this.maximalX)
			this.maximalX = value.getX();
		if (value.getZ() > this.maximalZ)
			this.maximalZ = value.getZ();
		this.markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList tp = new NBTTagList();
		synchronized (lock) {
			for (TerrainPoint point : terrainMap) {
				tp.appendTag(point.toNBT());
			}
		}
		compound.setTag("terrainMap", tp);
		return compound;
	}

	public static WorldSavedDataTerrainSurface getOrCreateWorldSavedData(World worldIn) {
		WorldSavedDataTerrainSurface data = (WorldSavedDataTerrainSurface) worldIn.getPerWorldStorage()
				.getOrLoadData(WorldSavedDataTerrainSurface.class, WorldSavedDataTerrainSurface.DATA_IDENTIFIER);
		if (data == null) {
			data = new WorldSavedDataTerrainSurface(DATA_IDENTIFIER);
			worldIn.getPerWorldStorage().setData(DATA_IDENTIFIER, data);
		}
		return data;
	}

	public void clear() {
		synchronized (lock) {
			terrainMap.clear();
		}
		minimalX = 0;
		minimalZ = 0;
		maximalX = 0;
		maximalZ = 0;
		this.markDirty();
	}
}
