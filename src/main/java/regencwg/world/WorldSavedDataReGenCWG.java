package regencwg.world;

import java.util.HashSet;
import java.util.Set;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import regencwg.ReGenCWGMod;
import regencwg.world.storage.DiskDataUtil;

public class WorldSavedDataReGenCWG extends WorldSavedData {

	final static String DATA_IDENTIFIER = ReGenCWGMod.MODID+"Data";
	public final Set<CubePos> remainingCP = new HashSet<CubePos>();
	private boolean isInitialized = false;
	
	public WorldSavedDataReGenCWG(String name) {
		super(name);
	}
	
	public void initialize(World world) {
		remainingCP.clear();
		DiskDataUtil.addAllSavedCubePosToSet(world, remainingCP);
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

	public void stop() {
		remainingCP.clear();
		this.markDirty();
	}
}
