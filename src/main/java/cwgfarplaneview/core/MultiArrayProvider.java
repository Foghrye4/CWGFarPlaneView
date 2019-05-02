package cwgfarplaneview.core;

public class MultiArrayProvider {
	private static final int ARRAYS_ALLOCATED = 8;
	private int[][] arrays;
	private int currentArraySize;
	private int currentArray = 0;

	public MultiArrayProvider(int arraySizeIn) {
		currentArraySize = arraySizeIn;
		arrays = new int[ARRAYS_ALLOCATED][currentArraySize];
	}

	public int[] getArray(int arraySize) {
		if (arraySize > currentArraySize) {
			currentArraySize = arraySize;
			arrays = new int[ARRAYS_ALLOCATED][currentArraySize];
		}
		return arrays[++currentArray % ARRAYS_ALLOCATED];
	}
}
