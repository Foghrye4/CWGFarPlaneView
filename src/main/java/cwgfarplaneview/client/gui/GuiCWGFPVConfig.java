package cwgfarplaneview.client.gui;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.CWGFarPlaneViewMod.config;

import java.util.ArrayList;
import java.util.List;

import cwgfarplaneview.CWGFarPlaneViewConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiCWGFPVConfig extends GuiConfig {

	public GuiCWGFPVConfig(GuiScreen parent) {
		super(parent, getConfigElements(), MODID, false, false,
				GuiConfig.getAbridgedConfigPath(config.configuration.toString()));
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new DummyConfigElement.DummyCategoryElement("generalConfig", MODID + ".generalConfig",
				GeneralEntry.class));
		list.add(new DummyConfigElement.DummyCategoryElement("clientConfig", MODID + ".clientConfig",
				ClientEntry.class));
		return list;
	}

	public static class GeneralEntry extends CategoryEntry {
		public GeneralEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen() {
			return new GuiConfig(this.owningScreen,
					new ConfigElement(config.configuration.getCategory(CWGFarPlaneViewConfig.CATEGORY_GENERAL))
							.getChildElements(),
					MODID, false, false, GuiConfig.getAbridgedConfigPath(config.configuration.toString()));
		}
	}

	public static class ClientEntry extends CategoryEntry {
		public ClientEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop) {
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen() {
			return new GuiConfig(this.owningScreen,
					new ConfigElement(config.configuration.getCategory(CWGFarPlaneViewConfig.CATEGORY_CLIENT))
							.getChildElements(),
					MODID, false, false, GuiConfig.getAbridgedConfigPath(config.configuration.toString()));
		}
	}
}
