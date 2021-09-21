package net.quantium.airdrop.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.airdrop.client.manager.ClientAirdropList;

import javax.annotation.Nullable;

public class MessageRemoveAirdrop implements IMessage {

    private int id;
    public MessageRemoveAirdrop(int id) {
        this.id = id;
    }

    public MessageRemoveAirdrop() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public static class Handler implements IMessageHandler<MessageRemoveAirdrop, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageRemoveAirdrop message, MessageContext ctx) {
            ClientAirdropList.remove(message.id);
            return null;
        }
    }
}
