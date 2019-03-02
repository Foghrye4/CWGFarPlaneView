package regencwg.world;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.stream.JsonReader;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.core.world.cube.Cube;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import regencwg.world.storage.DiskDataUtil;

public class BlockReplaceConfig {
	public final Map<IBlockState,IBlockState> replaceMap = new HashMap<IBlockState,IBlockState>();

	public static BlockReplaceConfig fromFile(File rcSettingFile) {
		BlockReplaceConfig config = new BlockReplaceConfig();
		try (FileReader fileReader = new FileReader(rcSettingFile)) {
			read(fileReader, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	private static void read(Reader sourceReader, BlockReplaceConfig config) {
		try (JsonReader reader = new JsonReader(sourceReader)) {
        reader.setLenient(true);
        reader.beginArray();
		while (reader.hasNext()) {
			reader.beginObject();
			IBlockState searchFor = null;
			IBlockState replaceWith = Blocks.STONE.getDefaultState();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (name.equals("search-for")) {
					NBTTagCompound tag = JsonToNBT.getTagFromJson(readJsonObjectAsString(reader));
					searchFor = NBTUtil.readBlockState(tag);
				}
				else if (name.equals("replace-with")) {
					NBTTagCompound tag = JsonToNBT.getTagFromJson(readJsonObjectAsString(reader));
					replaceWith = NBTUtil.readBlockState(tag);
				}
				else {
					reader.skipValue();
				}
			}
			if(searchFor!=null) {
				config.replaceMap.put(searchFor, replaceWith);
			}
			reader.endObject();
		}
		reader.endArray();
		} catch (IOException | NBTException e) {
			e.printStackTrace();
		}
	}
	
	private static String readJsonObjectAsString(JsonReader reader) throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		reader.beginObject();
		while (reader.hasNext()) {
			buffer.append(reader.nextName());
			buffer.append(':');
			buffer.append('"');
			buffer.append(reader.nextString());
			buffer.append('"');
			if(reader.hasNext())
				buffer.append(',');
		}
		reader.endObject();
		buffer.append('}');
		return buffer.toString();
	}

	public int runReplacer(World world) {
		int affectedCubes = 0;
		Set<CubePos> toReplaceInPosSet = new HashSet<CubePos>();
		DiskDataUtil.addAllSavedCubePosToSet(world, toReplaceInPosSet);
		for(CubePos pos:toReplaceInPosSet) {
			ICube cube = ((ICubicWorld)world).getCubeFromCubeCoords(pos);
			if(cube.getStorage() == null || cube.getStorage().isEmpty())
				continue;
			ExtendedBlockStorage ebs = cube.getStorage();
			boolean markDirty = false;
			for (int i = 0; i < 4096; i++) {
				IBlockState bs = ebs.get(i & 15, i >> 8, i >> 4 & 15);
				IBlockState bsToReplace = replaceMap.get(bs);
				if (bsToReplace == null)
					continue;
				ebs.set(i & 15, i >> 8, i >> 4 & 15, bsToReplace);
				markDirty = true;
			}
			if(markDirty) {
				Cube ccube = (Cube) cube;
				ccube.markDirty();
				affectedCubes++;
			}
		}
		return affectedCubes;
	}

	public static BlockReplaceConfig fromString(String string) {
		BlockReplaceConfig config = new BlockReplaceConfig();
		StringReader stringReader = new StringReader(string);
		read(stringReader, config);
		stringReader.close();
		return config;
	}
}
