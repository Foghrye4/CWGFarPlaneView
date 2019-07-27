package cwgfarplaneview.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import net.minecraft.util.EnumFacing;

public class CubeRenderer {

	public static Set<Triangle> getTriangles(TerrainPoint3D tp000, TerrainPoint3D tp100, TerrainPoint3D tp010, TerrainPoint3D tp001,
			TerrainPoint3D tp110, TerrainPoint3D tp101, TerrainPoint3D tp011, TerrainPoint3D tp111) {
		Set<TerrainPoint3D> allPoints = new HashSet<TerrainPoint3D>();
		Set<Triangle> triangles = new HashSet<Triangle>();
		allPoints.add(tp000);
		allPoints.add(tp100);
		allPoints.add(tp010);
		allPoints.add(tp001);
		allPoints.add(tp110);
		allPoints.add(tp101);
		allPoints.add(tp011);
		allPoints.add(tp111);
		triangles.add(new Triangle(tp000, tp001, tp101, EnumFacing.DOWN.getDirectionVec()));
		triangles.add(new Triangle(tp101, tp100, tp000, EnumFacing.DOWN.getDirectionVec()));
		triangles.add(new Triangle(tp100, tp101, tp111, EnumFacing.EAST.getDirectionVec()));
		triangles.add(new Triangle(tp111, tp110, tp100, EnumFacing.EAST.getDirectionVec()));
		triangles.add(new Triangle(tp000, tp100, tp110, EnumFacing.NORTH.getDirectionVec()));
		triangles.add(new Triangle(tp000, tp010, tp110, EnumFacing.NORTH.getDirectionVec()));
		triangles.add(new Triangle(tp001, tp011, tp111, EnumFacing.SOUTH.getDirectionVec()));
		triangles.add(new Triangle(tp001, tp101, tp111, EnumFacing.SOUTH.getDirectionVec()));
		triangles.add(new Triangle(tp010, tp110, tp111, EnumFacing.UP.getDirectionVec()));
		triangles.add(new Triangle(tp010, tp011, tp111, EnumFacing.UP.getDirectionVec()));
		triangles.add(new Triangle(tp000, tp001, tp011, EnumFacing.WEST.getDirectionVec()));
		triangles.add(new Triangle(tp000, tp010, tp011, EnumFacing.WEST.getDirectionVec()));
		Iterator<TerrainPoint3D> tpi = allPoints.iterator();
		while (tpi.hasNext()) {
			TerrainPoint3D tp = tpi.next();
			if (tp.isVisible())
				continue;
			Set<TerrainPoint3D> newTriangle = new HashSet<TerrainPoint3D>();
			Iterator<Triangle> tri = triangles.iterator();
			while (tri.hasNext()) {
				Triangle triangle = tri.next();
				if (triangle.contain(tp)) {
					tri.remove();
					triangle.addPointsToSetExcept(newTriangle, tp);
				}
			}
			int verticesNum = newTriangle.size();
			assert verticesNum<=3;
			if(verticesNum==3)
				triangles.add(new Triangle(newTriangle, tp));
		}
		return triangles;
	}
}
