package cwgfarplaneview.world.storage;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cwgfarplaneview.CWGFarPlaneViewMod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class WorldSavedDataStructures extends WorldSavedData {
	private static final String DATA_IDENTIFIER = CWGFarPlaneViewMod.MODID + "StructuresData";

	public final ConcurrentLinkedDeque<EntryLocation3D> positionsDeque = new ConcurrentLinkedDeque<EntryLocation3D>();
	volatile public boolean isInitialized = false;

	public WorldSavedDataStructures(String name) {
		super(name);
	}

	public void initialize(SaveSection3D cubeIO) throws IOException {
		positionsDeque.clear();
		cubeIO.forAllKeys(c -> {
			positionsDeque.add(c);
		});
		isInitialized = true;
		this.markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		isInitialized = nbt.getBoolean("isInitialized");
		positionsDeque.clear();
		NBTTagList tp = nbt.getTagList("positionsDeque", 10);
		for (int i = 0; i < tp.tagCount(); i++) {
			NBTTagCompound tag = tp.getCompoundTagAt(i);
			EntryLocation3D cpos = new EntryLocation3D(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
			positionsDeque.add(cpos);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isInitialized", isInitialized);
		NBTTagList tp = new NBTTagList();
		for (EntryLocation3D cpos : positionsDeque) {
			NBTTagCompound cpTag = new NBTTagCompound();
			cpTag.setInteger("x", cpos.getEntryX());
			cpTag.setInteger("y", cpos.getEntryY());
			cpTag.setInteger("z", cpos.getEntryZ());
			tp.appendTag(cpTag);
		}
		compound.setTag("positionsDeque", tp);
		return compound;
	}

	public static WorldSavedDataStructures getOrCreateWorldSavedData(World worldIn) {
		WorldSavedDataStructures data = (WorldSavedDataStructures) worldIn.getPerWorldStorage()
				.getOrLoadData(WorldSavedDataStructures.class, WorldSavedDataStructures.DATA_IDENTIFIER);
		if (data == null) {
			data = new WorldSavedDataStructures(DATA_IDENTIFIER);
			worldIn.getPerWorldStorage().setData(DATA_IDENTIFIER, data);
		}
		return data;
	}

	public EntryLocation3D removeLast() {
		this.markDirty();
		return positionsDeque.removeLast();
	}

	public EntryLocation3D removeFirst() {
		this.markDirty();
		return positionsDeque.removeFirst();
	}
}
