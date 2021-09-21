package net.quantium.airdrop.client.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.ModProvider;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

public class ClientAirdropList {
    private static final ConcurrentHashMap<Integer, Entry> airdrops = new ConcurrentHashMap<>();

    public static Iterable<Entry> iterate() {
        return airdrops.values();
    }

    @Nullable
    public static Entry get(int id) {
        return airdrops.get(id);
    }

    public static void add(Entry entry) {
        airdrops.put(entry.getId(), entry);
        MinecraftForge.EVENT_BUS.post(new ClientAirdropAdded(entry));
    }

    public static void remove(int id) {
        Entry entry = airdrops.remove(id);
        if(entry != null) MinecraftForge.EVENT_BUS.post(new ClientAirdropRemoved(entry));
    }

    public static void clear() {
        for(Entry e : airdrops.values())
            MinecraftForge.EVENT_BUS.post(new ClientAirdropRemoved(e));

        airdrops.clear();
    }

    public static class Entry {
        private final int id;
        private final int dimension;
        private final double posX, posZ;
        private final boolean called;
        private int timeLeft;
        private boolean spawned;

        public Entry(int id, int dimension, double posX, double posZ, boolean called, int timeLeft) {
            this.id = id;
            this.dimension = dimension;
            this.posX = posX;
            this.posZ = posZ;
            this.called = called;
            this.timeLeft = timeLeft;
        }

        public int getId() {
            return id;
        }

        public int getDimension() {
            return dimension;
        }

        public double getPositionX() {
            return posX;
        }

        public double getPositionZ() {
            return posZ;
        }

        public boolean isCalled() {
            return called;
        }

        public int getTimeLeft() {
            return timeLeft;
        }

        public void setTimeLeft(int timeLeft) {
            this.timeLeft = timeLeft;
        }

        public boolean isSpawned() {
            return spawned;
        }

        public void setSpawned(boolean spawned) {
            this.spawned = spawned;
        }
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void clientPlayerDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
            ClientAirdropList.clear();
        }
    }
}
