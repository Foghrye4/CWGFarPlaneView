package vertical_spawn_control.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gson.stream.JsonReader;

import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTException;

public class SpawnLayer {
	int from;
	int to;
	public boolean blockNaturalSpawn = true;
	public final List<EntitySpawnDefinition> spawnList = new ArrayList<EntitySpawnDefinition>();
	public final List<Class<? extends Entity>> blackList = new ArrayList<Class<? extends Entity>>();
	public final Set<Biome> biomeBlackList = new HashSet<Biome>();
	public final Set<Biome> biomeWhiteList = new HashSet<Biome>();
	
	private static final int MAX_GROUP_SIZE = 4;
	
	public SpawnLayer(JsonReader reader) throws IOException, NBTException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("from")) {
				from = reader.nextInt();
			} else if (name.equals("to")) {
				to = reader.nextInt();
			} else if (name.equals("exclude_biomes")) {
				reader.beginArray();
				while (reader.hasNext()) {
					Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(reader.nextString()));
					biomeBlackList.add(biome);
				}
				reader.endArray();
			} else if (name.equals("only_in_biomes")) {
				reader.beginArray();
				while (reader.hasNext()) {
					Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(reader.nextString()));
					biomeWhiteList.add(biome);
				}
				reader.endArray();
			} else if (name.equals("block_natural_spawn")) {
				blockNaturalSpawn = reader.nextBoolean();
			} else if (name.equals("black_list")) {
				reader.beginArray();
				while (reader.hasNext()) {
					Class<? extends Entity> entityClass = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(reader.nextString())).getEntityClass();
					blackList.add(entityClass);
				}
				reader.endArray();
			} else if (name.equals("spawn_list")) {
				reader.beginArray();
				while (reader.hasNext()) {
					spawnList.add(new EntitySpawnDefinition(reader));
				}
				reader.endArray();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		if(from>to) {
			int a = from;
			from = to;
			to = a;
		}
	}

	public void onCubeLoad(World world, BlockPos blockPos) {
		EntityPlayer player = world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 24, false);
		if(player!=null) {
			return;
		}
		Random rand = world.rand;
		EntitySpawnDefinition def = spawnList.get(rand.nextInt(spawnList.size()));
		int from1 = Math.max(blockPos.getY(), from);
		int to1 = Math.min(blockPos.getY()+ICube.SIZE-1, to);
		PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		int groupSize = 0;
		if(rand.nextFloat()>def.chance/(1.0f + world.loadedEntityList.size()*0.01f)) {
			return;
		}
		IEntityLivingData data = null;
		for(int ix = blockPos.getX()+rand.nextInt(5);ix<=blockPos.getX()+15;ix+=rand.nextInt(5) + 1)
			for(int iz = blockPos.getZ()+rand.nextInt(5);iz<=blockPos.getZ()+15;iz+=rand.nextInt(5) + 1)
				for(int iy = from1;iy<=to1;iy++) {
					pos.setPos(ix, iy, iz);
					if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry
			                .getPlacementForEntity(def.entityClass), world, pos)) {
						continue;
					}
					int light = world.getLight(pos, false);
					if(light<def.minLightLevel)
						continue;
					if(light>def.maxLightLevel)
						continue;
					if(def.spawn(world, pos, data) && groupSize++>MAX_GROUP_SIZE) {
						return;
					}
					from1 = iy;
					to1 = iy+1;
					break;
				}
		pos.release();
	}
	
	public boolean isPosInside(BlockPos position) {
		return position.getY()>from && position.getY()<to;
	}
	
	public boolean isIntersects(int posYMin, int posYMax) {
		return posYMin>=from && posYMin<=to || posYMax>=from && posYMax<=to || posYMin>=from && posYMax<=to || posYMin<=from && posYMax>=to;
	}
	
	public boolean isEffectiveAtBiomeAtPos(World world, BlockPos pos) {
		Biome biome = world.getBiome(pos);
		if(biomeBlackList.contains(biome))
			return false;
		if(!biomeWhiteList.isEmpty() && !biomeWhiteList.contains(biome))
			return false;
		return true;
	}
}
