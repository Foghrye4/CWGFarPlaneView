package regencwg.event;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.CubeWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
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
import regencwg.world.BlockReplaceConfig;
import regencwg.world.WorldSavedDataReGenCWG;

public class ReGenCWGEventHandler {

	Int2ObjectMap<List<ReGenArea>> oresAtDimension = new Int2ObjectOpenHashMap<List<ReGenArea>>();
	Int2ObjectMap<BlockReplaceConfig> blockReplaceConfigAtDimension = new Int2ObjectOpenHashMap<BlockReplaceConfig>();
	private static final String FILE_NAME = "custom_generator_settings.json";
	private static final String RC_FILE_NAME = "replace_config.json";

	public ReGenCWGEventHandler() {
		ImmutableList<ReGenArea> list = ImmutableList.<ReGenArea>builder().build();
		oresAtDimension.defaultReturnValue(list);
	}

	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		World world = event.getWorld();
		if (world.isRemote || !(world instanceof WorldServer))
			return;
		String settingString = loadJsonStringFromSaveFolder(event.getWorld(), FILE_NAME);
		WorldSavedDataReGenCWG data = WorldSavedDataReGenCWG.getOrCreateWorldSavedData(world);
		if (settingString != null) {
			CustomGeneratorSettings setting = CustomGeneratorSettings.fromJson(settingString);
			ArrayList<ReGenArea> areas = new ArrayList<ReGenArea>();
			if (!setting.standardOres.isEmpty() || !setting.periodicGaussianOres.isEmpty())
				areas.add(new ReGenArea(setting));
			this.addReGenAreasToList(areas, setting);
			oresAtDimension.put(event.getWorld().provider.getDimension(), areas);
			if (!data.isInitialized()) {
				data.initialize(world);
			}
		}
		File rcSettingFile = getSettingsFile(event.getWorld(), RC_FILE_NAME);
		if (rcSettingFile.exists()) {
			BlockReplaceConfig brc = BlockReplaceConfig.fromFile(rcSettingFile);
			if (!brc.replaceMap.isEmpty()) {
				blockReplaceConfigAtDimension.put(event.getWorld().provider.getDimension(), brc);
			}
		}
	}
	
	private void addReGenAreasToList(List<ReGenArea> areas, CustomGeneratorSettings setting) {
		for (Entry<IntAABB, CustomGeneratorSettings> entry : setting.cubeAreas.entrySet()) {
			if (!entry.getValue().standardOres.isEmpty() || !entry.getValue().periodicGaussianOres.isEmpty())
				areas.add(new ReGenArea(entry.getKey(), entry.getValue()));
			this.addReGenAreasToList(areas, entry.getValue());
		}

	}

	@SubscribeEvent
	public void onCubeWatchEvent(CubeWatchEvent event) {
		World world = (World) event.getWorld();
		WorldSavedDataReGenCWG data = WorldSavedDataReGenCWG.getOrCreateWorldSavedData(world);
		CubePos pos = event.getCubePos();
		if (data.remainingCP.remove(pos)) {
			this.populate(pos, event.getCube(), world);
		}
		data.markDirty();
	}

	public int runReplacer(World world) {
		int dimensionId = world.provider.getDimension();
		if (!blockReplaceConfigAtDimension.containsKey(dimensionId))
			return 0;
		BlockReplaceConfig rc = blockReplaceConfigAtDimension.get(dimensionId);
		if (rc.replaceMap.isEmpty())
			return 0;
		return rc.runReplacer(world);
	}

	public void populate(CubePos pos, ICube cube, World world) {
		List<ReGenArea> ores = oresAtDimension.get(world.provider.getDimension());
		for (ReGenArea area : ores) {
			area.generateIfInArea(world, pos, cube.getBiome(pos.getCenterBlockPos()));
		}
	}

	private static File getSettingsFile(World world, String fileName) {
		File worldDirectory = world.getSaveHandler().getWorldDirectory();
		String subfolder = world.provider.getSaveFolder();
		if (subfolder == null)
			subfolder = "";
		else
			subfolder += "/";
		File settings = new File(worldDirectory, "./" + subfolder + "data/" + ReGenCWGMod.MODID + "/" + fileName);
		return settings;
	}

	public static String loadJsonStringFromSaveFolder(World world, String fileName) {
		File settings = getSettingsFile(world, fileName);
		if (settings.exists()) {
			try (FileReader reader = new FileReader(settings)) {
				CharBuffer sb = CharBuffer.allocate((int) settings.length());
				reader.read(sb);
				sb.flip();
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ReGenCWGMod.logger.info("No settings provided at path:" + settings.toString());
		}
		return null;
	}

	public void addReplacerConfig(int dimension, String string) {
		BlockReplaceConfig brc = BlockReplaceConfig.fromString(string);
		blockReplaceConfigAtDimension.put(dimension, brc);
	}
}
