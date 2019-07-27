package cwgfarplaneview.client;

import java.util.Iterator;
import java.util.Set;

import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import net.minecraft.util.math.Vec3i;

public class Triangle {
	public final TerrainPoint3D[] vertices = new TerrainPoint3D[3];

	public Triangle(TerrainPoint3D tp1, TerrainPoint3D tp2, TerrainPoint3D tp3, Vec3i normal) {
		this.checkFaceDirectionAndSetVertices(tp1, tp2, tp3, normal);
	}
	
	public Triangle(Set<TerrainPoint3D> tps, TerrainPoint3D external) {
		Iterator<TerrainPoint3D> tpi = tps.iterator();
		TerrainPoint3D tp1 = tpi.next();
		TerrainPoint3D tp2 = tpi.next();
		TerrainPoint3D tp3 = tpi.next();
		int bx1 = tp1.getX();
		int bz1 = tp1.getZ();
		int by1 = tp1.getY();
		int bx2 = tp2.getX();
		int bz2 = tp2.getZ();
		int by2 = tp2.getY();
		int bx3 = tp3.getX();
		int bz3 = tp3.getZ();
		int by3 = tp3.getY();
		int mx = bx1 + bx2 + bx3;
		int my = by1 + by2 + by3;
		int mz = bz1 + bz2 + bz3;
		int nx = external.getX() * 3 - mx;
		int ny = external.getY() * 3 - my;
		int nz = external.getZ() * 3 - mz;
		this.checkFaceDirectionAndSetVertices(tp1, tp2, tp3, new Vec3i(nx, ny, nz));
	}

	private void checkFaceDirectionAndSetVertices(TerrainPoint3D tp1, TerrainPoint3D tp2, TerrainPoint3D tp3, Vec3i normal) {
		int bx1 = tp1.getX();
		int bz1 = tp1.getZ();
		int by1 = tp1.getY();
		int bx2 = tp2.getX();
		int bz2 = tp2.getZ();
		int by2 = tp2.getY();
		int bx3 = tp3.getX();
		int bz3 = tp3.getZ();
		int by3 = tp3.getY();
		int v1x = bx1 - bx2;
		int v1y = by1 - by2;
		int v1z = bz1 - bz2;
		int v2x = bx3 - bx2;
		int v2y = by3 - by2;
		int v2z = bz3 - bz2;
		int nx = v1y * v2z - v1z * v2y;
		int ny = v1z * v2x - v1x * v2z;
		int nz = v1x * v2y - v1y * v2z;
		int nn = nx * normal.getX() + ny * normal.getY() + nz * normal.getZ();
		if (nn < 0) {
			vertices[0] = tp3;
			vertices[1] = tp2;
			vertices[2] = tp1;
		} else {
			vertices[0] = tp1;
			vertices[1] = tp2;
			vertices[2] = tp3;
		}
	}


	public boolean contain(TerrainPoint3D point) {
		for (TerrainPoint3D tp : vertices) {
			if (tp.equals(point))
				return true;
		}
		return false;
	}

	public void addPointsToSetExcept(Set<TerrainPoint3D> set, TerrainPoint3D exclude) {
		for (TerrainPoint3D tp : vertices) {
			if (!tp.equals(exclude))
				set.add(tp);
		}
	}
}
