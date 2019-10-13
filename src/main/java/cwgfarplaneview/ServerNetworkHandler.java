package cwgfarplaneview;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.CWGFarPlaneViewMod.logger;

import java.io.IOException;
import java.util.List;

import cwgfarplaneview.world.terrain.flat.TerrainPoint;
import cwgfarplaneview.world.terrain.volumetric.TerrainCube;
import cwgfarplaneview.world.terrain.volumetric.TerrainPoint3D;
import io.github.opencubicchunks.cubicchunks.api.util.XZMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.registries.GameData;

public class ServerNetworkHandler {
	
	protected static FMLEventChannel channel;

	protected MinecraftServer server;

	public enum ServerCommands {
		REQUEST_TERRAIN_DATA;
	}

	public enum ClientCommands {
		RECIEVE_TERRAIN_DATA, FLUSH, RECIEVE_SEA_LEVEL, RECIEVE_3DTERRAIN_DATA, RECIEVE_TERRAIN_CUBE;
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
			byteBufOutputStream.writeInt(Biome.getIdForBiome(tp.getBiome()));
		}
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}
	
	public void testTerrainCubeRender(EntityPlayerMP player) {
		int tCubeX = player.chunkCoordX>>4;
		int tCubeY = player.chunkCoordY>>4;
		int tCubeZ = player.chunkCoordZ>>4;
		tCubeX++;
		TerrainCube cube = new TerrainCube(player.world, tCubeX, tCubeY, tCubeZ);
		for (int ix = 0; ix < 16; ix++)
			for (int iy = 0; iy < 16; iy++)
				for (int iz = 0; iz < 16; iz++)
					cube.setBlock(ix, iy, iz, Blocks.PURPUR_BLOCK.getDefaultState());
		this.sendTerrainCubeToPlayer(player, cube);
	}
	
	@SuppressWarnings("deprecation")
	public void sendTerrainCubeToPlayer(EntityPlayerMP player, TerrainCube cube) {
		ByteBuf bb = Unpooled.buffer(1024);
		PacketBuffer byteBufOutputStream = new PacketBuffer(bb);
		byteBufOutputStream.writeByte(ClientCommands.RECIEVE_TERRAIN_CUBE.ordinal());
		byteBufOutputStream.writeInt(cube.x);
		byteBufOutputStream.writeInt(cube.y);
		byteBufOutputStream.writeInt(cube.z);
		
		ExtendedBlockStorage ebs = cube.storage;
        byte[] abyte = new byte[4096];
        NibbleArray data = new NibbleArray();
        NibbleArray add = null;
        NibbleArray add2neid = null;

        for (int i = 0; i < 4096; ++i) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;

            int id = Block.BLOCK_STATE_IDS.get(ebs.getData().get(x, y, z));

            int in1 = (id >> 12) & 0xF;
            int in2 = (id >> 16) & 0xF;

            if (in1 != 0) {
                if (add == null) {
                    add = new NibbleArray();
                }
                add.setIndex(i, in1);
            }
            if (in2 != 0) {
                if (add2neid == null) {
                    add2neid = new NibbleArray();
                }
                add2neid.setIndex(i, in2);
            }

            abyte[i] = (byte) (id >> 4 & 255);
            data.setIndex(i, id & 15);
        }
        
        byteBufOutputStream.writeByteArray(abyte);
        byteBufOutputStream.writeByteArray(data.getData());
        byteBufOutputStream.writeBoolean(add != null);
		if (add != null) {
			byteBufOutputStream.writeByteArray(add.getData());
		}
		byteBufOutputStream.writeBoolean(add2neid != null);
		if (add2neid != null) {
			byteBufOutputStream.writeByteArray(add2neid.getData());
		}
		byteBufOutputStream.writeByteArray(ebs.getBlockLight().getData());
		byteBufOutputStream.writeBoolean(ebs.getSkyLight() != null);
		if (ebs.getSkyLight() != null) {
			byteBufOutputStream.writeByteArray(ebs.getSkyLight().getData());
		}
		byteBufOutputStream.writeBytes(cube.biomeData.data);
		byteBufOutputStream.writeBytes(cube.xLocals.getData());
		byteBufOutputStream.writeBytes(cube.yLocals.getData());
		byteBufOutputStream.writeBytes(cube.zLocals.getData());
		getChannel().sendTo(new FMLProxyPacket(byteBufOutputStream, MODID), player);
	}
}