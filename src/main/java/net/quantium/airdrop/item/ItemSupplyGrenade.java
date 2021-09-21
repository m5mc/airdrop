package net.quantium.airdrop.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.entity.EntitySupplyGrenade;
import net.quantium.airdrop.manager.AirdropSpawner;

public class ItemSupplyGrenade extends Item {

    public ItemSupplyGrenade() {
        this.setCreativeTab(CreativeTabs.MISC);
        this.setRegistryName(new ResourceLocation(ModProvider.MODID, "supply_grenade"));
        this.setUnlocalizedName(ModProvider.MODID + ".supply");

        this.maxStackSize = 4;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            int level = AirdropSpawner.get().getLootLevel();
            if(itemstack.getTagCompound() != null) {
                level = itemstack.getTagCompound().getInteger("LootLevel");
            }

            EntitySupplyGrenade entity = new EntitySupplyGrenade(world);
            entity.setLootLevel(level);
            entity.setPosition(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            entity.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 0.0F);
            world.spawnEntity(entity);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}
