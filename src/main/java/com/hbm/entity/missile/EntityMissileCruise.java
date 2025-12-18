package com.hbm.entity.missile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class EntityMissileCruise extends EntityMissileBaseNT {

	public enum FlightPhase {
		ASCENT,
		CRUISE,
		TERMINAL
	}

	protected double cruiseAltitude = 300.0D;
	protected double descentDistance = 50.0D;
	protected FlightPhase currentPhase = FlightPhase.ASCENT;

	protected double cruiseSpeed = 1.0D;
	protected double startY;
	protected int launchTime = 0;

	public EntityMissileCruise(World world) {
		super(world);
	}

	public EntityMissileCruise(World world, float x, float y, float z, int a, int b) {
		super(world, x, y, z, a, b);
		startY = y;
		launchTime = 0;
	}

	@Override
	public void onUpdate() {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		super.onUpdate();
		
		if(!worldObj.isRemote) {
			if(launchTime == 0) {
				launchTime = this.ticksExisted;
			}
			
			if(velocity < getCruiseVelocity()) {
				velocity += 0.005;
			}

			updateFlightPhase();

			switch(currentPhase) {
				case ASCENT:
					updateAscentPhase();
					break;
				case CRUISE:
					updateCruisePhase();
					break;
				case TERMINAL:
					updateTerminalPhase();
					break;
			}

			this.rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
			float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationPitch = (float) (Math.atan2(this.motionY, f2) * 180.0D / Math.PI) - 90;

			while(this.rotationPitch - this.prevRotationPitch >= 180.0F) this.prevRotationPitch += 360.0F;
			while(this.rotationYaw - this.prevRotationYaw < -180.0F) this.prevRotationYaw -= 360.0F;
			while(this.rotationYaw - this.prevRotationYaw >= 180.0F) this.prevRotationYaw += 360.0F;
		}
	}

	@Override
	protected void spawnContrail() {
		int timeSinceLaunch = this.ticksExisted - launchTime;
		if(worldObj.isRemote && timeSinceLaunch <= 100 && timeSinceLaunch >= 0) {
			super.spawnContrail();
		}
	}

	protected void updateFlightPhase() {
		int timeSinceLaunch = this.ticksExisted - launchTime;
		double distanceToTarget = Math.sqrt(
			(targetX - posX) * (targetX - posX) + 
			(targetZ - posZ) * (targetZ - posZ)
		);

		if(timeSinceLaunch < 100) {
			currentPhase = FlightPhase.ASCENT;
		} else if(distanceToTarget > descentDistance) {
			currentPhase = FlightPhase.CRUISE;
		} else {
			currentPhase = FlightPhase.TERMINAL;
		}
	}

	protected void updateAscentPhase() {
		int timeSinceLaunch = this.ticksExisted - launchTime;
		
		if(timeSinceLaunch < 90) {
			motionY = velocity * 0.8;
			motionX = 0;
			motionZ = 0;
		} else {
			double transitionProgress = (timeSinceLaunch - 90) / 10.0;
			if(transitionProgress > 1.0) transitionProgress = 1.0;
			
			motionY = velocity * 0.8 * (1.0 - transitionProgress);
			
			Vec3 vector = Vec3.createVectorHelper(targetX - posX, 0, targetZ - posZ);
			double distance = vector.lengthVector();
			
			if(distance > 5) {
				vector = vector.normalize();
				double horizontalSpeed = velocity * 0.5 * transitionProgress;
				motionX = vector.xCoord * horizontalSpeed;
				motionZ = vector.zCoord * horizontalSpeed;
			}
		}
	}

	protected void updateCruisePhase() {
		motionY = 0;
		
		Vec3 vector = Vec3.createVectorHelper(targetX - posX, 0, targetZ - posZ);
		double distance = vector.lengthVector();
		
		if(distance > 5) {
			vector = vector.normalize();
			double horizontalSpeed = velocity * 0.5;
			motionX = vector.xCoord * horizontalSpeed;
			motionZ = vector.zCoord * horizontalSpeed;
		} else {
			motionX *= 0.95;
			motionZ *= 0.95;
		}
	}

	protected void updateTerminalPhase() {
		Vec3 targetVector = Vec3.createVectorHelper(
			targetX - posX, 
			0 - posY,
			targetZ - posZ
		);
		
		double vectorLength = targetVector.lengthVector();
		if(vectorLength > 1) {
			targetVector = targetVector.normalize();
			double diveSpeed = velocity * 0.5;
			motionX = targetVector.xCoord * diveSpeed;
			motionY = targetVector.yCoord * diveSpeed;
			motionZ = targetVector.zCoord * diveSpeed;
		} else {
			if(motionY > -velocity * 0.5) {
				motionY -= 0.1;
			}
			motionX *= 0.85;
			motionZ *= 0.85;
		}
	}



	@Override
	public boolean hasPropulsion() {
		return false;
	}

	protected double getCruiseVelocity() {
		return 1.0D;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		cruiseAltitude = nbt.getDouble("cruiseAlt");
		descentDistance = nbt.getDouble("descentDist");
		launchTime = nbt.getInteger("launchTime");
		int phaseOrdinal = nbt.getInteger("flightPhase");
		if(phaseOrdinal >= 0 && phaseOrdinal < FlightPhase.values().length) {
			currentPhase = FlightPhase.values()[phaseOrdinal];
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setDouble("cruiseAlt", cruiseAltitude);
		nbt.setDouble("descentDist", descentDistance);
		nbt.setInteger("launchTime", launchTime);
		nbt.setInteger("flightPhase", currentPhase.ordinal());
	}

	@Override
	public boolean suppliesRedstone(RadarScanParams params) {
		if(params.smartMode && this.currentPhase == FlightPhase.ASCENT) return false;
		return true;
	}
}
