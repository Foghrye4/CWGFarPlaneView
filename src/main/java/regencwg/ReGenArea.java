package regencwg;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.populator.DefaultDecorator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ReGenArea {
	final IntAABB area;
	final DefaultDecorator.Ores ores;
	
	public ReGenArea(CustomGeneratorSettings settingsIn) {
		this(new IntAABB(-300000, -300000, -300000, 300000, 300000, 300000), settingsIn);
	}
	
	public ReGenArea(IntAABB areaIn, CustomGeneratorSettings settingsIn) {
		area = areaIn;
		ores = new DefaultDecorator.Ores(settingsIn);
	}

	public void generateIfInArea(World world, CubePos pos, Biome biome) {
		if (area.contains(pos.getX(), pos.getY(), pos.getZ()))
			ores.generate(world, world.rand, pos, biome);
	}
}
