package cwgfarplaneview.world.terrain.flat;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;

public interface TerrainPointProvider {

	TerrainPoint getTerrainPointAt(int meshX, int meshZ) throws IncorrectTerrainDataException;
}
