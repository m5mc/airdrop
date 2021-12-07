package net.quantium.airdrop.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.ModProvider;
import net.quantium.airdrop.client.manager.ClientAirdropAdded;
import net.quantium.airdrop.client.manager.ClientAirdropList;
import net.quantium.airdrop.client.manager.ClientAirdropRemoved;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientPopupRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/gui/popup.png");
    private static final List<Popup> popups = new ArrayList<>();

    private static String getInfo2(ClientAirdropList.Entry entry) {
        int time = entry.getTimeLeft();

        if(time > 0) {
            int minutes = time / 60;
            int seconds = time - 60 * minutes;

            return I18n.format(ModProvider.MODID + ".popup.status.waiting", String.format("%02d:%02d", minutes, seconds), (int)entry.getPositionX(), (int)entry.getPositionZ());
        }

        int tick = (int) ((Minecraft.getSystemTime() / 1000) % 4);
        return I18n.format(ModProvider.MODID + ".popup.status.ready." + tick, (int)entry.getPositionX(), (int)entry.getPositionZ());
    }

    private static String getInfo(ClientAirdropList.Entry entry) {
        if(entry.isCalled())
            return I18n.format(ModProvider.MODID + ".popup.status.called");
        return I18n.format(ModProvider.MODID + ".popup.status.inbound");
    }

    @SubscribeEvent
    public static void popupAdd(ClientAirdropAdded e) {
        popups.add(new Popup(e.entry));
    }

    @SubscribeEvent
    public static void popupRemove(ClientAirdropRemoved e) {
        popups.stream().filter(a -> a.entry == e.entry).findFirst().ifPresent(a -> a.closed = true);
    }

    private static boolean mouse = false;

    @SubscribeEvent
    public static void renderPopups(RenderGameOverlayEvent e) {
        if(e.getType() != RenderGameOverlayEvent.ElementType.HOTBAR || e.isCanceled()) {
            return;
        }

        boolean press = !mouse && Mouse.isButtonDown(0);
        mouse = Mouse.isButtonDown(0);

        for(int i = 0; i < popups.size(); i++) {
            Popup entry = popups.get(i);

            if(renderPopup(entry, e.getResolution(), i * 3) && i == popups.size() - 1 && press) {
                entry.closed = true;
            }
        }

        popups.removeIf(p -> p.closed && p.animation <= 1e-16);
    }

    private static boolean renderPopup(Popup entry, ScaledResolution resolution, int offset) {
        int w = 160;
        int h =  32;
        int pad = 0;

        int x = resolution.getScaledWidth() - pad - w + (int)((w + pad) * (1 - entry.animation));
        int y = pad + offset;

        int tx = x + 36;
        int ty = y + 8;

        int cx = x + 147;
        int cy = y + 6;
        int cw = 7;
        int ch = 7;

        int mx = Mouse.getX() / resolution.getScaleFactor();
        int my = resolution.getScaledHeight() - Mouse.getY() / resolution.getScaleFactor();

        boolean close = false;

        if(!entry.closed && entry.animation < 1) {
            entry.animation += Math.min(1 - entry.animation, 0.03f);
        }

        if(entry.closed && entry.animation > 0) {
            entry.animation -= Math.min(entry.animation, 0.03f);
        }

        GlStateManager.disableDepth();
        GlStateManager.color(1, 1, 1);

        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(x, y, 0, 0, w, h);

        if(mx >= cx && my >= cy && mx <= cx + cw && my <= cy + ch) {
            close = true;
            drawTexturedModalRect(cx, cy, w, 0, cw, ch);
        }

        if(((Minecraft.getSystemTime() / 1000) & 1) == 0) {
            drawTexturedModalRect(x + 5, y + 3, 0, h, 26, 25);
        }

        mc.fontRenderer.drawString(getInfo(entry.entry), tx, ty, 0x333333, false);
        mc.fontRenderer.drawString(getInfo2(entry.entry), tx, ty + mc.fontRenderer.FONT_HEIGHT, 0x333333, false);

        GlStateManager.enableDepth();

        return close;
    }

    private static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x + 0), y + height, (double)0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + 0), (double)0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
}

    public static class Popup {
        public final ClientAirdropList.Entry entry;
        public float animation = 0;
        public boolean closed = false;

        public Popup(ClientAirdropList.Entry entry) {
            this.entry = entry;
        }
    }
}
