package cwgfarplaneview;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static cwgfarplaneview.CWGFarPlaneViewMod.*;

import cwgfarplaneview.util.AddressUtil;

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
		AddressUtil.setMaxUpdateDistance(configuration.getInt("max_update_distance", CATEGORY_GENERAL, 192, 16, 16777216,
				"Control both client side horizont distance and server side terrain shaper max update distance."));
		AddressUtil.setClosePlaneRange(configuration.getFloat("close_plane_range", CATEGORY_CLIENT, 16.0f, 0.1f, 1024f,
				"Close plane cutting range. Terrain and water pixels, which are more close to player, than this value, will not be rendered."));
		if (configuration.hasChanged()) {
			configuration.save();
		}
	}
}
