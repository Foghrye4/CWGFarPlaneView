package cwgfarplaneview.world.terrain.volumetric;

import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.api.util.XYZAddressable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.GameData;

public class TerrainPoint3D implements XYZAddressable {
	public int meshX;
	public int meshY;
	public int meshZ;
	public byte localX;
	public byte localY;
	public byte localZ;
	public byte blockLight = 0;
	public byte skyLight = -1;
	public IBlockState blockState;
	private Biome biome = Biomes.PLAINS;

	public TerrainPoint3D() {}
	
	public TerrainPoint3D(int x, int y, int z, byte localX, byte localY, byte localZ, int blockStateIDIn, int biomeId) throws IncorrectTerrainDataException {
		this(x, y, z, localX, localY, localZ, GameData.getBlockStateIDMap().getByValue(blockStateIDIn), Biome.getBiome(biomeId));
	}

	public TerrainPoint3D(int x, int y, int z, byte localXIn, byte localYIn, byte localZIn, IBlockState blockStateIn, Biome biomeIn) throws IncorrectTerrainDataException {
		meshX = x;
		meshY = y;
		meshZ = z;
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
	public int getX() {
		return meshX;
	}
	
	@Override
	public int getY() {
		return meshY;
	}

	@Override
	public int getZ() {
		return meshZ;
	}
	
	public Biome getBiome() {
		return biome;
	}

	public void setBiome(Biome biomeIn) {
		if (biomeIn == null)
			throw new NullPointerException();
		biome = biomeIn;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", meshX);
		nbt.setInteger("y", meshY);
		nbt.setInteger("z", meshZ);
		nbt.setByte("localX", localX);
		nbt.setByte("localY", localY);
		nbt.setByte("localZ", localZ);
		nbt.setInteger("blockstate", GameData.getBlockStateIDMap().get(blockState));
		nbt.setInteger("biome", Biome.getIdForBiome(biome));
		nbt.setByte("blockLight", blockLight);
		nbt.setByte("skyLight", skyLight);
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
		TerrainPoint3D tp = new TerrainPoint3D(x, y, z, localX, localY, localZ, bstate, biome);
		tp.blockLight = nbt.getByte("blockLight");
		if(nbt.hasKey("skyLight"))
			tp.skyLight = nbt.getByte("skyLight");
		return tp;
	}

	@Override
	public String toString() {
		return "TerrainPoint3D[cubeX:" + meshX + ",cubeY:" + meshY + ",cubeZ:" + meshZ + "]";
	}

	public boolean isVisible() {
		return !TerrainUtil.shouldBeSkipped(blockState);
	}
}
