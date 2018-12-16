package vertical_spawn_control.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;

public abstract class EntityAIEntryInstance {

	public final int priority;
	private final EntityAIEntry entry;

	public EntityAIEntryInstance(int priorityIn, EntityAIEntry entryIn) {
		priority = priorityIn;
		entry = entryIn;
	}

	public abstract void addTaskTo(EntityLiving entity);

	public void removeTaskFrom(EntityLiving entity) {
		EntityAITaskEntry taskToRemove = null;
		for (EntityAITaskEntry task : entity.tasks.taskEntries) {
			if (this.entry.aiClass.isInstance(task.action)) {
				taskToRemove = task;
				break;
			}
		}
		if (taskToRemove != null) {
			entity.tasks.removeTask(taskToRemove.action);
			return;
		}
		for (EntityAITaskEntry task : entity.targetTasks.taskEntries) {
			if (this.entry.aiClass.isInstance(task.action)) {
				taskToRemove = task;
				break;
			}
		}
		if (taskToRemove != null) {
			entity.targetTasks.removeTask(taskToRemove.action);
		}
	}
}
