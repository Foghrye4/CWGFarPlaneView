package cwgfarplaneview.world;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.util.TerrainUtil.isAirOrWater;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cwgfarplaneview.util.DiskDataUtil;
import cwgfarplaneview.world.storage.WorldSavedDataStructures;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface2d;
import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StructuresBuilderWorker implements Runnable {

	private final WorldServer worldServer;
	private WorldSavedDataStructures data;
	private WorldSavedDataTerrainSurface2d surface;
	private final byte[] emptyByteArray = new byte[4096];
	private final NibbleArray emptyNibbleArray = new NibbleArray();
	private int heightHint = 64;
	volatile public boolean run = true;

	public StructuresBuilderWorker(WorldServer worldServerIn) {
		worldServer = worldServerIn;
		logger.info("Structures builder worker initialized.");
	}

	public void tick(SaveSection3D cubeIO) throws IOException {
		EntryLocation3D cpos = data.removeFirst();
		int cubeX = cpos.getEntryX();
		int cubeZ = cpos.getEntryZ();
		TerrainPoint tp = surface.get(cubeX, cubeZ);
		if (tp == null)
			return;
		Optional<ByteBuffer> data = cubeIO.load(cpos);
		if (!data.isPresent())
			return;
		NBTTagCompound nbt = FMLCommonHandler.instance().getDataFixer().process(FixTypes.CHUNK,
				CompressedStreamTools.readCompressed(new ByteArrayInputStream(data.get().array())));
		ExtendedBlockStorage ebs = readBlocks(nbt, cpos.getEntryY());
		int minBlockY = cpos.getEntryY() << 4;
		int maxBlockY = minBlockY + ICube.SIZE;
		if (tp.blockY > maxBlockY) {
			return;
		}
		if (ebs.isEmpty()) {
			if (tp.blockY > minBlockY) {
				this.replaceTerrainPoint(tp, minBlockY);
			}
			return;
		}
		int minY = 0;
		for (int y = 0; y < 16; y++) {
			if (isAirOrWater(ebs.get(0, y, 0))) {
				minY = y - 1;
				break;
			}
		}
		if (minBlockY + minY < tp.blockY) {
			tp = this.replaceTerrainPoint(tp, minBlockY + minY);
		}
		Set<IBlockState> structureBlocks = new HashSet<IBlockState>();
		for (int ix = 0; ix < 16; ix++)
			for (int iy = Math.max(tp.blockY + 2, 0); iy < 16; iy++)
				for (int iz = 0; iz < 16; iz++) {
					IBlockState state = ebs.get(ix, iy, iz);
					if(isAirOrWater(state))
						continue;
					if(structureBlocks.contains(state))
						continue;
					structureBlocks.add(state);
					int minSX = ix;
					int minSY = iy;
					int minSZ = iz;
					int maxSX = ix;
					int maxSY = iy;
					int maxSZ = iz;
					int prevBlockCount = 0;
					for(int i=0;i<EnumFacing.VALUES.length;i++) {
						EnumFacing side = EnumFacing.VALUES[i];
						int blocksCountStructure = 0;
						int blocksCountTotal = 0;
						Vec3i faceVec = side.getDirectionVec();
						int minSX1 = minSX + faceVec.getX() < 0 ? -1 : 0;
						int minSY1 = minSY + faceVec.getY() < 0 ? -1 : 0;
						int minSZ1 = minSZ + faceVec.getZ() < 0 ? -1 : 0;
						int maxSX1 = maxSX + faceVec.getX() > 0 ? 1 : 0;
						int maxSY1 = maxSY + faceVec.getY() > 0 ? 1 : 0;
						int maxSZ1 = maxSZ + faceVec.getZ() > 0 ? 1 : 0;
						int count = this.countBlockstates(ebs, state, minSX1, minSY1, minSZ1, maxSX1, maxSY1, maxSZ1);
						if (count == prevBlockCount)
							continue;
						int expectedMax = (maxSX1-minSX1)*(maxSY1-minSY1)*(maxSZ1-minSZ1);
					}
				}
	}
	
	private int countBlockstates(ExtendedBlockStorage ebs, IBlockState state, int minX, int minY, int minZ, int maxX, int maxY,int maxZ) {
		int count = 0;
		for (int ix = minX; ix <= maxX; ix++)
			for (int iy = minY; iy <= maxY; iy++)
				for (int iz = minZ; iz <= maxZ; iz++) {
					if(ebs.get(ix, iy, iz) == state)
						count++;
				}
		return count;
	}

	private TerrainPoint replaceTerrainPoint(TerrainPoint old, int newHeight) {
		try {
			TerrainPoint tp = new TerrainPoint(old.chunkX, old.chunkZ, newHeight, old.blockState, old.biome);
			surface.addToMap(tp);
			return tp;
		} catch (IncorrectTerrainDataException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ExtendedBlockStorage readBlocks(NBTTagCompound nbt, int cubeY) {
		ExtendedBlockStorage ebs = new ExtendedBlockStorage(Coords.cubeToMinBlock(cubeY), false);
		boolean isEmpty = !nbt.hasKey("Sections");// is this an empty cube?
		if (!isEmpty) {
			NBTTagList sectionList = nbt.getTagList("Sections", 10);
			nbt = sectionList.getCompoundTagAt(0);
			byte[] abyte = nbt.getByteArray("Blocks");
			NibbleArray data = new NibbleArray(nbt.getByteArray("Data"));
			NibbleArray add = nbt.hasKey("Add", 7) ? new NibbleArray(nbt.getByteArray("Add")) : null;
			ebs.getData().setDataFromNBT(abyte, data, add);
			ebs.recalculateRefCounts();
			return ebs;
		}
		ebs.getData().setDataFromNBT(emptyByteArray, emptyNibbleArray, null);
		return ebs;
	}

	@Override
	public void run() {
		try (SaveSection3D cubeIO = DiskDataUtil.createCubeIO(worldServer)) {
			data = WorldSavedDataStructures.getOrCreateWorldSavedData(worldServer);
			surface = WorldSavedDataTerrainSurface2d.getOrCreateWorldSavedData(worldServer);
			if (!data.isInitialized) {
				data.initialize(cubeIO);
			}
			while (run) {
				tick(cubeIO);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
