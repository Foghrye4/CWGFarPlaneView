package cwgfarplaneview.core;

public class NonStaticIntCache {
	private int intCacheSize = 512;
	private int[] smallArray = new int[256];
	private int[] bigArray = new int[512];

	public synchronized int[] getIntCache(int size) {
		if (size <= 256) {
			return smallArray;
		} else if (size > intCacheSize) {
			bigArray = new int[size];
			intCacheSize = size;
		}
		return bigArray;
	}
}
