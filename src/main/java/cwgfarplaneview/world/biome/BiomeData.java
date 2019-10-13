package cwgfarplaneview.world.biome;

import net.minecraft.init.Biomes;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.biome.Biome;

public class BiomeData {
	public byte[] data = new byte[4096];

	public BiomeData() {
	}

	private static int getIndex(int x, int y, int z) {
		return y << 8 | z << 4 | x;
	}

	public void set(int x, int y, int z, Biome state) {
		this.set(getIndex(x & 15, y & 15, z & 15), state);
	}

	protected void set(int index, Biome state) {
		byte i = (byte) Biome.getIdForBiome(state);
		this.data[index] = i;
	}

	public Biome get(int x, int y, int z) {
		return this.get(getIndex(x, y, z));
	}

	protected Biome get(int index) {
		Biome biome = Biome.getBiome(data[index]);
		if (biome == null)
			return Biomes.PLAINS;
		return biome;
	}

	public void read(PacketBuffer buf) {
		buf.readBytes(data);
	}

	public void write(PacketBuffer buf) {
		buf.writeByteArray(data);
	}

	public int getSerializedSize() {
		return data.length;
	}
}
