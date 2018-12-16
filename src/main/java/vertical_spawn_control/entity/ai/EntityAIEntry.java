package vertical_spawn_control.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.nbt.NBTTagCompound;
import vertical_spawn_control.entity.ai.modificator.EntityAILeapAtTargetEntry;

public abstract class EntityAIEntry {

	protected final Class<? extends EntityAIBase> aiClass;
	
	public EntityAIEntry(Class<? extends EntityAIBase> aiClassIn) {
		aiClass = aiClassIn;
	}
	
	public abstract EntityAIEntryInstance getInstance(NBTTagCompound tag);
}
