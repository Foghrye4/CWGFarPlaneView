package cwgfarplaneview.client;

import cwgfarplaneview.util.Vec3f;

public class MutableWeightedNormal {
	public float x = 0.0f;
	public float y = 0.0f;
	public float z = 0.0f;
	private int weight = 0;

	public void add(Vec3f normal) {
		if (weight == 0) {
			x = normal.getX();
			y = normal.getY();
			z = normal.getZ();
		} else {
			x = (x * weight + normal.getX()) / (weight + 1);
			y = (y * weight + normal.getY()) / (weight + 1);
			z = (z * weight + normal.getZ()) / (weight + 1);
		}
		weight++;
	}
	
	public void reset() {
		weight = 0;
	}
}
