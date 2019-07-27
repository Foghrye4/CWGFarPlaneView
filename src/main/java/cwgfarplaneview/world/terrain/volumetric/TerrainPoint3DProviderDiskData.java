package cwgfarplaneview.world.terrain.volumetric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cwgfarplaneview.util.AddressUtil;
import cwgfarplaneview.util.DiskDataUtil;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

public class TerrainPoint3DProviderDiskData implements TerrainPoint3DProvider {
	private SaveSection3D cubeIO;
	private WorldServer world;

	public TerrainPoint3DProviderDiskData(WorldServer worldIn) {
		world = worldIn;
		cubeIO = DiskDataUtil.createCubeIO(worldIn);
	}

	@Override
	public TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException {
		int cubeX = meshX << AddressUtil.MESH_SIZE_BIT_CHUNKS;
		int cubeZ = meshZ << AddressUtil.MESH_SIZE_BIT_CHUNKS;
		int cubeY = meshY << AddressUtil.MESH_SIZE_BIT_CHUNKS;
		int rangeOfSearch = 15;
		try {
			ExtendedBlockStorage[] cache = prepareCache(cubeX, cubeY, cubeZ);
			if (cache == null)
				return null;
			IBlockState blockState = this.getBlockState(cache, 0, 0, 0);
			if (TerrainUtil.isAirOrWater(blockState))
				return new TerrainPoint3D(meshX, meshY, meshZ, (byte) 0, (byte) 0, (byte) 0, blockState,
						Biomes.PLAINS);
			for (EnumFacing face : EnumFacing.values()) {
				blockState = this.getBlockState(cache, face.getFrontOffsetX() * rangeOfSearch,
						face.getFrontOffsetY() * rangeOfSearch, face.getFrontOffsetZ() * rangeOfSearch);
				if (TerrainUtil.isAirOrWater(blockState)) {
					while (TerrainUtil.isAirOrWater(blockState)) {
						rangeOfSearch--;
						blockState = this.getBlockState(cache, face.getFrontOffsetX() * rangeOfSearch,
								face.getFrontOffsetY() * rangeOfSearch, face.getFrontOffsetZ() * rangeOfSearch);
					}
					byte localX = (byte) (face.getFrontOffsetX() * rangeOfSearch);
					byte localY = (byte) (face.getFrontOffsetY() * rangeOfSearch);
					byte localZ = (byte) (face.getFrontOffsetZ() * rangeOfSearch);
					return new TerrainPoint3D(meshX, meshY, meshZ, localX, localY, localZ, blockState,
							getBiomeAt(cubeX, cubeZ));
				}
			}
			return new TerrainPoint3D(meshX, meshY, meshZ, (byte) 0, (byte) 0, (byte) 0, blockState,
					getBiomeAt(cubeX, cubeZ));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ExtendedBlockStorage[] prepareCache(int cubeX,int cubeY,int cubeZ) throws IOException {
		ExtendedBlockStorage[] cache = new ExtendedBlockStorage[8];
		for(int ix=-1;ix<=0;ix++) {
			for(int iy=-1;iy<=0;iy++) {
				for(int iz=-1;iz<=0;iz++) {
					EntryLocation3D ebsKey = new EntryLocation3D(cubeX + ix, cubeY + iy, cubeZ + iz);
					Optional<ByteBuffer> buf = cubeIO.load(ebsKey);
					if (!buf.isPresent())
						return null;
					NBTTagCompound nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(buf.get().array())).getCompoundTag("Level");
					cache[cacheKey(ix+1,iy+1,iz+1)] = readBlocks(nbt, world);
					if(cubeY <= 3 && !nbt.hasKey("Sections")) {
						logger.error(
								"Cube at " + cubeX * 16 + ";" + cubeY * 16 + ";" + cubeZ * 16 + " appear to be empty");
					}
				}
			}
		}
		return cache;
	}
		
	private IBlockState getBlockState(ExtendedBlockStorage[] cache, int localX, int localY, int localZ) {
		int keyX = (localX+15)/16;
		int keyY = (localY+15)/16;
		int keyZ = (localZ+15)/16;
		int ebsX = localX < 0 ? localX + 15 : localX;
		int ebsY = localY < 0 ? localY + 15 : localY;
		int ebsZ = localZ < 0 ? localZ + 15 : localZ;
		return cache[cacheKey(keyX,keyY,keyZ)].get(ebsX, ebsY, ebsZ);
	}
	
	private int cacheKey(int x,int y,int z) {
		return x<<2|y<<1|z;
	}

	@SuppressWarnings("deprecation")
	private ExtendedBlockStorage readBlocks(NBTTagCompound nbt, World world) {
		ExtendedBlockStorage ebs = new ExtendedBlockStorage(0, false);
		boolean isEmpty = !nbt.hasKey("Sections");// is this an empty cube?
		if (!isEmpty) {
			NBTTagList sectionList = nbt.getTagList("Sections", 10);
			nbt = sectionList.getCompoundTagAt(0);

			byte[] abyte = nbt.getByteArray("Blocks");
			NibbleArray data = new NibbleArray(nbt.getByteArray("Data"));
			NibbleArray add = nbt.hasKey("Add", Constants.NBT.TAG_BYTE_ARRAY) ? new NibbleArray(nbt.getByteArray("Add"))
					: null;
			NibbleArray add2neid = nbt.hasKey("Add2", Constants.NBT.TAG_BYTE_ARRAY)
					? new NibbleArray(nbt.getByteArray("Add2"))
					: null;

			for (int i = 0; i < 4096; i++) {
				int x = i & 15;
				int y = i >> 8 & 15;
				int z = i >> 4 & 15;

				int toAdd = add == null ? 0 : add.getFromIndex(i);
				toAdd = (toAdd & 0xF) | (add2neid == null ? 0 : add2neid.getFromIndex(i) << 4);
				int id = (toAdd << 12) | ((abyte[i] & 0xFF) << 4) | data.getFromIndex(i);
				ebs.getData().set(x, y, z, Block.BLOCK_STATE_IDS.getByValue(id));
			}
		}
		return ebs;
	}

	private Biome getBiomeAt(int x, int z) {
		Biome[] biomes = new Biome[256];
		world.getBiomeProvider().getBiomes(biomes, x << 4, z << 4, 16, 16, false);
		return biomes[0];
	}

}
