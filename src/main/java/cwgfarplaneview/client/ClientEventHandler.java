package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.network;

import cwgfarplaneview.ClientNetworkHandler;
import cwgfarplaneview.ServerNetworkHandler.ServerCommands;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler {

	@SubscribeEvent
	public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() != Minecraft.getMinecraft().player)
			return;
		ClientNetworkHandler cn = (ClientNetworkHandler) network;
		cn.sendCommand(ServerCommands.REQUEST_TERRAIN_DATA);
	}

}
