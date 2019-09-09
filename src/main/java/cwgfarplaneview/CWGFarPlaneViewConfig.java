package cwgfarplaneview;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static cwgfarplaneview.CWGFarPlaneViewMod.*;

import cwgfarplaneview.util.TerrainConfig;

public class CWGFarPlaneViewConfig {
	public static final String CATEGORY_GENERAL = "general";
	public static final String CATEGORY_CLIENT = "client";

	public Configuration configuration;

	public CWGFarPlaneViewConfig(Configuration configuration) {
		this.configuration = configuration;
		syncConfig();
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(MODID)) {
			config.syncConfig();
		}
	}

	private void syncConfig() {
		TerrainConfig.FLAT.setMaxUpdateDistance(configuration.getInt("flat_distance", CATEGORY_GENERAL, 0, 0, 16777216,
				"Control both client side horizont distance and server side terrain shaper max update distance. Set to 0 to disable flat terrain rendering."));
		TerrainConfig.VOLUMETRIC_HORIZONTAL.setMaxUpdateDistance(configuration.getInt("3D_distance_XZ", CATEGORY_GENERAL, 192, 0, 16777216,
				"Control both client side horizont distance and server side terrain shaper max update distance. Set to 0 to disable 3D terrain rendering."));
		TerrainConfig.VOLUMETRIC_VERTICAL.setMaxUpdateDistance(configuration.getInt("3D_distance_Y", CATEGORY_GENERAL, 16, 0, 16777216,
				"Control both client side horizont distance and server side terrain shaper max update distance. Set to 0 to disable 3D terrain rendering."));
		
		TerrainConfig.setClosePlaneRange(configuration.getFloat("close_plane_range", CATEGORY_CLIENT, 16.0f, 0.1f, 1024f,
				"Close plane cutting range. Terrain and water pixels, which are more close to player, than this value, will not be rendered."));
		if (configuration.hasChanged()) {
			configuration.save();
		}
	}
}
