package cwgfarplaneview.world;

import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.save.SaveSection3D;
import cwgfarplaneview.util.DiskDataUtil;
import cwgfarplaneview.world.storage.WorldSavedDataStructures;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.core.server.chunkio.IONbtReader;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StructuresBuilderWorker implements Runnable {

	private final WorldServer worldServer;
	private final WorldSavedDataStructures data;
	private final NibbleArray emptyArray = new NibbleArray();
	private int heightHint = 64;
	volatile public boolean run = true;

	public StructuresBuilderWorker(WorldServer worldServerIn, WorldSavedDataStructures dataIn) {
		worldServer = worldServerIn;
		data = dataIn;
		logger.info("Structures builder worker initialized.");
	}

	public void tick(SaveSection3D cubeIO) throws IOException {
		EntryLocation3D cpos = data.removeFirst();
		Optional<ByteBuffer> data = cubeIO.load(cpos);
		if(!data.isPresent())
			return;
        NBTTagCompound nbt = FMLCommonHandler.instance().getDataFixer().process(FixTypes.CHUNK, CompressedStreamTools.readCompressed(new ByteArrayInputStream(data.get().array())));
        ExtendedBlockStorage ebs = readBlocks(nbt,cpos.getEntryY());
		if (ebs == null) {
			
		}

	}
	
    private ExtendedBlockStorage readBlocks(NBTTagCompound nbt, int cubeY) {
        boolean isEmpty = !nbt.hasKey("Sections");// is this an empty cube?
        if (!isEmpty) {
            NBTTagList sectionList = nbt.getTagList("Sections", 10);
            nbt = sectionList.getCompoundTagAt(0);
            ExtendedBlockStorage ebs = new ExtendedBlockStorage(Coords.cubeToMinBlock(cubeY), false);
            byte[] abyte = nbt.getByteArray("Blocks");
            NibbleArray data = new NibbleArray(nbt.getByteArray("Data"));
            NibbleArray add = nbt.hasKey("Add", 7) ? new NibbleArray(nbt.getByteArray("Add")) : null;
            ebs.getData().setDataFromNBT(abyte, data, add);
            ebs.recalculateRefCounts();
            return ebs;
        }
        return null;
    }

	@Override
	public void run() {
		try(SaveSection3D cubeIO = DiskDataUtil.createCubeIO(worldServer)) {
			if(!data.isInitialized) {
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
