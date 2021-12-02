package net.quantium.airdrop.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.manager.AirdropHandle;
import net.quantium.airdrop.manager.AirdropManager;

import javax.annotation.Nullable;

public class EntityAirdrop extends Entity {
    private static final DataParameter<Boolean> PARAM_PARACHUTING = EntityDataManager.createKey(EntityAirdrop.class, DataSerializers.BOOLEAN);

    private final InventoryBasic inventory = new InventoryBasic("Airdrop", true, 9);
    private AirdropHandle handle = null;

    public EntityAirdrop(World world) {
        super(world);
        this.setSize(1.125f, 1.125f);
        this.forceSpawn = true;
    }

    public IInventory getInventory() {
        return this.inventory;
    }

    public boolean isParachuting() {
        return this.dataManager.get(PARAM_PARACHUTING);
    }

    public void setParachuting(boolean para) {
        this.dataManager.set(PARAM_PARACHUTING, para);
    }

    public void setHandle(AirdropHandle handle) {
        this.handle = handle;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(PARAM_PARACHUTING, false);
    }

    @Override
    public void setDead() {
        if(this.handle != null) {
            AirdropManager.get().removeAirdrop(this.handle);
        }

        super.setDead();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!this.isParachuting()) {
            if (!this.world.isRemote) {
                player.openGui(ModProvider.instance(), getEntityId(), world, 0, 0, 0);
            }

            return true;
        }

        return false;
    }

    private float getSubmergedVolume() {
        BlockPos pos = getPosition();

        if (this.world.getBlockState(pos).getMaterial().isLiquid()) {
            return 1f;
        }

        if (this.world.getBlockState(pos.add(0, -1, 0)).getMaterial().isLiquid()) {
            double posy = this.posY + 0.5d;
            float frac = (float) (posy - (int)posy);

            return Math.max(1 - frac * 2 / this.height, 0);
        }

        return 0f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private double velY;

    private void doMove() {
        this.move(MoverType.SELF, 0, velY, 0);

        float sub = getSubmergedVolume();

        float drag = lerp(0.99f, 0.7f, sub);
        float grav = lerp(-0.05f, 0.005f, sub);

        this.velY *= drag;

        if (!this.hasNoGravity()) {
            this.velY += grav;
        }

        if (this.onGround) {
            velY = 0;
        }

        if ((sub > 0 || this.onGround) && this.isParachuting()) {
            this.setParachuting(false);
            if(!this.world.isRemote) this.world.createExplosion(this, posX, posY, posZ, 15f, true);
        }

        if(this.isParachuting()) {
            if(this.world.isRemote) {
                for(int i = 0; i < 100; i++)
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY + i / 10f, posZ, Math.random() - 0.5, velY + 1f, Math.random() - 0.5);
            }
        }

        this.doBlockCollisions();
    }

    @Override
    public void onUpdate() {
        this.doMove();

        super.onUpdate();

        if(!this.world.isRemote) {
            if (this.isWet()) {
                this.extinguish();
            }

            if(this.inventory.isEmpty()) {
                this.setDead();
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("Items", 10);

        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < this.inventory.getSizeInventory()) {
                this.inventory.setInventorySlotContents(slot, new ItemStack(tag));
            }
        }

        this.setParachuting(nbt.getBoolean("Parachuting"));
        this.handle = AirdropManager.get().getAirdropById(nbt.getInteger("Handle"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();

        for(int i = 0; i < this.inventory.getSizeInventory(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                stack.writeToNBT(tag);
                list.appendTag(tag);
            }
        }

        nbt.setTag("Items", list);
        nbt.setBoolean("Parachuting", this.isParachuting());

        if(this.handle != null) {
            nbt.setInteger("Handle", this.handle.getId());
        }
    }

    public boolean canInteractWith(EntityPlayer player) {
        return this.getDistanceSq(player) < 64;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox().expand(0, 255, 0);
    }
}
