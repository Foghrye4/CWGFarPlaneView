package cwgfarplaneview.world.terrain.volumetric;

import static cwgfarplaneview.util.TerrainConfig.CUBE_SIZE_BIT_MESH;

import cwgfarplaneview.util.NBTUtil;
import cwgfarplaneview.world.biome.BiomeData;
import io.github.opencubicchunks.cubicchunks.api.util.XYZAddressable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class TerrainCube implements XYZAddressable {
	public int x, y, z;
	public ExtendedBlockStorage storage;
	public BiomeData biomeData = new BiomeData();
	public NibbleArray xLocals = new NibbleArray();
	public NibbleArray yLocals = new NibbleArray();
	public NibbleArray zLocals = new NibbleArray();
	private World world;

	public TerrainCube(World worldIn, int xIn, int yIn, int zIn) {
		x = xIn;
		y = yIn;
		z = zIn;
		world = worldIn;
		storage = new ExtendedBlockStorage(y << CUBE_SIZE_BIT_MESH, world.provider.hasSkyLight());
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getZ() {
		return z;
	}
	
	public void getTerrainPoint(TerrainPoint3D pointIn, int meshX, int meshY, int meshZ) {
		pointIn.meshX = meshX;
		pointIn.meshY = meshY;
		pointIn.meshZ = meshZ;
		int meshLocalX = meshX & 15;
		int meshLocalY = meshY & 15;
		int meshLocalZ = meshZ & 15;
		pointIn.localX = (byte) xLocals.get(meshLocalX, meshLocalY, meshLocalZ);
		pointIn.localY = (byte) yLocals.get(meshLocalX, meshLocalY, meshLocalZ);
		pointIn.localZ = (byte) zLocals.get(meshLocalX, meshLocalY, meshLocalZ);
		pointIn.blockLight = (byte) storage.getBlockLight(meshLocalX, meshLocalY, meshLocalZ);
		if(world.provider.hasSkyLight())
			pointIn.skyLight = (byte) storage.getSkyLight(meshLocalX, meshLocalY, meshLocalZ);
		pointIn.blockState = storage.get(meshLocalX, meshLocalY, meshLocalZ);
		pointIn.setBiome(biomeData.get(meshLocalX, meshLocalY, meshLocalZ));
	}
	
	public void setLocals(int meshX, int meshY, int meshZ, int localX, int localY, int localZ) {
		xLocals.set(meshX & 15, meshY & 15, meshZ & 15, localX);
		yLocals.set(meshX & 15, meshY & 15, meshZ & 15, localY);
		zLocals.set(meshX & 15, meshY & 15, meshZ & 15, localZ);
	}
	
	public void setBlock(int meshX, int meshY, int meshZ, IBlockState state) {
		storage.set(meshX & 15, meshY & 15, meshZ & 15, state);
	}
	
	public void setBlockLight(int meshX, int meshY, int meshZ, int blockLight) {
		storage.setBlockLight(meshX & 15, meshY & 15, meshZ & 15, blockLight);
	}
	
	public void setSkyLight(int meshX, int meshY, int meshZ, int skyLight) {
		storage.setSkyLight(meshX & 15, meshY & 15, meshZ & 15, skyLight);
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setTag("storage", NBTUtil.writeEBS(storage, new NBTTagCompound()));
		nbt.setByteArray("biomeData",biomeData.data);
		nbt.setByteArray("xLocals",xLocals.getData());
		nbt.setByteArray("yLocals",yLocals.getData());
		nbt.setByteArray("zLocals",zLocals.getData());
		return nbt;
	}
	
	public static TerrainCube fromNBT(World world,NBTTagCompound nbt) {
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		TerrainCube cube = new TerrainCube(world, x, y, z);
		NBTUtil.readEBS(cube.storage,nbt.getCompoundTag("storage"));
		cube.biomeData.data = nbt.getByteArray("biomeData");
		cube.xLocals = new NibbleArray(nbt.getByteArray("xLocals"));
		cube.yLocals = new NibbleArray(nbt.getByteArray("yLocals"));
		cube.zLocals = new NibbleArray(nbt.getByteArray("zLocals"));
		return cube;
	}
}
