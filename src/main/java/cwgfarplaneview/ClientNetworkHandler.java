package cwgfarplaneview;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.CWGFarPlaneViewMod.proxy;
import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

import java.io.IOException;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class ClientNetworkHandler extends ServerNetworkHandler {

	@SubscribeEvent
	public void onPacketFromServerToClient(FMLNetworkEvent.ClientCustomPacketEvent event) throws IOException {
		ByteBuf data = event.getPacket().payload();
		PacketBuffer byteBufInputStream = new PacketBuffer(data);
		Minecraft mc = Minecraft.getMinecraft();
		switch (ClientCommands.values()[byteBufInputStream.readByte()]) {
		case RECIEVE_TERRAIN_DATA:
			int amount = byteBufInputStream.readInt();
			TerrainPoint[] tps = new TerrainPoint[amount];
			for (int i = 0; i < amount; i++) {
				int chunkX = byteBufInputStream.readInt();
				int chunkZ = byteBufInputStream.readInt();
				int blockY = byteBufInputStream.readInt();
				try {
					tps[i] = new TerrainPoint(chunkX, chunkZ, blockY, byteBufInputStream.readInt(),
							byteBufInputStream.readInt());
				} catch (IncorrectTerrainDataException e) {
					logger.catching(e);
					break;
				}
			}
			((ClientProxy) proxy).terrainRenderer.terrainSurfaceRenderWorker.schleduleAddToMap(tps);
			break;
		case FLUSH:
			((ClientProxy) proxy).terrainRenderer.terrainSurfaceRenderWorker.clear();
			break;
		case RECIEVE_SEA_LEVEL:
			mc.addScheduledTask(() -> {
				((ClientProxy) proxy).terrainRenderer.setSeaLevel(byteBufInputStream.readInt());
			});
			break;
		case RECIEVE_3DTERRAIN_DATA:
			amount = byteBufInputStream.readInt();
			TerrainPoint3D[] tps3d = new TerrainPoint3D[amount];
			for (int i = 0; i < amount; i++) {
				int cubeX = byteBufInputStream.readInt();
				int cubeY = byteBufInputStream.readInt();
				int cubeZ = byteBufInputStream.readInt();
				byte localX = byteBufInputStream.readByte();
				byte localY = byteBufInputStream.readByte();
				byte localZ = byteBufInputStream.readByte();
				try {
					tps3d[i] = new TerrainPoint3D(cubeX, cubeY, cubeZ, localX, localY, localZ, byteBufInputStream.readInt(),
							byteBufInputStream.readInt());
				} catch (IncorrectTerrainDataException e) {
					logger.catching(e);
					break;
				}
			}
			((ClientProxy) proxy).terrainRenderer.terrain3DShapeRenderWorker.schleduleAddToMap(tps3d);
			break;
		default:
			break;
		}
	}

	public void sendCommand(ServerCommands command) {
		WorldClient world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(command.ordinal());
		byteBufOutputStream.writeInt(player.getEntityId());
		byteBufOutputStream.writeInt(world.provider.getDimension());
		channel.sendToServer(new FMLProxyPacket(byteBufOutputStream, MODID));
	}
}
