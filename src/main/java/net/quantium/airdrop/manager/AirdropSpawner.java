package net.quantium.airdrop.manager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.loot.AirdropLevelCondition;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AirdropSpawner extends WorldSavedData {
    public static final String ID = ModProvider.MODID + ":spw";
    public static final int MINUTE = 60;

    public static AirdropSpawner get() {
        MapStorage storage = Objects.requireNonNull(DimensionManager.getWorld(0).getMapStorage());
        AirdropSpawner instance = (AirdropSpawner) storage.getOrLoadData(AirdropSpawner.class, ID);

        if (instance == null) {
            instance = new AirdropSpawner();
            storage.setData(ID, instance);
        }

        return instance;
    }

    public AirdropSpawner(String id) {
        super(id);
    }
    public AirdropSpawner() {
        super(ID);
    }

    private int secondsUntilNextDrop = (int)(ModProvider.config().timeMin * MINUTE);
    private int airdropsDropped = 0;
    private final Random random = new Random();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        airdropsDropped = nbt.getInteger("AirdropsDropped");
        secondsUntilNextDrop = nbt.getInteger("SecondsUntilNextDrop");

        if(!nbt.hasKey("SecondsUntilNextDrop")) {
            secondsUntilNextDrop = (int)(ModProvider.config().timeMin * MINUTE);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("AirdropsDropped", airdropsDropped);
        nbt.setInteger("SecondsUntilNextDrop", secondsUntilNextDrop);

        return nbt;
    }

    public NonNullList<ItemStack> generateDrops(WorldServer world, int lootLevel) {
        AirdropLevelCondition.setLevel(lootLevel);
        LootContext ctx = new LootContext.Builder(world).build();
        LootTable table = world.getLootTableManager().getLootTableFromLocation(ModProvider.LOOT_TABLE);
        List<ItemStack> stacks = table.generateLootForPools(random, ctx);

        NonNullList<ItemStack> list = NonNullList.create();
        for(int i = 0; i < Math.min(9, stacks.size()); i++) {
            list.add(stacks.get(i));
        }

        return list;
    }

    public int getLootLevel() {
        return 0;
    }

    public int getDropTime() {
        return (int)(ModProvider.config().timeDrop * MINUTE);
    }

    @Nullable
    public AirdropHandle dropAirdrop(WorldServer world, int x, int z, int lootLevel, int time, boolean called) {
        if(!world.provider.hasSkyLight()) return null;
        return AirdropManager.get().createAirdrop(world, x + .5d, z + .5d, called, time, generateDrops(world, lootLevel));
    }

    @Nullable
    public AirdropHandle dropNonCalledAirdrop() {
        WorldServer world = DimensionManager.getWorld(0);
        BlockPos spawn = world.getSpawnPoint();

        float radius = (float) (random.nextGaussian() * ModProvider.config().radius);
        float angle = (float) (random.nextFloat() * Math.PI * 2);
        float dx = radius * MathHelper.cos(angle);
        float dz = radius * MathHelper.sin(angle);

        int x = spawn.getX() + (int)dx;
        int z = spawn.getZ() + (int)dz;

        AirdropHandle handle = dropAirdrop(world, x, z, getLootLevel(), getDropTime(), false);

        int minTime = (int)(ModProvider.config().timeMin * MINUTE);
        int maxTime = (int)(ModProvider.config().timeMax * MINUTE);

        if(minTime >= maxTime) {
            maxTime = minTime + 1;
        }

        airdropsDropped++;
        secondsUntilNextDrop = minTime + random.nextInt(maxTime - minTime);

        return handle;
    }

    public boolean canUpdateNonCalledAirdrops() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return false;

        return server.getCurrentPlayerCount() >= 1 && AirdropManager.get().airdrops().allMatch(AirdropHandle::isCalled);
    }

    private void doUpdate() {
        if(canUpdateNonCalledAirdrops()) {
            if(--secondsUntilNextDrop <= 0) {
                dropNonCalledAirdrop();
            }

            markDirty();
        }
    }

    @Mod.EventBusSubscriber
    public static class Events {
        private static int counter = 0;
        @SubscribeEvent
        public static void serverTick(TickEvent.ServerTickEvent e) {
            if(e.phase == TickEvent.Phase.END) {
                if(++counter >= 20) {
                    AirdropSpawner.get().doUpdate();
                    counter = 0;
                }
            }
        }
    }
}
