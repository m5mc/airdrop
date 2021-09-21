package net.quantium.airdrop.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.quantium.airdrop.manager.AirdropHandle;
import net.quantium.airdrop.manager.AirdropSpawner;

public class EntitySupplyGrenade extends EntityThrowable {
    private int lootLevel;

    public EntitySupplyGrenade(World world) {
        super(world);
    }

    public void setLootLevel(int level) {
        lootLevel = level;
    }

    public int getLootLevel() {
        return lootLevel;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("LootLevel", lootLevel);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        lootLevel = compound.getInteger("LootLevel");
    }

    @Override
    public void setDead() {
        super.setDead();

        if(world.isRemote) {
            for(int i = 0; i < 50; i++) {
                float dx = (rand.nextFloat() * 2 - 1) * .1f;
                float dy = (rand.nextFloat() * 1 + 0) * .1f;
                float dz = (rand.nextFloat() * 2 - 1) * .1f;
                this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, dx, dy, dz);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if(!world.isRemote) {
            if(result.typeOfHit == RayTraceResult.Type.BLOCK) {
                AirdropHandle h = AirdropSpawner.get().dropAirdrop(
                        (WorldServer) world,
                        MathHelper.floor(result.hitVec.x),
                        MathHelper.floor(result.hitVec.z),
                        (this.lootLevel + AirdropSpawner.get().getLootLevel()) / 2,
                        60 * 6,
                        true);

                if(h == null) {

                }

                setDead();
            }
        }
    }
}
