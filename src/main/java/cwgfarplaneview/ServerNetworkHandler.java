package cwgfarplaneview;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.registries.GameData;

import static cwgfarplaneview.CWGFarPlaneViewMod.*;

import java.io.IOException;
import java.util.List;

import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ServerNetworkHandler {
	
	protected static FMLEventChannel channel;

	protected MinecraftServer server;

	public enum ServerCommands {
		REQUEST_TERRAIN_DATA;
	}

	public enum ClientCommands {
		RECIEVE_TERRAIN_DATA, FLUSH, RECIEVE_SEA_LEVEL, RECIEVE_3DTERRAIN_DATA;
	}
	
	protected FMLEventChannel getChannel() {
		return channel;
	}
	
	public void setServer(MinecraftServer serverIn) {
		this.server = serverIn;
	}

	public void load() {
		if (channel == null) {
			channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(MODID);
			channel.register(this);
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPacketFromClientToServer(FMLNetworkEvent.ServerCustomPacketEvent event) throws IOException {
		ByteBuf data = event.getPacket().payload();
		PacketBuffer byteBufInputStream = new PacketBuffer(data);
		int playerEntityId;
		int worldDimensionId;
		WorldServer world;
		EntityPlayerMP player;
		switch (ServerCommands.values()[byteBufInputStream.readByte()]) {
		case REQUEST_TERRAIN_DATA:
			// NOOP
			playerEntityId = byteBufInputStream.readInt();
			worldDimensionId = byteBufInputStream.readInt();
			world = server.getWorld(worldDimensionId);
			player = (EntityPlayerMP) world.getEntityByID(playerEntityId);
			if (player == null) {
				logger.error("Player requesting terrain data, but server side player instance is NULL!");
				break;
			}
			break;
		}
	}
	
	public void sendTerrainPointsToClient(EntityPlayerMP player, List<TerrainPoint> tps) {
		ByteBuf bb = Unpooled.buffer(1024);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_TERRAIN_DATA.ordinal());
		byteBufOutputStream.writeInt(tps.size());
		for (TerrainPoint tp : tps) {
			byteBufOutputStream.writeInt(tp.getX());
			byteBufOutputStream.writeInt(tp.getZ());
			byteBufOutputStream.writeInt(tp.blockY);
			byteBufOutputStream.writeInt(GameData.getBlockStateIDMap().get(tp.blockState));
			byteBufOutputStream.writeInt(Biome.getIdForBiome(tp.biome));
		}
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void sendCommandFlush() {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.FLUSH.ordinal());
		getChannel().sendToAll(new FMLProxyPacket(byteBufOutputStream, MODID));
	}

	public void sendAllTerrainPointsToClient(EntityPlayerMP player, XZMap<TerrainPoint> tps) {
		ByteBuf bb = Unpooled.buffer(1024);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_TERRAIN_DATA.ordinal());
		byteBufOutputStream.writeInt(tps.getSize());
		for (TerrainPoint tp : tps) {
			byteBufOutputStream.writeInt(tp.getX());
			byteBufOutputStream.writeInt(tp.getZ());
			byteBufOutputStream.writeInt(tp.blockY);
			byteBufOutputStream.writeInt(GameData.getBlockStateIDMap().get(tp.blockState));
			byteBufOutputStream.writeInt(Biome.getIdForBiome(tp.biome));
		}
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}
	
	public void sendSeaLevel(EntityPlayerMP player, int seaLevel) {
		ByteBuf bb = Unpooled.buffer(36);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_SEA_LEVEL.ordinal());
		byteBufOutputStream.writeInt(seaLevel);
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}

	public void send3DTerrainPointsToClient(EntityPlayerMP player, List<TerrainPoint3D> tps) {
		ByteBuf bb = Unpooled.buffer(1024);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_3DTERRAIN_DATA.ordinal());
		byteBufOutputStream.writeInt(tps.size());
		for (TerrainPoint3D tp : tps) {
			byteBufOutputStream.writeInt(tp.getX());
			byteBufOutputStream.writeInt(tp.getY());
			byteBufOutputStream.writeInt(tp.getZ());
			byteBufOutputStream.writeByte(tp.localX);
			byteBufOutputStream.writeByte(tp.localY);
			byteBufOutputStream.writeByte(tp.localZ);
			byteBufOutputStream.writeInt(GameData.getBlockStateIDMap().get(tp.blockState));
			byteBufOutputStream.writeInt(Biome.getIdForBiome(tp.biome));
		}
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}
	
	public void testTerrainCubeRender(EntityPlayerMP player) {
		ByteBuf bb = Unpooled.buffer(1024);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_3DTERRAIN_DATA.ordinal());
		byteBufOutputStream.writeInt(8);
		int cubeX = player.chunkCoordX;
		int cubeY = player.chunkCoordY;
		int cubeZ = player.chunkCoordZ;
		byteBufOutputStream.writeInt(cubeX);
		byteBufOutputStream.writeInt(cubeY);
		byteBufOutputStream.writeInt(cubeZ);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX+1);
		byteBufOutputStream.writeInt(cubeY);
		byteBufOutputStream.writeInt(cubeZ);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX);
		byteBufOutputStream.writeInt(cubeY+1);
		byteBufOutputStream.writeInt(cubeZ);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX);
		byteBufOutputStream.writeInt(cubeY);
		byteBufOutputStream.writeInt(cubeZ+1);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX+1);
		byteBufOutputStream.writeInt(cubeY+1);
		byteBufOutputStream.writeInt(cubeZ);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX+1);
		byteBufOutputStream.writeInt(cubeY);
		byteBufOutputStream.writeInt(cubeZ+1);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX);
		byteBufOutputStream.writeInt(cubeY+1);
		byteBufOutputStream.writeInt(cubeZ+1);
		this.addBlankTPData(byteBufOutputStream);
		byteBufOutputStream.writeInt(cubeX+1);
		byteBufOutputStream.writeInt(cubeY+1);
		byteBufOutputStream.writeInt(cubeZ+1);
		this.addBlankTPData(byteBufOutputStream);
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}
	
	private void addBlankTPData(PacketBuffer byteBufOutputStream) {
		byteBufOutputStream.writeByte(0);
		byteBufOutputStream.writeByte(0);
		byteBufOutputStream.writeByte(0);
		byteBufOutputStream.writeInt(GameData.getBlockStateIDMap().get(Blocks.STONE.getDefaultState()));
		byteBufOutputStream.writeInt(Biome.getIdForBiome(Biomes.PLAINS));
	}
}