package net.quantium.airdrop.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.inventory.ContainerAirdrop;
import net.quantium.airdrop.ModProvider;

@SideOnly(Side.CLIENT)
public class GuiAirdrop extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/gui/inventory.png");

    public GuiAirdrop(ContainerAirdrop container) {
        super(container);
        this.xSize = 176;
        this.ySize = 120;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(
                this.guiLeft,
                this.guiTop,
                0, 0,
                this.xSize, this.ySize);

        this.mc.fontRenderer.drawString(I18n.format(ModProvider.MODID + ".airdrop"), this.guiLeft + 6, this.guiTop + 5, 4210752, false);
    }
}
