package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.api.util.XYZAddressable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.GameData;

public class TerrainPoint3D implements XYZAddressable {
	public int cubeX;
	public int cubeY;
	public int cubeZ;
	public byte localX;
	public byte localY;
	public byte localZ;
	public byte blockLight = 0;
	public byte skyLight = -1;
	public IBlockState blockState;
	public Biome biome;

	public TerrainPoint3D(int x, int y, int z, byte localX, byte localY, byte localZ, int blockStateIDIn, int biomeId) throws IncorrectTerrainDataException {
		this(x, y, z, localX, localY, localZ, GameData.getBlockStateIDMap().getByValue(blockStateIDIn), Biome.getBiome(biomeId));
	}

	public TerrainPoint3D(int x, int y, int z, byte localXIn, byte localYIn, byte localZIn, IBlockState blockStateIn, Biome biomeIn) throws IncorrectTerrainDataException {
		cubeX = x;
		cubeY = y;
		cubeZ = z;
		localX = localXIn;
		localY = localYIn;
		localZ = localZIn;
		blockState = blockStateIn;
		biome = biomeIn;
		if (blockState == null)
			throw new IncorrectTerrainDataException("Blockstate is NULL");
		if (biome == null)
			throw new IncorrectTerrainDataException("Biome is NULL");
	}

	@Override
	public int getX() {
		return cubeX;
	}
	
	@Override
	public int getY() {
		return cubeY;
	}

	@Override
	public int getZ() {
		return cubeZ;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", cubeX);
		nbt.setInteger("y", cubeY);
		nbt.setInteger("z", cubeZ);
		nbt.setByte("localX", localX);
		nbt.setByte("localY", localY);
		nbt.setByte("localZ", localZ);
		nbt.setInteger("blockstate", GameData.getBlockStateIDMap().get(blockState));
		nbt.setInteger("biome", Biome.getIdForBiome(biome));
		return nbt;
	}

	public static TerrainPoint3D fromNBT(NBTTagCompound nbt) throws IncorrectTerrainDataException {
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		byte localX = nbt.getByte("localX");
		byte localY = nbt.getByte("localY");
		byte localZ = nbt.getByte("localZ");
		int bstate = nbt.getInteger("blockstate");
		int biome = nbt.getInteger("biome");
		return new TerrainPoint3D(x, y, z, localX, localY, localZ, bstate, biome);
	}

	@Override
	public String toString() {
		return "TerrainPoint3D[cubeX:" + cubeX + ",cubeY:" + cubeY + ",cubeZ:" + cubeZ + "]";
	}

	public boolean isVisible() {
		return !TerrainUtil.isAirOrWater(blockState);
	}
}
