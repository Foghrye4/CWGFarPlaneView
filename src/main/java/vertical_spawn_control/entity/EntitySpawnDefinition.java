package vertical_spawn_control.entity;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.gson.stream.JsonReader;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vertical_spawn_control.VSCMod;

public class EntitySpawnDefinition {

	Class<? extends Entity> entityClass;
	private java.lang.reflect.Constructor<? extends Entity> ctr;
	public float chance = 1.0f;
	public int groupSize = 4;
	public NBTTagCompound nbt;
	public int minLightLevel = 0;
	public int maxLightLevel = 16;
	
	EntitySpawnDefinition(Class<? extends EntityLiving> entityClassIn) {
		entityClass = entityClassIn;
		try {
			ctr = entityClass.getConstructor(World.class);
		} catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
		}
	}
	
	public EntitySpawnDefinition(JsonReader reader) throws IOException, NBTException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if(name.equals("class")) {
				String ename = reader.nextString();
				entityClass = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(ename)).getEntityClass();
				if(entityClass==null)
					throw new NullPointerException("No such entity registered: " + ename);
				try {
					ctr = entityClass.getConstructor(World.class);
				} catch (NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
			else if(name.equals("chance")) {
				chance = (float) reader.nextDouble();
			} else if (name.equals("group_size")) {
				groupSize = reader.nextInt();
			}
			else if(name.equals("nbt")) {
				nbt = JsonToNBT.getTagFromJson(reader.nextString());
			}
			else if(name.equals("min_light_level")) {
				minLightLevel = reader.nextInt();
			}
			else if(name.equals("max_light_level")) {
				maxLightLevel = reader.nextInt();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	public boolean spawn(World world, BlockPos pos, @Nullable IEntityLivingData data) {
        try {
            Entity spawnedEntity = ctr.newInstance(world);
            if(nbt!=null)
            	spawnedEntity.readFromNBT(nbt);
            spawnedEntity.setLocationAndAngles(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, world.rand.nextFloat() * 360.0F, 0.0F);
            if(spawnedEntity instanceof EntityLiving) {
            	data = ((EntityLiving)spawnedEntity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(spawnedEntity)), data);
                if(!((EntityLiving)spawnedEntity).isNotColliding()) {
                	spawnedEntity.setDead();
                	return false;
                }
            }
            world.spawnEntity(spawnedEntity);
        	return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        	return false;
        }
	}
}
