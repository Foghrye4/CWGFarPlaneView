package cwgfarplaneview.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class CWGFarPlaneViewCoreMod implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"cwgfarplaneview.core.CWGFPVClassTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
