package api.hbm.entity;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class RadarEntry {

    public String unlocalizedName;
    public int blipLevel;
    public int posX;
    public int posY;
    public int posZ;
    public int dim;
    public int entityID;
    public boolean redstone;
    public short motionX;
    public short motionZ;

    public RadarEntry() {
    }

    public RadarEntry(String name, int level, int x, int y, int z, int dim, int entityID, boolean redstone) {
        this.unlocalizedName = name;
        this.blipLevel = level;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dim = dim;
        this.entityID = entityID;
        this.redstone = redstone;
        this.motionX = 0;
        this.motionZ = 0;
    }

    public RadarEntry(IRadarDetectableNT detectable, Entity entity, boolean redstone) {
        this(detectable.getUnlocalizedName(), detectable.getBlipLevel(), (int) Math.floor(entity.posX),
                (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ), entity.dimension, entity.getEntityId(),
                redstone);
        this.motionX = (short) (entity.motionX * 100);
        this.motionZ = (short) (entity.motionZ * 100);
    }

    public RadarEntry(IRadarDetectable detectable, Entity entity) {
        this(detectable.getTargetType().name, detectable.getTargetType().ordinal(), (int) Math.floor(entity.posX),
                (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ), entity.dimension, entity.getEntityId(),
                entity.motionY < 0);
        this.motionX = (short) (entity.motionX * 100);
        this.motionZ = (short) (entity.motionZ * 100);
    }

    public RadarEntry(EntityPlayer player) {
        this(player.getDisplayName(), IRadarDetectableNT.PLAYER, (int) Math.floor(player.posX),
                (int) Math.floor(player.posY), (int) Math.floor(player.posZ), player.dimension, player.getEntityId(),
                true);
        this.motionX = (short) (player.motionX * 100);
        this.motionZ = (short) (player.motionZ * 100);
    }

    public double getMotionX() {
        return motionX / 100.0;
    }

    public double getMotionZ() {
        return motionZ / 100.0;
    }

    public double getSpeedBPS() {
        double vx = getMotionX();
        double vz = getMotionZ();
        return Math.sqrt(vx * vx + vz * vz) * 20.0;
    }

    public void fromBytes(ByteBuf buf) {
        this.unlocalizedName = ByteBufUtils.readUTF8String(buf);
        this.blipLevel = buf.readShort();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.dim = buf.readShort();
        this.entityID = buf.readInt();
        this.motionX = buf.readShort();
        this.motionZ = buf.readShort();
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.unlocalizedName);
        buf.writeShort(this.blipLevel);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeShort(this.dim);
        buf.writeInt(this.entityID);
        buf.writeShort(this.motionX);
        buf.writeShort(this.motionZ);
    }
}
