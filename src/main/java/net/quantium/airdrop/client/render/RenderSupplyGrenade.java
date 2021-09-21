package net.quantium.airdrop.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.entity.EntitySupplyGrenade;

@SideOnly(Side.CLIENT)
public class RenderSupplyGrenade extends RenderSnowball<EntitySupplyGrenade> {
    public RenderSupplyGrenade(RenderManager renderManager) {
        super(renderManager, ModProvider.SUPPLY_GRENADE, Minecraft.getMinecraft().getRenderItem());
    }
}
