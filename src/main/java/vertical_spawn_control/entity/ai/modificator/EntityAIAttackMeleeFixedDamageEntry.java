package vertical_spawn_control.entity.ai.modificator;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import vertical_spawn_control.entity.ai.EntityAIAttackMeleeFixedDamage;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;

public class EntityAIAttackMeleeFixedDamageEntry extends EntityAIEntry {

	public EntityAIAttackMeleeFixedDamageEntry() {
		super(EntityAIAttackMeleeFixedDamage.class);
	}

	@Override
	public EntityAIEntryInstance getInstance(NBTTagCompound tag) {
		return new Instance(tag.getInteger("priority"), this, tag.getDouble("speed"), tag.getBoolean("use_long_memory"), tag.getFloat("damage"));
	}
	
	public class Instance extends EntityAIEntryInstance {

		private double speed;
		private boolean useLongMemory;
		private float damage;

		public Instance(int priorityIn, EntityAIEntry entryIn, double speedIn, boolean useLongMemoryIn, float damageIn) {
			super(priorityIn, entryIn);
			 speed = speedIn;
			 useLongMemory = useLongMemoryIn;
			 damage = damageIn;
		}

		@Override
		public void addTaskTo(EntityLiving entity) {
			EntityAIAttackMeleeFixedDamage aiInstance = new EntityAIAttackMeleeFixedDamage((EntityCreature) entity, speed, useLongMemory, damage);
			entity.tasks.addTask(priority, aiInstance);
		}
		
	}

}
