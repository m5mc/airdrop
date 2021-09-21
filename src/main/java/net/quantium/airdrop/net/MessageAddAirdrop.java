package net.quantium.airdrop.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.airdrop.client.manager.ClientAirdropList;

import javax.annotation.Nullable;

public class MessageAddAirdrop implements IMessage {

    private int id;
    private int dimension;
    private double posX, posZ;
    private int timeLeft;
    private boolean called;

    public MessageAddAirdrop(int id, int dimension, double posX, double posZ, int timeLeft, boolean called) {
        this.id = id;
        this.dimension = dimension;
        this.posX = posX;
        this.posZ = posZ;
        this.timeLeft = timeLeft;
        this.called = called;
    }

    public MessageAddAirdrop() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        dimension = buf.readInt();
        posX = buf.readDouble();
        posZ = buf.readDouble();
        timeLeft = buf.readInt();
        called = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(dimension);
        buf.writeDouble(posX);
        buf.writeDouble(posZ);
        buf.writeInt(timeLeft);
        buf.writeBoolean(called);
    }

    public static class Handler implements IMessageHandler<MessageAddAirdrop, IMessage> {

        @Override
        @Nullable
        public IMessage onMessage(MessageAddAirdrop message, MessageContext ctx) {
            ClientAirdropList.Entry e = new ClientAirdropList.Entry(message.id, message.dimension, message.posX, message.posZ, message.called, message.timeLeft);
            ClientAirdropList.add(e);
            return null;
        }
    }
}
