package cwgfarplaneview.world.terrain.volumetric;

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
	public IBlockState blockState;
	public Biome biome;

	public TerrainPoint3D(int x, int y, int z, int blockStateIDIn, int biomeId) throws IncorrectTerrainDataException {
		this(x, y, z, GameData.getBlockStateIDMap().getByValue(blockStateIDIn), Biome.getBiome(biomeId));
	}

	public TerrainPoint3D(int x, int y, int z, IBlockState blockStateIn, Biome biomeIn) throws IncorrectTerrainDataException {
		cubeX = x;
		cubeY = y;
		cubeZ = z;
		blockState = blockStateIn;
		biome = biomeIn;
		if (blockState == null)
			throw new IncorrectTerrainDataException("Blockstate is NULL");
		if (blockState == Blocks.AIR.getDefaultState())
			throw new IncorrectTerrainDataException("Blockstate should not be AIR!");
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
		nbt.setInteger("blockstate", GameData.getBlockStateIDMap().get(blockState));
		nbt.setInteger("biome", Biome.getIdForBiome(biome));
		return nbt;
	}

	public static TerrainPoint3D fromNBT(NBTTagCompound nbt) throws IncorrectTerrainDataException {
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		int bstate = nbt.getInteger("blockstate");
		int biome = nbt.getInteger("biome");
		return new TerrainPoint3D(x, y, z, bstate, biome);
	}

	@Override
	public String toString() {
		return "TerrainPoint3D[cubeX:" + cubeX + ",cubeY:" + cubeY + ",cubeZ:" + cubeZ + "]";
	}
}
