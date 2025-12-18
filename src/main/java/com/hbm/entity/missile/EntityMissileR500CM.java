package com.hbm.entity.missile;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.particle.helper.ExplosionCreator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityMissileR500CM extends EntityMissileCruise {

	public EntityMissileR500CM(World world) {
		super(world);
		this.health = 25;
		this.cruiseAltitude = 175.0D;
		this.cruiseSpeed = 1.3D;
	}

	public EntityMissileR500CM(World world, float x, float y, float z, int a, int b) {
		super(world, x, y, z, a, b);
		this.health = 25;
		this.cruiseAltitude = 175.0D;
		this.cruiseSpeed = 1.3D;
	}

	@Override
	protected double getCruiseVelocity() {
		return 6.5D;
	}

	@Override
	public void onMissileImpact(MovingObjectPosition mop) {
		this.explodeStandard(8F, 32, false);
		ExplosionCreator.composeEffectStandard(worldObj, posX, posY, posZ);
	}

	@Override
	public List<ItemStack> getDebris() {
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(new ItemStack(ModItems.plate_steel, 10));
		list.add(new ItemStack(ModItems.plate_titanium, 6));
		list.add(new ItemStack(ModItems.thruster_medium, 1));
		return list;
	}

	@Override
	public ItemStack getDebrisRareDrop() {
		return new ItemStack(ModItems.warhead_generic_medium);
	}

	@Override
	public ItemStack getMissileItemForInfo() {
		return new ItemStack(ModItems.missile_r500cm);
	}

	@Override
	public String getUnlocalizedName() {
		return "radar.target.tier2";
	}

	@Override
	public int getBlipLevel() {
		return api.hbm.entity.IRadarDetectableNT.TIER2;
	}
}
