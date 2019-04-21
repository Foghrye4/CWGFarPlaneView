package cwgfarplaneview.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTerrainPoints implements IMessage {

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}
	
	public static class Handler implements IMessageHandler<PacketTerrainPoints, IMessage> {

		@Override
		public IMessage onMessage(PacketTerrainPoints message, MessageContext ctx) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
