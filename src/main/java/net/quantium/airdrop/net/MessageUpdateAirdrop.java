package net.quantium.airdrop.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.airdrop.client.manager.ClientAirdropList;

import javax.annotation.Nullable;

public class MessageUpdateAirdrop implements IMessage {

    private int id;
    private int timeLeft;
    private boolean spawned;

    public MessageUpdateAirdrop(int id, int timeLeft, boolean spawned) {
        this.id = id;
        this.timeLeft = timeLeft;
        this.spawned = spawned;
    }

    public MessageUpdateAirdrop() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        timeLeft = buf.readInt();
        spawned = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(timeLeft);
        buf.writeBoolean(spawned);
    }

    public static class Handler implements IMessageHandler<MessageUpdateAirdrop, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageUpdateAirdrop message, MessageContext ctx) {
            ClientAirdropList.Entry e = ClientAirdropList.get(message.id);
            if(e != null) {
                e.setTimeLeft(message.timeLeft);
                e.setSpawned(message.spawned);
            }

            return null;
        }
    }
}
