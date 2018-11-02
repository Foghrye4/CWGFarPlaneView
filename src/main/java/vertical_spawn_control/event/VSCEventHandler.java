package vertical_spawn_control.event;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.world.CubeWatchEvent;
import io.github.opencubicchunks.cubicchunks.api.world.ICubeProvider;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import io.github.opencubicchunks.cubicchunks.core.server.CubeWatcher;
import io.github.opencubicchunks.cubicchunks.core.server.PlayerCubeMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import vertical_spawn_control.VSCMod;
import vertical_spawn_control.entity.SpawnLayer;

public class VSCEventHandler {
	
	protected int thisTickSpawnAttempts = 0;
	List<SpawnLayer> spawnLayers = new ArrayList<SpawnLayer>();
	
	@SubscribeEvent
	public void onSpawn(LivingSpawnEvent.CheckSpawn event) {
		if(event.getWorld().provider.getDimension()!=0)
			return;
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
	public void onWorldLoadEvent(WorldEvent.Load event) {
		if(event.getWorld().provider.getDimension()!=0 || event.getWorld().isRemote)
			return;
		File worldDirectory = event.getWorld().getSaveHandler().getWorldDirectory();
		File settings = new File(worldDirectory, "./vertical_spawn_control.json");
		if(settings.exists()) {
			try {
				this.readFromJSON(settings);
				VSCMod.logger.info("Loading settings provided at " + settings.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NBTException e) {
				e.printStackTrace();
			}
		}
		else {
			VSCMod.logger.error("No setting provided at " + settings.getAbsolutePath());
		}
	}

	private void readFromJSON(File file) throws IOException, NBTException {
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
		ICubicWorld cworld = (ICubicWorld) event.world;
		if (!cworld.isCubicWorld() || !(cworld instanceof WorldServer))
			return;
		PlayerCubeMap playerCubeMap = (PlayerCubeMap) ((WorldServer) cworld).getPlayerChunkMap();
		int ssize = spawnLayers.size();
		int rShift = event.world.rand.nextInt(ssize);
		Iterator<CubeWatcher> cwi = playerCubeMap.getRandomWrappedCubeWatcherIterator(event.world.rand.nextInt());
		for (int i=0;i<ssize;i++) {
			SpawnLayer spawnLayer = spawnLayers.get((i+rShift)%ssize);
			int spawnAttemptsLimit = 16;
			while (cwi.hasNext() && spawnAttemptsLimit-- > 0) {
				CubeWatcher cw = cwi.next();
				int cposX = cw.getX();
				int cposY = cw.getY();
				int cposZ = cw.getZ();
				int posX = Coords.cubeToMinBlock(cposX);
				int posZ = Coords.cubeToMinBlock(cposZ);
				int posYMin = Coords.cubeToMinBlock(cposY);
				int posYMax = Coords.cubeToMaxBlock(cposY);
				BlockPos pos = new BlockPos(posX, posYMin, posZ);
				if (spawnLayer.isIntersects(posYMin, posYMax) && spawnLayer.isEffectiveAtBiomeAtPos(event.world, pos))
					spawnLayer.onCubeLoad(event.world, pos);
			}
		}
	}
}
