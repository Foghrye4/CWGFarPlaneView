package cwgfarplaneview.world.terrain;

import java.util.Iterator;

import javax.annotation.Nullable;

import cwgfarplaneview.util.TerrainUtil;

public class TerrainQuad implements Iterable<TerrainPoint> {

	TerrainPoint terrainPoint00;
	TerrainPoint terrainPoint01;
	TerrainPoint terrainPoint11;
	TerrainPoint terrainPoint10;

	@Nullable
	TerrainQuad child1 = null;
	@Nullable
	TerrainQuad child2 = null;

	public TerrainQuad(TerrainPoint terrainPoint1In, TerrainPoint terrainPoint2In, TerrainPoint terrainPoint3In,
			TerrainPoint terrainPoint4In) {
		terrainPoint00 = terrainPoint1In;
		terrainPoint01 = terrainPoint2In;
		terrainPoint11 = terrainPoint3In;
		terrainPoint10 = terrainPoint4In;
		if (terrainPoint00 == null || terrainPoint01 == null || terrainPoint11 == null || terrainPoint10 == null)
			throw new NullPointerException();
	}

	public void split(TerrainPoint tpIn) {
		if (child1 != null) {
			child1.split(tpIn);
			child2.split(tpIn);
			return;
		}
		if (tpIn.chunkX < terrainPoint00.chunkX || tpIn.chunkX > terrainPoint11.chunkX)
			return;
		if (tpIn.chunkZ < terrainPoint00.chunkZ || tpIn.chunkZ > terrainPoint11.chunkZ)
			return;
		if (tpIn.chunkX > terrainPoint00.chunkX && tpIn.chunkX < terrainPoint11.chunkX) {
			if (tpIn.chunkZ == terrainPoint00.chunkZ) {
				TerrainPoint tp11_01 = TerrainUtil.interpolateBetween(terrainPoint01, terrainPoint11, tpIn.chunkX,
						terrainPoint11.chunkZ);
				child1 = new TerrainQuad(terrainPoint00, terrainPoint01, tp11_01, tpIn);
				child2 = new TerrainQuad(tpIn, tp11_01, terrainPoint11, terrainPoint10);
			} else if (tpIn.chunkZ == terrainPoint11.chunkZ) {
				TerrainPoint tp10_00 = TerrainUtil.interpolateBetween(terrainPoint00, terrainPoint10, tpIn.chunkX,
						terrainPoint00.chunkZ);
				child1 = new TerrainQuad(terrainPoint00, terrainPoint01, tpIn, tp10_00);
				child2 = new TerrainQuad(tp10_00, tpIn, terrainPoint11, terrainPoint10);
			} else {
				TerrainPoint tp11_01 = TerrainUtil.interpolateBetween(terrainPoint01, terrainPoint11, tpIn.chunkX,
						terrainPoint11.chunkZ);
				TerrainPoint tp10_00 = TerrainUtil.interpolateBetween(terrainPoint00, terrainPoint10, tpIn.chunkX,
						terrainPoint00.chunkZ);
				child1 = new TerrainQuad(terrainPoint00, terrainPoint01, tp11_01, tp10_00);
				child2 = new TerrainQuad(tp10_00, tp11_01, terrainPoint11, terrainPoint10);
				child1.split(tpIn);
				child2.split(tpIn);
			}
		} else if (tpIn.chunkZ > terrainPoint00.chunkZ && tpIn.chunkZ < terrainPoint11.chunkZ) {
			if (tpIn.chunkX == terrainPoint00.chunkX) {
				TerrainPoint tp11_10 = TerrainUtil.interpolateBetween(terrainPoint11, terrainPoint10, tpIn.chunkZ,
						terrainPoint11.chunkX);
				child1 = new TerrainQuad(terrainPoint00, tpIn, tp11_10, terrainPoint10);
				child2 = new TerrainQuad(tpIn, terrainPoint01, terrainPoint11, tp11_10);
			} else if (tpIn.chunkX == terrainPoint11.chunkX) {
				TerrainPoint tp01_00 = TerrainUtil.interpolateBetween(terrainPoint00, terrainPoint01, tpIn.chunkZ,
						terrainPoint00.chunkX);
				child1 = new TerrainQuad(terrainPoint00, tp01_00, tpIn, terrainPoint10);
				child2 = new TerrainQuad(tp01_00, terrainPoint10, terrainPoint11, tpIn);
			}
		}
	}

	@Override
	public Iterator<TerrainPoint> iterator() {
		if (child1 != null)
			return new Iterator<TerrainPoint>() {
				Iterator<TerrainPoint> child1Iterator = child1.iterator();
				Iterator<TerrainPoint> child2Iterator = child2.iterator();

				@Override
				public boolean hasNext() {
					return child1Iterator.hasNext() || child2Iterator.hasNext();
				}

				@Override
				public TerrainPoint next() {
					return child1Iterator.hasNext() ? child1Iterator.next() : child2Iterator.next();
				}
			};
		return new Iterator<TerrainPoint>() {
			int index = -1;

			@Override
			public boolean hasNext() {
				return index != 3;
			}

			@Override
			public TerrainPoint next() {
				switch (++index) {
				case 0:
					return terrainPoint00;
				case 1:
					return terrainPoint01;
				case 2:
					return terrainPoint11;
				case 3:
					return terrainPoint10;
				}
				throw new NullPointerException();
			}

		};

	}
}
