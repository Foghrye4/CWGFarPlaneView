package cwgfarplaneview;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.CWGFarPlaneViewMod.proxy;

import java.io.IOException;

import cwgfarplaneview.world.terrain.IncorrectTerrainDataException;
import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import cwgfarplaneview.world.terrain.volumetric.TerrainCube;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class ClientNetworkHandler extends ServerNetworkHandler {

	@SuppressWarnings("deprecation")
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
		case RECIEVE_TERRAIN_CUBE:
			int cubeX = byteBufInputStream.readInt();
			int cubeY = byteBufInputStream.readInt();
			int cubeZ = byteBufInputStream.readInt();
			TerrainCube cube = new TerrainCube(mc.world, cubeX, cubeY, cubeZ);
			byte[] abyte = byteBufInputStream.readByteArray();
			
			NibbleArray blockData = new NibbleArray(byteBufInputStream.readByteArray());
			NibbleArray add = null;
			NibbleArray add2neid = null;
			if(byteBufInputStream.readBoolean()) {
				add = new NibbleArray(byteBufInputStream.readByteArray());
			}
			if(byteBufInputStream.readBoolean()) {
				add2neid = new NibbleArray(byteBufInputStream.readByteArray());
			}

			for (int i = 0; i < 4096; i++) {
				int x = i & 15;
				int y = i >> 8 & 15;
				int z = i >> 4 & 15;

				int toAdd = add == null ? 0 : add.getFromIndex(i);
				toAdd = (toAdd & 0xF) | (add2neid == null ? 0 : add2neid.getFromIndex(i) << 4);
				int id = (toAdd << 12) | ((abyte[i] & 0xFF) << 4) | blockData.getFromIndex(i);
				cube.storage.getData().set(x, y, z, Block.BLOCK_STATE_IDS.getByValue(id));
			}
			cube.storage.setBlockLight(new NibbleArray(byteBufInputStream.readByteArray()));
			if (byteBufInputStream.readBoolean()) {
				cube.storage.setSkyLight(new NibbleArray(byteBufInputStream.readByteArray()));
			}
			byteBufInputStream.readBytes(cube.biomeData.data);
			byteBufInputStream.readBytes(cube.xLocals.getData());
			byteBufInputStream.readBytes(cube.yLocals.getData());
			byteBufInputStream.readBytes(cube.zLocals.getData());
			((ClientProxy) proxy).terrainRenderer.terrain3DShapeRenderWorker.schleduleAddToMap(cube);
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
