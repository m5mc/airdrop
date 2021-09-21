package net.quantium.airdrop.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.quantium.airdrop.ModProvider;

public class NetManager {
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ModProvider.MODID);

    static {
        CHANNEL.registerMessage(MessageAddAirdrop.Handler.class, MessageAddAirdrop.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(MessageRemoveAirdrop.Handler.class, MessageRemoveAirdrop.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(MessageUpdateAirdrop.Handler.class, MessageUpdateAirdrop.class, 3, Side.CLIENT);
    }

    public static void send(IMessage message) {
        CHANNEL.sendToAll(message);
    }

    public static void sendTo(EntityPlayerMP ply, IMessage message) {
        CHANNEL.sendTo(message, ply);
    }
}
