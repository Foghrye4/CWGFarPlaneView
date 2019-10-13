package cwgfarplaneview.util;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;

public class NBTUtil {
	
	@SuppressWarnings("deprecation")
	public static void readEBS(ExtendedBlockStorage ebs, NBTTagCompound nbt) {
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
	
	
    @SuppressWarnings("deprecation")
	public static NBTTagCompound writeEBS(ExtendedBlockStorage ebs, NBTTagCompound nbt) {
        byte[] abyte = new byte[4096];
        NibbleArray data = new NibbleArray();
        NibbleArray add = null;
        NibbleArray add2neid = null;

        for (int i = 0; i < 4096; ++i) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;

            int id = Block.BLOCK_STATE_IDS.get(ebs.getData().get(x, y, z));

            int in1 = (id >> 12) & 0xF;
            int in2 = (id >> 16) & 0xF;

            if (in1 != 0) {
                if (add == null) {
                    add = new NibbleArray();
                }
                add.setIndex(i, in1);
            }
            if (in2 != 0) {
                if (add2neid == null) {
                    add2neid = new NibbleArray();
                }
                add2neid.setIndex(i, in2);
            }

            abyte[i] = (byte) (id >> 4 & 255);
            data.setIndex(i, id & 15);
        }

        nbt.setByteArray("Blocks", abyte);
        nbt.setByteArray("Data", data.getData());

        if (add != null) {
            nbt.setByteArray("Add", add.getData());
        }
        if (add2neid != null) {
            nbt.setByteArray("Add2", add2neid.getData());
        }

        nbt.setByteArray("BlockLight", ebs.getBlockLight().getData());

		if (ebs.getSkyLight() != null) {
            nbt.setByteArray("SkyLight", ebs.getSkyLight().getData());
        }
		return nbt;
    }
}
