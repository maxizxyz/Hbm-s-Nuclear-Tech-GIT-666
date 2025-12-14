package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.lib.Library;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.util.AxisAlignedBB;

public class TileEntityMachineRadarP35 extends TileEntityMachineRadarNT {

    public static int radarP35Range = 45_000;
    public static int consumptionP35 = 850;

    public TileEntityMachineRadarP35() {
        super();
        this.scanMissiles = true;
        this.scanShells = false;
        this.scanPlayers = false;
        this.smartMode = false;
    }

    @Override
    public String getConfigName() {
        return "radar_p35";
    }

    @Override
    public void readIfPresent(JsonObject obj) {
        radarP35Range = IConfigurableMachine.grab(obj, "I:radarP35Range", radarP35Range);
        consumptionP35 = IConfigurableMachine.grab(obj, "I:consumptionP35", consumptionP35);
    }

    @Override
    public void writeConfig(JsonWriter writer) throws IOException {
        writer.name("I:radarP35Range").value(radarP35Range);
        writer.name("I:consumptionP35").value(consumptionP35);
    }

    @Override
    public int getRange() {
        return radarP35Range;
    }

    @Override
    public void updateEntity() {
        this.scanMissiles = true;
        this.scanShells = false;
        this.scanPlayers = false;

        if (!worldObj.isRemote) {
            if (this.power >= consumptionP35) {
            }
        } else {
            prevRotation = rotation;
            if (power > 0)
                rotation += 2F;

            if (rotation >= 360) {
                rotation -= 360F;
                prevRotation -= 360F;
            }
        }

        if (!worldObj.isRemote) {
            super.updateEntity();
        }
    }

    @Override
    protected void allocateTargets() {
        this.entries.clear();

        if (this.yCoord < radarAltitude)
            return;
        if (this.power < consumptionP35) {
            this.power = 0;
            return;
        }
        this.power -= consumptionP35;

        this.scanMissiles = true;
        this.scanShells = false;
        this.scanPlayers = false;

        int scan = this.getRange();

        api.hbm.entity.IRadarDetectableNT.RadarScanParams params = new api.hbm.entity.IRadarDetectableNT.RadarScanParams(
                true, false, false, false);

        for (net.minecraft.entity.Entity e : matchingEntities) {

            if (e.dimension == worldObj.provider.dimensionId &&
                    Math.abs(e.posX - (xCoord + 0.5)) <= scan &&
                    Math.abs(e.posZ - (zCoord + 0.5)) <= scan &&
                    e.posY - yCoord > radarBuffer) {

                if (e instanceof net.minecraft.entity.EntityLivingBase &&
                        com.hbm.extprop.HbmLivingProps.getDigamma((net.minecraft.entity.EntityLivingBase) e) > 0.001) {
                    this.jammed = true;
                    entries.clear();
                    return;
                }

                for (java.util.function.Function<com.hbm.util.Tuple.Triplet<net.minecraft.entity.Entity, Object, api.hbm.entity.IRadarDetectableNT.RadarScanParams>, api.hbm.entity.RadarEntry> converter : converters) {

                    api.hbm.entity.RadarEntry entry = converter.apply(new com.hbm.util.Tuple.Triplet(e, this, params));
                    if (entry != null) {
                        this.entries.add(entry);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(xCoord + 2, yCoord, zCoord, Library.POS_X),
                new DirPos(xCoord - 2, yCoord, zCoord, Library.NEG_X),
                new DirPos(xCoord, yCoord, zCoord + 2, Library.POS_Z),
                new DirPos(xCoord, yCoord, zCoord - 2, Library.NEG_Z),
        };
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = AxisAlignedBB.getBoundingBox(
                    xCoord - 5,
                    yCoord,
                    zCoord - 5,
                    xCoord + 6,
                    yCoord + 10,
                    zCoord + 6);
        }

        return bb;
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public Object provideGUI(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world,
            int x, int y, int z) {
        if (ID == 0)
            return new com.hbm.inventory.gui.GUIMachineRadarP35(this);
        if (ID == 1)
            return new com.hbm.inventory.gui.GUIMachineRadarNTSlots(player.inventory, this);
        return null;
    }
}
