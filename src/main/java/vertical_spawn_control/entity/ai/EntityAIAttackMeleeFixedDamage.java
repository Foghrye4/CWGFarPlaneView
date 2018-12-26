package vertical_spawn_control.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

public class EntityAIAttackMeleeFixedDamage extends EntityAIAttackMelee {

	private final float damage;
	public EntityAIAttackMeleeFixedDamage(EntityCreature creature, double speedIn, boolean useLongMemory, float damageIn) {
		super(creature, speedIn, useLongMemory);
		damage = damageIn;
	}
	
	protected void checkAndPerformAttack(EntityLivingBase target, double range) {
		double d0 = this.getAttackReachSqr(target);
		if (range <= d0 && this.attackTick <= 0) {
			this.attackTick = 20;
			this.attacker.swingArm(EnumHand.MAIN_HAND);
			this.attacker.attackEntityAsMob(target);
			target.attackEntityFrom(DamageSource.causeMobDamage(attacker), damage);
		}
	}
}
