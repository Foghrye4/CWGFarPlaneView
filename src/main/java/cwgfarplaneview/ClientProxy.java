package cwgfarplaneview;

import cwgfarplaneview.client.ClientTerrainRenderer;
import cwgfarplaneview.client.block_color.BlockColorsCache;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends ServerProxy {
	public final ClientTerrainRenderer terrainRenderer = new ClientTerrainRenderer();
	public final BlockColorsCache blockColors = new BlockColorsCache();
	
	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		World world = event.getWorld();
		if (!world.isRemote || world.provider.getDimension() != 0)
			return;
		world.provider.setSkyRenderer(terrainRenderer);
		MinecraftForge.EVENT_BUS.register(terrainRenderer);
	}
	
	public void postInit() {
		blockColors.loadBlockTextureMapToRAM();
		terrainRenderer.init();
	}
}
