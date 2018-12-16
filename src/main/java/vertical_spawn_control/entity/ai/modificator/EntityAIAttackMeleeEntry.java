package vertical_spawn_control.entity.ai.modificator;

import java.lang.reflect.Constructor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;

public class EntityAIAttackMeleeEntry extends EntityAIEntry {

	public EntityAIAttackMeleeEntry() {
		super(EntityAIAttackMelee.class);
	}

	@Override
	public EntityAIEntryInstance getInstance(NBTTagCompound tag) {
		return new Instance(tag.getInteger("priority"), this, tag.getDouble("speed"), tag.getBoolean("use_long_memory"));
	}
	
	public class Instance extends EntityAIEntryInstance {

		private double speed;
		private boolean useLongMemory;

		public Instance(int priorityIn, EntityAIEntry entryIn, double speedIn, boolean useLongMemoryIn) {
			super(priorityIn, entryIn);
			 speed = speedIn;
			 useLongMemory = useLongMemoryIn;
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public void addTaskTo(EntityLiving entity) {
			Constructor<EntityAIAttackMelee> constructor;
			try {
				constructor = (Constructor<EntityAIAttackMelee>) aiClass.getConstructor(EntityCreature.class, double.class, boolean.class);
				EntityAIAttackMelee aiInstance = constructor.newInstance(entity, speed, useLongMemory);
				entity.tasks.addTask(priority, aiInstance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
