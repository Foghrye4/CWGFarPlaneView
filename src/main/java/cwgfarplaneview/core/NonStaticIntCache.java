package cwgfarplaneview.core;

public class NonStaticIntCache {
	private static final int MAX_ARRAYS_ALLOCATED = 4;
	private int intCacheSize = 512;
	private int allocatedSmallArray = 0;
	private int allocatedBigArray = 0;
	private int[][] smallArrays = new int[MAX_ARRAYS_ALLOCATED][256];
	private int[][] bigArrays = new int[MAX_ARRAYS_ALLOCATED][512];

	public synchronized int[] getIntCache(int size) {
		if (size <= 256) {
			return smallArrays[++allocatedSmallArray%MAX_ARRAYS_ALLOCATED];
		} else if (size > intCacheSize) {
			bigArrays = new int[MAX_ARRAYS_ALLOCATED][size];
			intCacheSize = size;
		}
		return bigArrays[++allocatedBigArray%MAX_ARRAYS_ALLOCATED];
	}
}
