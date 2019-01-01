package vertical_spawn_control.entity.ai.modificator;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.nbt.NBTTagCompound;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;

public class EntityAIWanderEntry extends EntityAIEntry {

	public EntityAIWanderEntry() {
		super(EntityAILeapAtTarget.class);
	}

	@Override
	public EntityAIEntryInstance getInstance(NBTTagCompound tag) {
		return new Instance(tag.getInteger("priority"), tag.getFloat("speed"), this);
	}
	
	public class Instance extends EntityAIEntryInstance {
		private final float speed;
		public Instance(int priorityIn, float speedIn, EntityAIWanderEntry entryIn) {
			super(priorityIn, entryIn);
			speed = speedIn;
		}
		
		@Override
		public void addTaskTo(EntityLiving entity) {
			EntityAIWander aiInstance = new EntityAIWander((EntityCreature) entity, speed);
			entity.tasks.addTask(priority, aiInstance);
		}
	}
}
