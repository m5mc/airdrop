package net.quantium.airdrop.manager;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.net.MessageAddAirdrop;
import net.quantium.airdrop.net.MessageRemoveAirdrop;
import net.quantium.airdrop.net.MessageUpdateAirdrop;
import net.quantium.airdrop.net.NetManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class AirdropManager extends WorldSavedData {
    public static final String ID = ModProvider.MODID + ":mgr";

    public static AirdropManager get() {
        MapStorage storage = Objects.requireNonNull(DimensionManager.getWorld(0).getMapStorage());
        AirdropManager instance = (AirdropManager) storage.getOrLoadData(AirdropManager.class, ID);

        if (instance == null) {
            instance = new AirdropManager();
            storage.setData(ID, instance);
        }

        return instance;
    }

    private final HashMap<Integer, AirdropHandle> airdrops = new HashMap<>();

    public AirdropManager(String id) {
        super(id);
    }

    public AirdropManager() {
        super(ID);
    }

    private static final Random RANDOM = new Random();
    private int generateHandleId() {
        int id;
        do id = RANDOM.nextInt();
        while (this.airdrops.containsKey(id));
        return id;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("Airdrops", 10);

        for(int i = 0; i < list.tagCount(); i++) {
            try {
                AirdropHandle handle = new AirdropHandle(list.getCompoundTagAt(i));
                airdrops.put(handle.getId(), handle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();

        for(AirdropHandle handle : airdrops.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            handle.serializeNBT(tag);
            list.appendTag(tag);
        }

        nbt.setTag("Airdrops", list);
        return nbt;
    }

    @Nullable
    public AirdropHandle getAirdropById(int id) {
        return airdrops.get(id);
    }

    public Stream<AirdropHandle> airdrops() {
        return airdrops.values().stream();
    }

    public AirdropHandle createAirdrop(World world, double posX, double posZ, boolean called, int timeLeft, NonNullList<ItemStack> inventory) {
        int id = generateHandleId();
        AirdropHandle handle = new AirdropHandle(id, world, posX, posZ, called, timeLeft, inventory);
        this.airdrops.put(id, handle);
        NetManager.send(buildAdd(handle));
        this.markDirty();

        return handle;
    }

    public void removeAirdrop(AirdropHandle handle) {
        if(airdrops.remove(handle.getId()) != null) {
            NetManager.send(new MessageRemoveAirdrop(handle.getId()));
            this.markDirty();
        }
    }

    private MessageAddAirdrop buildAdd(AirdropHandle handle) {
        return new MessageAddAirdrop(
                handle.getId(),
                handle.getWorld().provider.getDimension(),
                handle.getPositionX(),
                handle.getPositionZ(),
                handle.getTimeLeft(),
                handle.isCalled());
    }

    private void sendFull(EntityPlayerMP ply) {
        for (AirdropHandle h : airdrops.values()) {
            NetManager.sendTo(ply, buildAdd(h));
        }
    }

    private void doUpdate() {
        for(AirdropHandle handle : airdrops.values()) {
            if(handle.doUpdate()) {
                markDirty();
            }

            NetManager.send(new MessageUpdateAirdrop(handle.getId(), handle.getTimeLeft(), handle.hasSpawned()));
        }

        this.markDirty();
    }

    @Mod.EventBusSubscriber
    public static class Events {
        private static int counter = 0;
        @SubscribeEvent
        public static void serverTick(TickEvent.ServerTickEvent e) {
            if(e.phase == TickEvent.Phase.END) {
                if(++counter >= 20) {
                    AirdropManager.get().doUpdate();
                    counter = 0;
                }
            }
        }

        @SubscribeEvent
        public static void serverPlayerConnected(PlayerEvent.PlayerLoggedInEvent e) {
            //AirdropManager.get().createAirdrop(e.player.world, e.player.posX, e.player.posZ, true, 36, NonNullList.create());
            AirdropManager.get().sendFull((EntityPlayerMP)e.player);
        }
    }
}
