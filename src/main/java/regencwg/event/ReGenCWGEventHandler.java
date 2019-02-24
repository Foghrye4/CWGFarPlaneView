package regencwg.event;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.CubeWatchEvent;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings.IntAABB;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import regencwg.ReGenArea;
import regencwg.ReGenCWGMod;
import regencwg.world.WorldSavedDataReGenCWG;

public class ReGenCWGEventHandler {
	
	Int2ObjectMap<List<ReGenArea>> oresAtDimension = new Int2ObjectOpenHashMap<List<ReGenArea>>();
	
	public ReGenCWGEventHandler() {}
	
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		World world = event.getWorld();
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		String settingString = loadJsonStringFromSaveFolder(event.getWorld());
		if(settingString==null) {
			return;
		}
		CustomGeneratorSettings setting = CustomGeneratorSettings.fromJson(settingString);
		ArrayList<ReGenArea> areas = new ArrayList<ReGenArea>();
		if (!setting.standardOres.isEmpty() || !setting.periodicGaussianOres.isEmpty())
			areas.add(new ReGenArea(setting));
		for (Entry<IntAABB, CustomGeneratorSettings> entry : setting.cubeAreas.entrySet()) {
			if (!entry.getValue().standardOres.isEmpty() || !entry.getValue().periodicGaussianOres.isEmpty())
				areas.add(new ReGenArea(entry.getKey(), entry.getValue()));
		}
		oresAtDimension.put(event.getWorld().provider.getDimension(), areas);
		WorldSavedDataReGenCWG data = WorldSavedDataReGenCWG.getOrCreateWorldSavedData(world);
		if(!data.isInitialized()) {
			data.initialize(world);
		}
	}	
	
	@SubscribeEvent
	public void onCubeWatchEvent(CubeWatchEvent event) {
		World world = (World)event.getWorld();
		WorldSavedDataReGenCWG data = WorldSavedDataReGenCWG.getOrCreateWorldSavedData(world);
		CubePos pos = event.getCubePos();
		if(data.remainingCP.remove(pos)) {
			List<ReGenArea> ores = oresAtDimension.get(world.provider.getDimension());
			for(ReGenArea area:ores) {
				area.generateIfInArea(world, pos, event.getCube().getBiome(pos.getCenterBlockPos()));
			}
			data.markDirty();
		}
	}
	
    public static String loadJsonStringFromSaveFolder(World world) {
		File worldDirectory = world.getSaveHandler().getWorldDirectory();
		String subfolder = world.provider.getSaveFolder();
		if (subfolder == null)
			subfolder = "";
		else
			subfolder += "/";
		File settings = new File(worldDirectory,"./" + subfolder + "data/" + ReGenCWGMod.MODID + "/custom_generator_settings.json");
        if (settings.exists()) {
            try (FileReader reader = new FileReader(settings)){
                CharBuffer sb = CharBuffer.allocate((int) settings.length());
                reader.read(sb);
                sb.flip();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
        	ReGenCWGMod.logger.info("No settings provided at path:" + settings.toString());
        }
        return null;
    }
}
