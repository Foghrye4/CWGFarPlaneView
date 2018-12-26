package vertical_spawn_control.entity.ai.modificator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;

public class EntityAINearestAttackableTargetEntry extends EntityAIEntry {

	public EntityAINearestAttackableTargetEntry() {
		super(EntityAINearestAttackableTarget.class);
	}

	@Override
	public EntityAIEntryInstance getInstance(NBTTagCompound tag) {
		String ename = tag.getString("target_class");
		Class<? extends Entity> entityClass;
		if(ename.equals("player"))
			entityClass = EntityPlayer.class;
		else {
			EntityEntry rentry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(ename));
			if(rentry==null)
				throw new NullPointerException("No such entity registered: " + ename);
			entityClass = rentry.getEntityClass();
		}
		return new Instance(tag.getInteger("priority"), this, entityClass, tag.getBoolean("check_sight"));
	}
	
	public class Instance extends EntityAIEntryInstance {

		private Class<? extends Entity> target;
		private boolean checkSight;

		public Instance(int priorityIn, EntityAIEntry entryIn, Class<? extends Entity> entityClass, boolean checkSightIn) {
			super(priorityIn, entryIn);
			target =  entityClass;
			checkSight = checkSightIn;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void addTaskTo(EntityLiving entity) {
			EntityAINearestAttackableTarget aiInstance = new EntityAINearestAttackableTarget((EntityCreature) entity, target, checkSight);
			entity.targetTasks.addTask(priority, aiInstance);
		}
		
	}

}
