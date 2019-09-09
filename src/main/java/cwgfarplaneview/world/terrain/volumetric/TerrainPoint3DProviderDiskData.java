package cwgfarplaneview.world.terrain.volumetric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cwgfarplaneview.util.TerrainConfig;
import cwgfarplaneview.util.DiskDataUtil;
import cwgfarplaneview.util.TerrainUtil;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import io.github.opencubicchunks.cubicchunks.core.util.AddressTools;
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

public class TerrainPoint3DProviderDiskData extends TerrainPoint3DProvider {
	private SaveSection3D cubeIO;
	private ExtendedBlockStorage cachedStorage;

	public TerrainPoint3DProviderDiskData(WorldServer worldIn) {
		super(worldIn);
		cubeIO = DiskDataUtil.createCubeIO(worldIn);
	}

	@Override
	public TerrainPoint3D getTerrainPointAt(int meshX, int meshY, int meshZ) throws IncorrectTerrainDataException {
		int cubeX = meshX << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int cubeZ = meshZ << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		int cubeY = meshY << TerrainConfig.MESH_SIZE_BIT_CHUNKS;
		EntryLocation3D ebsKey = new EntryLocation3D(cubeX, cubeY, cubeZ);
		Optional<ByteBuffer> buf;
		try {
			buf = cubeIO.load(ebsKey);
			if (!buf.isPresent()) {
				return null;
			}
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(buf.get().array()))
					.getCompoundTag("Level");
			cachedStorage = readBlocks(nbt, world);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		TerrainPoint3D tp = getPointOf(meshX, meshY, meshZ);
		tp.blockLight = (byte) cachedStorage.getBlockLight(tp.localX, tp.localY, tp.localZ);
		tp.skyLight = (byte) cachedStorage.getSkyLight(tp.localX, tp.localY, tp.localZ);
		return tp;
	}
	
	@Override
	protected void reset(int cubeX, int cubeY, int cubeZ) {	}
		
	@Override
	protected IBlockState getBlockStateAt(int x, int y, int z) {
		return cachedStorage.get(x & 15, y & 15, z & 15);
	}

	@SuppressWarnings("deprecation")
	private ExtendedBlockStorage readBlocks(NBTTagCompound nbt, World world) {
		ExtendedBlockStorage ebs = new ExtendedBlockStorage(0, true);
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
            ebs.setBlockLight(new NibbleArray(nbt.getByteArray("BlockLight")));
            ebs.setSkyLight(new NibbleArray(nbt.getByteArray("SkyLight")));
		}
		return ebs;
	}
}
