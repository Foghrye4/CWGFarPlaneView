package vertical_spawn_control.event;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.stream.JsonReader;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import vertical_spawn_control.VSCMod;
import vertical_spawn_control.entity.SpawnLayer;
import vertical_spawn_control.entity.ai.EnumEntityAIModificatorAction;
import vertical_spawn_control.entity.ai.modificator.EntityAIModificator;

public class VSCEventHandler {
	
	protected int thisTickSpawnAttempts = 0;
	Int2ObjectMap<List<SpawnLayer>> spawnLayers2Dimension = new Int2ObjectOpenHashMap<List<SpawnLayer>>();
	
	public VSCEventHandler() {
		spawnLayers2Dimension.defaultReturnValue(new ArrayList<SpawnLayer>());
	}
	
	@SubscribeEvent
	public void onSpawn(LivingSpawnEvent.CheckSpawn event) {
		if(event.getSpawner()!=null)
			return;
		List<SpawnLayer> spawnLayers = spawnLayers2Dimension.get(event.getWorld().provider.getDimension());
		BlockPos pos = event.getEntity().getPosition();
		for(SpawnLayer spawnLayer:spawnLayers) {
			if(spawnLayer.blockNaturalSpawn && spawnLayer.isPosInside(pos)) {
				if(!spawnLayer.isEffectiveAtBiomeAtPos(event.getWorld(), pos))
					return;
				if(spawnLayer.blackList.isEmpty()) {
					event.setResult(Event.Result.DENY);
					return;
				}
				else {
					for (Class<? extends Entity> eClass : spawnLayer.blackList) {
						if (eClass.isInstance(event.getEntity())) {
							event.setResult(Event.Result.DENY);
							return;
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if(!(event.getEntity() instanceof EntityLiving))
			return;
		NBTTagCompound data = event.getEntity().getEntityData();
		if(!data.hasKey("CustomAI"))
			return;
		NBTTagList customAITagList = data.getTagList("CustomAI", 10);
		for(int i=0;i<customAITagList.tagCount();i++) {
			EntityAIModificator aiMod = new EntityAIModificator(customAITagList.getCompoundTagAt(i));
			if(aiMod.action == EnumEntityAIModificatorAction.REMOVE)
				aiMod.apply((EntityLiving)event.getEntity());
		}
		for(int i=0;i<customAITagList.tagCount();i++) {
			EntityAIModificator aiMod = new EntityAIModificator(customAITagList.getCompoundTagAt(i));
			if(aiMod.action == EnumEntityAIModificatorAction.ADD)
				aiMod.apply((EntityLiving)event.getEntity());
		}
	}


	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event) {
		if (event.getWorld().isRemote)
			return;
		File worldDirectory = event.getWorld().getSaveHandler().getWorldDirectory();
		String subfolder = event.getWorld().provider.getSaveFolder();
		if (subfolder == null)
			subfolder = "";
		else
			subfolder += "/";
		File settings = new File(worldDirectory,"./" + subfolder + "data/" + VSCMod.MODID + "/vertical_spawn_control.json");
		if (settings.exists()) {
			try {
				List<SpawnLayer> layers = new ArrayList<SpawnLayer>();
				this.readFromJSON(settings,layers);
				spawnLayers2Dimension.put(event.getWorld().provider.getDimension(), layers);
				VSCMod.logger.info("Loading settings provided at " + settings.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NBTException e) {
				e.printStackTrace();
			}
		} else {
			VSCMod.logger.error("No settings provided at " + settings.getAbsolutePath());
		}
	}

	private void readFromJSON(File file, List<SpawnLayer> spawnLayers) throws IOException, NBTException {
		spawnLayers.clear();
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.setLenient(true);
        reader.beginArray();
		while (reader.hasNext()) {
			spawnLayers.add(new SpawnLayer(reader));
		}
		reader.endArray();
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(event.phase != TickEvent.Phase.END)
			return;
		List<SpawnLayer> spawnLayers = spawnLayers2Dimension.get(event.world.provider.getDimension());
		ICubicWorld cworld = (ICubicWorld) event.world;
		if (!cworld.isCubicWorld() || !(cworld instanceof WorldServer))
			return;
		PlayerCubeMap playerCubeMap = (PlayerCubeMap) ((WorldServer) cworld).getPlayerChunkMap();
		int ssize = spawnLayers.size();
		if (ssize == 0)
			return;
		for (SpawnLayer spawnLayer : spawnLayers) {
			Iterator<CubeWatcher> cwi = playerCubeMap.getRandomWrappedCubeWatcherIterator(event.world.rand.nextInt());
			for (int i = 0; i < ssize; i++) {
				int spawnAttemptsLimit = 16;
				while (cwi.hasNext() && spawnAttemptsLimit-- > 0) {
					CubeWatcher cw = cwi.next();
					int cposX = cw.getX();
					int cposY = cw.getY();
					int cposZ = cw.getZ();
					int posYMin = Coords.cubeToMinBlock(cposY);
					int posYMax = Coords.cubeToMaxBlock(cposY);

					int posXMin = Coords.cubeToMinBlock(cposX);
					int posXMax = Coords.cubeToMaxBlock(cposX);

					int posZMin = Coords.cubeToMinBlock(cposZ);
					int posZMax = Coords.cubeToMaxBlock(cposZ);
					BlockPos pos = new BlockPos(posXMin, posYMin, posZMin);
					if (spawnLayer.isIntersectsY(posYMin, posYMax) && spawnLayer.isIntersectsX(posXMin, posXMax)
							&& spawnLayer.isIntersectsZ(posZMin, posZMax)
							&& spawnLayer.isEffectiveAtBiomeAtPos(event.world, pos))
						spawnLayer.onCubeLoad(event.world, pos);
				}
			}
		}
	}
}
