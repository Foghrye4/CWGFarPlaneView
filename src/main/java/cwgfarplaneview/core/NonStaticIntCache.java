package cwgfarplaneview.core;

public class NonStaticIntCache {
	private ThreadLocal<MultiArrayProvider> smallArrays = new ThreadLocal<MultiArrayProvider>() {
		@Override
		protected MultiArrayProvider initialValue() {
			return new MultiArrayProvider(256);
		}
	};
	private ThreadLocal<MultiArrayProvider> bigArrays = new ThreadLocal<MultiArrayProvider>() {
		@Override
		protected MultiArrayProvider initialValue() {
			return new MultiArrayProvider(512);
		}
	};

	public synchronized int[] getIntCache(int size) {
		if (size <= 256) {
			return smallArrays.get().getArray(size);
		}
		return bigArrays.get().getArray(size);
	}
}
