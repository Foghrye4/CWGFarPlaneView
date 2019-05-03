package cwgfarplaneview;

import cwgfarplaneview.client.BlockColors;
import cwgfarplaneview.client.ClientTerrainRenderer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends ServerProxy {
	public final ClientTerrainRenderer terrainRenderer = new ClientTerrainRenderer();
	public final BlockColors blockColors = new BlockColors();
	
	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	};
	
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		World world = event.getWorld();
		if (!world.isRemote)
			return;
		world.provider.setSkyRenderer(terrainRenderer);
		MinecraftForge.EVENT_BUS.register(terrainRenderer);
	}
	
	public void init() {
		blockColors.loadColors();
	}

}
