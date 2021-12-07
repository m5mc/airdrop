package net.quantium.airdrop.manager;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.quantium.airdrop.entity.EntityAirdrop;

public class AirdropHandle {
    private final int id;
    private final double posX, posZ;
    private final boolean called;
    private final World world;
    private final NonNullList<ItemStack> inventory;
    private int timeLeft;
    private boolean hasSpawned;

    public AirdropHandle(int id, World world, double posX, double posZ, boolean called, int timeLeft, NonNullList<ItemStack> inventory) {
        this.id = id;
        this.world = world;
        this.posX = posX;
        this.posZ = posZ;
        this.called = called;
        this.timeLeft = timeLeft;
        this.inventory = inventory;
        this.hasSpawned = false;
    }

    public AirdropHandle(NBTTagCompound tag) {
        if(!tag.hasKey("Dimension")) throw new RuntimeException("Failed to load airdrop handle: dimension missing");
        if(!tag.hasKey("Id")) throw new RuntimeException("Failed to load airdrop handle: id missing");

        World world = DimensionManager.getWorld(tag.getInteger("Dimension"));

        if(world == null) throw new RuntimeException("Failed to load airdrop handle: invalid dimension");

        this.world = world;
        this.id = tag.getInteger("Id");
        this.posX = tag.getDouble("X");
        this.posZ = tag.getDouble("Z");
        this.called = tag.getBoolean("Called");
        this.timeLeft = tag.getInteger("Time");
        this.hasSpawned = tag.getBoolean("Spawned");

        this.inventory = NonNullList.create();
        ItemStackHelper.loadAllItems(tag, this.inventory);
    }

    public boolean doUpdate() {
        if(this.timeLeft <= 0) {
            if(!this.hasSpawned) {
                if(spawnAirdrop()) {
                    this.hasSpawned = true;
                    return true;
                }
            }
        } else {
            this.timeLeft--;
            return true;
        }

        return false;
    }

    private boolean spawnAirdrop() {
        if(!this.world.isBlockLoaded(new BlockPos(this.posX, 0, this.posZ), false))
            return false;

        EntityAirdrop drop = new EntityAirdrop(this.world);
        drop.setParachuting(true);
        drop.setPosition(this.posX, 254, this.posZ);
        drop.setHandle(this);

        for(int i = 0; i < Math.min(drop.getInventory().getSizeInventory(), this.inventory.size()); i++) {
            drop.getInventory().setInventorySlotContents(i, this.inventory.get(i));
        }

        return this.world.spawnEntity(drop);
    }

    public int getId() {
        return this.id;
    }

    public double getPositionX() {
        return this.posX;
    }

    public double getPositionZ() {
        return this.posZ;
    }

    public boolean isCalled() {
        return this.called;
    }

    public World getWorld() {
        return this.world;
    }

    public int getTimeLeft() {
        return this.timeLeft;
    }

    public boolean hasSpawned() {
        return this.hasSpawned;
    }

    public void serializeNBT(NBTTagCompound tag) {
        tag.setInteger("Dimension", this.world.provider.getDimension());
        tag.setInteger("Id", this.id);
        tag.setDouble("X", this.posX);
        tag.setDouble("Z", this.posZ);
        tag.setBoolean("Called", this.called);
        tag.setInteger("Time", this.timeLeft);
        tag.setBoolean("Spawned", this.hasSpawned);
        ItemStackHelper.saveAllItems(tag, this.inventory);
    }
}
