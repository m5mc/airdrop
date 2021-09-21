package net.quantium.airdrop.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.entity.EntityAirdrop;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderAirdrop extends Render<EntityAirdrop> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/entities/airdrop.png");
    private static final ResourceLocation CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/normal.png");

    private final ModelChest chest = new ModelChest();
    private final ModelAirdropCage cage = new ModelAirdropCage();

    public RenderAirdrop(RenderManager manager) {
        super(manager);
    }

    private void renderCage(EntityAirdrop entity) {
        this.bindTexture(TEXTURE);
        this.cage.render(entity, 0, 0, 0, 0, 0, 0.0625f);
    }

    private void renderChest() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1 + 1 / 16d, 1);
        GlStateManager.rotate(180, 1, 0, 0);

        this.bindTexture(CHEST_TEXTURE);
        this.chest.renderAll();

        GlStateManager.popMatrix();
    }

    @Override
    public void doRender(EntityAirdrop entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - .5d, y + .0625d, z - .5d);

        renderCage(entity);
        renderChest();

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityAirdrop entity) {
        return TEXTURE;
    }

    private static class ModelAirdropCage extends ModelBase {

        private final ModelRenderer base;

        public ModelAirdropCage() {
            this.base = new ModelRenderer(this, 0, 0);
            this.base.addBox(-1,   0,   -1,   18, 1,  18);
            this.base.addBox(-1,   15f, -1,   18, 1,  18);
            this.base.addBox(0,   -1,   0,   16, 1,  16);
            this.base.addBox(0,   16f, 0,   16, 1,  16);

            this.base.addBox(0,   1f,  5f,  1,  14, 1);
            this.base.addBox(0,   1f,  10f, 1,  14, 1);
            this.base.addBox(0,   1f,  15f, 1,  14, 1);
            this.base.addBox(5f,  1f,  15f, 1,  14, 1);
            this.base.addBox(10f, 1f,  15f, 1,  14, 1);
            this.base.addBox(15f, 1f,  15f, 1,  14, 1);
            this.base.addBox(15f, 1f,  10f, 1,  14, 1);
            this.base.addBox(15f, 1f,  5f,  1,  14, 1);
            this.base.addBox(15f, 1f,  0,   1,  14, 1);
            this.base.addBox(10f, 1f,  0,   1,  14, 1);
            this.base.addBox(5f,  1f,  0,   1,  14, 1);
            this.base.addBox(0,   1f,  0,   1,  14, 1);
        }

        @Override
        public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            this.base.render(scale);
        }
    }
}
