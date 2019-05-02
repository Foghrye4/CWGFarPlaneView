package cwgfarplaneview.core;

public class ThreadLocalLong extends ThreadLocal<Long> {

	@Override
	protected Long initialValue() {
		return 0L;
	}
	
	public long getValue() {
		return super.get();
	}
	
	public void setValue(long value) {
		super.set(value);
	}
}
