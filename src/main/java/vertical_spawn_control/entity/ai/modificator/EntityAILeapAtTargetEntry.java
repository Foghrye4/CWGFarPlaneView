package vertical_spawn_control.entity.ai.modificator;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.nbt.NBTTagCompound;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;

public class EntityAILeapAtTargetEntry extends EntityAIEntry {

	public EntityAILeapAtTargetEntry() {
		super(EntityAILeapAtTarget.class);
	}

	@Override
	public EntityAIEntryInstance getInstance(NBTTagCompound tag) {
		return new Instance(tag.getInteger("priority"), tag.getFloat("leap_motion_y"), this);
	}
	
	public class Instance extends EntityAIEntryInstance {
		private final float leapMotionY;
		public Instance(int priorityIn, float leapMotionYIn, EntityAILeapAtTargetEntry entryIn) {
			super(priorityIn, entryIn);
			leapMotionY = leapMotionYIn;
		}
		
		@Override
		public void addTaskTo(EntityLiving entity) {
			EntityAILeapAtTarget aiInstance = new EntityAILeapAtTarget(entity, leapMotionY);
			entity.tasks.addTask(priority, aiInstance);
		}
	}
}
