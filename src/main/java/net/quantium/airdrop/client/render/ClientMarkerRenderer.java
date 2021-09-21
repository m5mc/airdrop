package net.quantium.airdrop.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.client.manager.ClientAirdropList;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientMarkerRenderer {

    private static String getInfo(ClientAirdropList.Entry entry) {
        int time = entry.getTimeLeft();

        if(time > 0) {
            int minutes = time / 60;
            int seconds = time - 60 * minutes;

            return String.format("%02d:%02d", minutes, seconds);
        }

        if(entry.isSpawned()) {
            return I18n.format(ModProvider.MODID + ".marker.status.ready");
        }

        int tick = (int) ((Minecraft.getSystemTime() / 1000) % 4);
        return I18n.format(ModProvider.MODID + ".marker.status.dropping." + tick);
    }

    private static String getInfo2(ClientAirdropList.Entry entry) {
        if(entry.isCalled())
            return I18n.format(ModProvider.MODID + ".marker.status.called");
        return I18n.format(ModProvider.MODID + ".marker.status.inbound");
    }

    @SubscribeEvent
    public static void renderMarkers(RenderWorldLastEvent e) {
        World world = Minecraft.getMinecraft().world;
        float worldTime = world.getWorldTime() + e.getPartialTicks();

        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        for(ClientAirdropList.Entry entry : ClientAirdropList.iterate()) {
            pos.setPos(entry.getPositionX(), 64, entry.getPositionZ());

            if(world.provider.getDimension() == entry.getDimension()) {
                if(world.isBlockLoaded(pos)) {
                    renderMarker(entry, world.getPrecipitationHeight(pos).getY(), Minecraft.getMinecraft().getRenderManager(), worldTime);
                }
            }
        }

        pos.release();
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/entities/marker.png");
    private static final ResourceLocation TEXTURE_CROSSHAIR = new ResourceLocation(ModProvider.MODID, "textures/entities/marker_crosshair.png");

    private static void renderMarker(ClientAirdropList.Entry e, float height, RenderManager manager, float worldTime) {
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();

        double deltaX = e.getPositionX() - manager.viewerPosX;
        double deltaY = height + 1 / 16d - manager.viewerPosY;
        double deltaZ = e.getPositionZ() - manager.viewerPosZ;

        GlStateManager.translate(deltaX, deltaY, deltaZ);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        GlStateManager.disableCull();
        renderCrosshair(manager, worldTime);
        renderText(manager, e);
        GlStateManager.enableCull();

        renderBeam(manager, worldTime);

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    public static final int COLOR_HEX = 0xffee22;
    public static final float[] COLOR_FLOAT = new float[] {
            ((COLOR_HEX >> 16) & 0xff) / 255f,
            ((COLOR_HEX >> 8)  & 0xff) / 255f,
            (COLOR_HEX         & 0xff) / 255f
    };

    private static void renderBeam(RenderManager manager, float worldTime) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-.5d, 0, -.5d);
        manager.renderEngine.bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
        TileEntityBeaconRenderer.renderBeamSegment(0, 0, 0, 0, 1,  worldTime, 0, 255, COLOR_FLOAT);
        GlStateManager.popMatrix();
    }

    private static void renderQuad() {
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-0.5, 0, -0.5).tex(0, 0).endVertex();
        buf.pos(-0.5, 0,  0.5).tex(0, 1).endVertex();
        buf.pos( 0.5, 0,  0.5).tex(1, 1).endVertex();
        buf.pos( 0.5, 0, -0.5).tex(1, 0).endVertex();
        Tessellator.getInstance().draw();
    }

    private static void renderCrosshair(RenderManager manager, float worldTime) {
        GlStateManager.pushMatrix();
        GlStateManager.color(COLOR_FLOAT[0], COLOR_FLOAT[1], COLOR_FLOAT[2]);

        //marker
        manager.renderEngine.bindTexture(TEXTURE);
        renderQuad();

        //crosshair
        float scale = MathHelper.sin(worldTime / 10f) * 0.2f + 0.8f;
        GlStateManager.scale(scale, scale, scale);
        manager.renderEngine.bindTexture(TEXTURE_CROSSHAIR);
        renderQuad();

        GlStateManager.popMatrix();
    }

    private static void renderText(RenderManager manager, ClientAirdropList.Entry e) {
        if(manager.getFontRenderer() == null) return; //you dumb bitch ide it CAN be null

        float textScale = 1 / 6f;

        GlStateManager.pushMatrix();
        GlStateManager.scale(1 / 16f, 1 / 16f, 1 / 16f);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-7.5d, 0, 8d);
        GlStateManager.scale(textScale, textScale, textScale);
        GlStateManager.rotate(90, 1, 0, 0);
        manager.getFontRenderer().drawString(getInfo(e), 0, 0, COLOR_HEX, false);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-8d, 0, -7.5d);
        GlStateManager.scale(textScale, textScale, textScale);
        GlStateManager.rotate(90, 1, 0, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        manager.getFontRenderer().drawString(getInfo2(e), 0, 0, COLOR_HEX, false);
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
