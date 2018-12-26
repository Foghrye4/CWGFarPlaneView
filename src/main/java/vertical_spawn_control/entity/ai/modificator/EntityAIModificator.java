package vertical_spawn_control.entity.ai.modificator;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import vertical_spawn_control.entity.ai.EntityAIEntry;
import vertical_spawn_control.entity.ai.EntityAIEntryInstance;
import vertical_spawn_control.entity.ai.EnumEntityAIModificatorAction;

public class EntityAIModificator {
	private static final Map<String, EntityAIEntry> aiRegistry = new HashMap<String, EntityAIEntry>();
	static {
		aiRegistry.put("nearest_attackable_target", new EntityAINearestAttackableTargetEntry());
		aiRegistry.put("leap_at_target", new EntityAILeapAtTargetEntry());
		aiRegistry.put("attack_melee", new EntityAIAttackMeleeEntry());
		aiRegistry.put("attack_melee_fixed_damage", new EntityAIAttackMeleeFixedDamageEntry());
	}

	public EnumEntityAIModificatorAction action = EnumEntityAIModificatorAction.ADD;
	EntityAIEntryInstance aiTaskConstructor;

	public EntityAIModificator(NBTTagCompound tag) {
		EntityAIEntry aiModificator = null;
		if (tag.hasKey("add")) {
			action = EnumEntityAIModificatorAction.ADD;
			aiModificator = aiRegistry.get(tag.getString("add"));
		}
		if (tag.hasKey("remove")) {
			action = EnumEntityAIModificatorAction.REMOVE;
			aiModificator = aiRegistry.get(tag.getString("remove"));
		}
		if (aiModificator == null)
			throw new IllegalArgumentException("No remove or add definitions for ai:" + tag.toString());
		aiTaskConstructor = aiModificator.getInstance(tag);
	}

	public void apply(EntityLiving entity) {
		switch (action) {
		case ADD: {
			aiTaskConstructor.addTaskTo(entity);
			break;
		}
		case REMOVE:
			aiTaskConstructor.removeTaskFrom(entity);
			break;
		}

	}
}
