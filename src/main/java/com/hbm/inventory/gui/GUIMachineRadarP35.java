package com.hbm.inventory.gui;

import java.util.Arrays;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.NBTControlPacket;
import com.hbm.tileentity.machine.TileEntityMachineRadarP35;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.entity.RadarEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class GUIMachineRadarP35 extends GuiScreen {

    public static final ResourceLocation texture = new ResourceLocation(
            RefStrings.MODID + ":textures/gui/machine/gui_radar_nt.png");

    protected TileEntityMachineRadarP35 radar;
    protected int xSize = 216;
    protected int ySize = 234;
    protected int guiLeft;
    protected int guiTop;

    public int lastMouseX;
    public int lastMouseY;

    private float zoomLevel = 1.0f;
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 20.0f;
    private static final float ZOOM_STEP = 1.0f;

    private static final double MIN_VECTOR_LENGTH = 15.0;
    private static final int VECTOR_MULTIPLIER = 10;

    private static final int SCREEN_RADIUS = 96;
    private static final int SCREEN_CENTER_X = 108;
    private static final int SCREEN_CENTER_Y = 117;

    public GUIMachineRadarP35(TileEntityMachineRadarP35 tile) {
        this.radar = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            if (checkClick(mouseX, mouseY, 8, 17, 200, 200)) {
                if (scroll > 0) {
                    zoomLevel = Math.min(MAX_ZOOM, zoomLevel + ZOOM_STEP);
                } else {
                    zoomLevel = Math.max(MIN_ZOOM, zoomLevel - ZOOM_STEP);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);

        String cmd = null;
        if (checkClick(x, y, -10, 128, 8, 8))
            cmd = "red";
        if (checkClick(x, y, -10, 138, 8, 8))
            cmd = "map";
        if (checkClick(x, y, -10, 158, 8, 8))
            cmd = "gui1";
        if (checkClick(x, y, -10, 178, 8, 8))
            cmd = "clear";

        if (cmd != null) {
            mc.getSoundHandler()
                    .playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
            NBTTagCompound data = new NBTTagCompound();
            data.setBoolean(cmd, true);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radar.xCoord, radar.yCoord, radar.zCoord));
        }
    }

    private int getDisplayRange() {
        return (int) (radar.getRange() / zoomLevel);
    }

    private double worldToScreenX(double worldX) {
        int displayRange = getDisplayRange();
        return (worldX - radar.xCoord) / ((double) displayRange * 2 + 1) * 192.0;
    }

    private double worldToScreenZ(double worldZ) {
        int displayRange = getDisplayRange();
        return (worldZ - radar.zCoord) / ((double) displayRange * 2 + 1) * 192.0;
    }

    private boolean isOnScreen(double screenX, double screenZ) {
        return screenX * screenX + screenZ * screenZ <= SCREEN_RADIUS * SCREEN_RADIUS;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GL11.glDisable(GL11.GL_LIGHTING);
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GL11.glEnable(GL11.GL_LIGHTING);

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    private void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (checkClick(mouseX, mouseY, 8, 221, 200, 7)) {
            this.func_146283_a(Arrays.asList(
                    BobMathUtil.getShortNumber(radar.power) + "/" + BobMathUtil.getShortNumber(radar.maxPower) + "HE"),
                    mouseX, mouseY);
        }

        if(com.hbm.tileentity.machine.TileEntityMachineRadarNT.radarHorizonEnabled) {
            double multiplier = radar.calculateVisibilityMultiplier();
            int effectiveRange = radar.calculateEffectiveRange();
            int baseRange = radar.getRange();
            
            if(checkClick(mouseX, mouseY, 8, 8, 80, 8)) {
                int visibility = (int)(multiplier * 100);
                this.func_146283_a(Arrays.asList(
                    I18nUtil.resolveKey("radar.visibility") + ": " + visibility + "%",
                    I18nUtil.resolveKey("radar.effectiveRange") + ": " + effectiveRange + " / " + baseRange
                ), mouseX, mouseY);
            }
        }

        if (checkClick(mouseX, mouseY, -10, 88, 8, 8))
            this.func_146283_a(Arrays.asList(I18nUtil.resolveKeyArray("radar.detectMissiles")), mouseX, mouseY);
        if (checkClick(mouseX, mouseY, -10, 128, 8, 8))
            this.func_146283_a(Arrays.asList(I18nUtil.resolveKeyArray("radar.redMode")), mouseX, mouseY);
        if (checkClick(mouseX, mouseY, -10, 138, 8, 8))
            this.func_146283_a(Arrays.asList(I18nUtil.resolveKeyArray("radar.showMap")), mouseX, mouseY);
        if (checkClick(mouseX, mouseY, -10, 158, 8, 8))
            this.func_146283_a(Arrays.asList(I18nUtil.resolveKeyArray("radar.toggleGui")), mouseX, mouseY);
        if (checkClick(mouseX, mouseY, -10, 178, 8, 8))
            this.func_146283_a(Arrays.asList(I18nUtil.resolveKeyArray("radar.clearMap")), mouseX, mouseY);

        if (!radar.entries.isEmpty()) {
            for (RadarEntry m : radar.entries) {
                double screenX = worldToScreenX(m.posX);
                double screenZ = worldToScreenZ(m.posZ);
                if (!isOnScreen(screenX, screenZ))
                    continue;

                int x = guiLeft + SCREEN_CENTER_X + (int) screenX;
                int z = guiTop + SCREEN_CENTER_Y + (int) screenZ;

                if (mouseX + 5 > x && mouseX - 4 <= x && mouseY + 5 > z && mouseY - 4 <= z) {
                    double speed = m.getSpeedBPS();
                    String[] text = new String[] {
                            I18nUtil.resolveKey(m.unlocalizedName),
                            m.posX + " / " + m.posZ,
                            "Alt.: " + m.posY,
                            "Speed: " + String.format("%.0f", speed) + " m/s"
                    };
                    this.func_146283_a(Arrays.asList(text), x, z);
                    return;
                }
            }
        }

        if (checkClick(mouseX, mouseY, 8, 17, 200, 200)) {
            int displayRange = getDisplayRange();
            int tX = (int) ((mouseX - guiLeft - SCREEN_CENTER_X) * ((double) displayRange * 2 + 1) / 192D
                    + radar.xCoord);
            int tZ = (int) ((mouseY - guiTop - SCREEN_CENTER_Y) * ((double) displayRange * 2 + 1) / 192D
                    + radar.zCoord);
            String zoomInfo = zoomLevel > 1.0f ? " (x" + String.format("%.0f", zoomLevel) + ")" : "";
            this.func_146283_a(Arrays.asList(tX + " / " + tZ + zoomInfo), mouseX, mouseY);
        }
    }

    private void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawTexturedModalRect(guiLeft - 14, guiTop + 84, 224, 0, 14, 66);
        drawTexturedModalRect(guiLeft - 14, guiTop + 154, 224, 66, 14, 36);

        if (radar.power > 0) {
            int i = (int) (radar.power * 200 / radar.maxPower);
            drawTexturedModalRect(guiLeft + 8, guiTop + 221, 0, 234, i, 16);
        }

        if(com.hbm.tileentity.machine.TileEntityMachineRadarNT.radarHorizonEnabled) {
            double multiplier = radar.calculateVisibilityMultiplier();
            int visibility = (int)(multiplier * 100);
            String visText = visibility + "%";
            int color = 0x00ff00;
            if(multiplier < 1.0) {
                if(multiplier >= 0.75) {
                    color = 0xffff00;
                } else if(multiplier >= 0.5) {
                    color = 0xff8800;
                } else {
                    color = 0xff0000;
                }
            }
            this.fontRendererObj.drawString(visText, guiLeft + 10, guiTop + 9, color);
        }

        drawTexturedModalRect(guiLeft - 10, guiTop + 88, 238, 4, 8, 8);
        if (radar.redMode ^ (radar.jammed && radar.getWorldObj().rand.nextBoolean()))
            drawTexturedModalRect(guiLeft - 10, guiTop + 128, 238, 44, 8, 8);
        if (radar.showMap ^ (radar.jammed && radar.getWorldObj().rand.nextBoolean()))
            drawTexturedModalRect(guiLeft - 10, guiTop + 138, 238, 54, 8, 8);

        if (radar.power < radar.consumption)
            return;

        if (radar.jammed) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    drawTexturedModalRect(guiLeft + 8 + i * 40, guiTop + 17 + j * 40, 216,
                            118 + radar.getWorldObj().rand.nextInt(81), 40, 40);
                }
            }
            return;
        }

        enableScissor(guiLeft + 8, guiTop + 17, 200, 200);

        drawRadarBackground();
        drawGrid();

        if (radar.showMap) {
            drawMap();
        }

        drawSweepLine(f);

        if (!radar.entries.isEmpty()) {
            drawVelocityVectors();
            drawBlips();
        }

        disableScissor();

        if (zoomLevel > 1.0f) {
            String zoomText = "x" + String.format("%.0f", zoomLevel);
            this.fontRendererObj.drawStringWithShadow(zoomText, guiLeft + 10, guiTop + 20, 0x00FF00);
        }
    }

    private void enableScissor(int x, int y, int width, int height) {
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = res.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (y + height) * scale, width * scale, height * scale);
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawRadarBackground() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA(0, 20, 0, 255);
        tess.addVertex(guiLeft + 8, guiTop + 217, this.zLevel);
        tess.addVertex(guiLeft + 208, guiTop + 217, this.zLevel);
        tess.addVertex(guiLeft + 208, guiTop + 17, this.zLevel);
        tess.addVertex(guiLeft + 8, guiTop + 17, this.zLevel);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawGrid() {
        int displayRange = getDisplayRange();
        int gridSpacing = displayRange / 4;
        if (gridSpacing < 1)
            gridSpacing = 1;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1.0F);

        Tessellator tess = Tessellator.instance;

        for (int ring = 1; ring <= 4; ring++) {
            double ringRadius = (double) (gridSpacing * ring) / ((double) displayRange * 2 + 1) * 192.0;
            tess.startDrawing(GL11.GL_LINE_LOOP);
            tess.setColorRGBA(0, 100, 0, 150);
            for (int i = 0; i < 64; i++) {
                double angle = i * Math.PI * 2 / 64;
                tess.addVertex(guiLeft + SCREEN_CENTER_X + Math.cos(angle) * ringRadius,
                        guiTop + SCREEN_CENTER_Y + Math.sin(angle) * ringRadius, this.zLevel);
            }
            tess.draw();
        }

        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(0, 100, 0, 150);
        tess.addVertex(guiLeft + SCREEN_CENTER_X - 100, guiTop + SCREEN_CENTER_Y, this.zLevel);
        tess.addVertex(guiLeft + SCREEN_CENTER_X + 100, guiTop + SCREEN_CENTER_Y, this.zLevel);
        tess.addVertex(guiLeft + SCREEN_CENTER_X, guiTop + SCREEN_CENTER_Y - 100, this.zLevel);
        tess.addVertex(guiLeft + SCREEN_CENTER_X, guiTop + SCREEN_CENTER_Y + 100, this.zLevel);
        tess.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        this.fontRendererObj.drawString("N", guiLeft + SCREEN_CENTER_X - 3,
                guiTop + SCREEN_CENTER_Y - SCREEN_RADIUS + 5, 0x00AA00);
        this.fontRendererObj.drawString("S", guiLeft + SCREEN_CENTER_X - 3,
                guiTop + SCREEN_CENTER_Y + SCREEN_RADIUS - 12, 0x00AA00);
        this.fontRendererObj.drawString("E", guiLeft + SCREEN_CENTER_X + SCREEN_RADIUS - 10,
                guiTop + SCREEN_CENTER_Y - 4, 0x00AA00);
        this.fontRendererObj.drawString("W", guiLeft + SCREEN_CENTER_X - SCREEN_RADIUS + 3,
                guiTop + SCREEN_CENTER_Y - 4, 0x00AA00);

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void drawMap() {
        Tessellator tess = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tess.startDrawingQuads();
        for (int i = 0; i < 40_000; i++) {
            int iX = i % 200;
            int iZ = i / 200;
            byte b = radar.map[i];
            if (b > 0) {
                int color = ((b - 50) * 255 / 78) << 8;
                tess.setColorOpaque_I(color);
                tess.addVertex(guiLeft + 8 + iX, guiTop + 18 + iZ, this.zLevel);
                tess.addVertex(guiLeft + 9 + iX, guiTop + 18 + iZ, this.zLevel);
                tess.addVertex(guiLeft + 9 + iX, guiTop + 17 + iZ, this.zLevel);
                tess.addVertex(guiLeft + 8 + iX, guiTop + 17 + iZ, this.zLevel);
            }
        }
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawSweepLine(float f) {
        Vec3 tr = Vec3.createVectorHelper(SCREEN_RADIUS, 0, 0);
        Vec3 tl = Vec3.createVectorHelper(SCREEN_RADIUS, 0, 0);
        Vec3 bl = Vec3.createVectorHelper(0, -5, 0);
        float rot = (float) -Math.toRadians(radar.prevRotation + (radar.rotation - radar.prevRotation) * f + 180F);
        tr.rotateAroundZ(rot);
        tl.rotateAroundZ(rot + 0.25F);
        bl.rotateAroundZ(rot);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_I(0x00ff00, 0);
        tess.addVertex(guiLeft + SCREEN_CENTER_X, guiTop + SCREEN_CENTER_Y, this.zLevel);
        tess.setColorRGBA_I(0x00ff00, 255);
        tess.addVertex(guiLeft + SCREEN_CENTER_X + tr.xCoord, guiTop + SCREEN_CENTER_Y + tr.yCoord, this.zLevel);
        tess.setColorRGBA_I(0x00ff00, 0);
        tess.addVertex(guiLeft + SCREEN_CENTER_X + tl.xCoord, guiTop + SCREEN_CENTER_Y + tl.yCoord, this.zLevel);
        tess.setColorRGBA_I(0x00ff00, 0);
        tess.addVertex(guiLeft + SCREEN_CENTER_X + bl.xCoord, guiTop + SCREEN_CENTER_Y + bl.yCoord, this.zLevel);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_FLAT);
    }

    private void drawBlips() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        for (RadarEntry m : radar.entries) {
            double screenX = worldToScreenX(m.posX);
            double screenZ = worldToScreenZ(m.posZ);
            if (!isOnScreen(screenX, screenZ))
                continue;

            drawTexturedModalRectDouble(guiLeft + SCREEN_CENTER_X + screenX - 4, guiTop + SCREEN_CENTER_Y + screenZ - 4,
                    216, 8 * m.blipLevel, 8, 8);
        }
    }

    private void drawVelocityVectors() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(3.0F);

        Tessellator tess = Tessellator.instance;
        int displayRange = getDisplayRange();

        for (RadarEntry entry : radar.entries) {
            double screenX = worldToScreenX(entry.posX);
            double screenZ = worldToScreenZ(entry.posZ);
            if (!isOnScreen(screenX, screenZ))
                continue;

            double velX = entry.getMotionX();
            double velZ = entry.getMotionZ();
            double speed = entry.getSpeedBPS();

            if (speed > 0.1) {
                double velScreenX = velX * 20 / ((double) displayRange * 2 + 1) * 192.0 * VECTOR_MULTIPLIER;
                double velScreenZ = velZ * 20 / ((double) displayRange * 2 + 1) * 192.0 * VECTOR_MULTIPLIER;

                double vectorLength = Math.sqrt(velScreenX * velScreenX + velScreenZ * velScreenZ);
                if (vectorLength < MIN_VECTOR_LENGTH && vectorLength > 0.01) {
                    double scale = MIN_VECTOR_LENGTH / vectorLength;
                    velScreenX *= scale;
                    velScreenZ *= scale;
                }

                tess.startDrawing(GL11.GL_LINES);
                int g = speed < 50 ? 255 : (speed < 200 ? 180 : 80);
                tess.setColorRGBA(255, g, 0, 255);
                tess.addVertex(guiLeft + SCREEN_CENTER_X + screenX, guiTop + SCREEN_CENTER_Y + screenZ, this.zLevel);
                tess.setColorRGBA(255, g / 2, 0, 100);
                tess.addVertex(guiLeft + SCREEN_CENTER_X + screenX + velScreenX,
                        guiTop + SCREEN_CENTER_Y + screenZ + velScreenZ, this.zLevel);
                tess.draw();
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public void drawTexturedModalRectDouble(double x, double y, int sourceX, int sourceY, int sizeX, int sizeY) {
        float f = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + sizeY, this.zLevel, sourceX * f, (sourceY + sizeY) * f);
        tessellator.addVertexWithUV(x + sizeX, y + sizeY, this.zLevel, (sourceX + sizeX) * f, (sourceY + sizeY) * f);
        tessellator.addVertexWithUV(x + sizeX, y, this.zLevel, (sourceX + sizeX) * f, sourceY * f);
        tessellator.addVertexWithUV(x, y, this.zLevel, sourceX * f, sourceY * f);
        tessellator.draw();
    }

    protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
        return guiLeft + left <= x && guiLeft + left + sizeX > x && guiTop + top < y && guiTop + top + sizeY >= y;
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.thePlayer.closeScreen();
        }

        int displayRange = getDisplayRange();

        if (checkClick(lastMouseX, lastMouseY, 8, 17, 200, 200) && c >= '1' && c <= '8') {
            int id = c - '1';

            if (!radar.entries.isEmpty()) {
                for (RadarEntry m : radar.entries) {
                    double screenX = worldToScreenX(m.posX);
                    double screenZ = worldToScreenZ(m.posZ);
                    int x = guiLeft + SCREEN_CENTER_X + (int) screenX;
                    int z = guiTop + SCREEN_CENTER_Y + (int) screenZ;

                    if (lastMouseX + 5 > x && lastMouseX - 4 <= x && lastMouseY + 5 > z && lastMouseY - 4 <= z) {
                        NBTTagCompound data = new NBTTagCompound();
                        data.setInteger("launchEntity", m.entityID);
                        data.setInteger("link", id);
                        PacketDispatcher.wrapper
                                .sendToServer(new NBTControlPacket(data, radar.xCoord, radar.yCoord, radar.zCoord));
                        return;
                    }
                }
            }

            int tX = (int) ((lastMouseX - guiLeft - SCREEN_CENTER_X) * ((double) displayRange * 2 + 1) / 192D
                    + radar.xCoord);
            int tZ = (int) ((lastMouseY - guiTop - SCREEN_CENTER_Y) * ((double) displayRange * 2 + 1) / 192D
                    + radar.zCoord);
            NBTTagCompound data = new NBTTagCompound();
            data.setInteger("launchPosX", tX);
            data.setInteger("launchPosZ", tZ);
            data.setInteger("link", id);
            PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(data, radar.xCoord, radar.yCoord, radar.zCoord));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
            this.mc.thePlayer.closeScreen();
        }
    }
}
