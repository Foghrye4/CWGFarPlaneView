package vertical_spawn_control.entity.ai.modificator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
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
		
		@SuppressWarnings("unchecked")
		@Override
		public void addTaskTo(EntityLiving entity) {
			Constructor<EntityAILeapAtTarget> constructor;
			try {
				constructor = (Constructor<EntityAILeapAtTarget>) aiClass.getConstructor(EntityLiving.class,float.class);
				EntityAILeapAtTarget aiInstance = constructor.newInstance(entity, leapMotionY);
				entity.tasks.addTask(priority, aiInstance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
