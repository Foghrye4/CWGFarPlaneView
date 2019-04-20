# Overview
This addon require CubicChunks version 0.0.928 or higher (https://minecraft.curseforge.com/projects/opencubicchunks), CubicWorldGen version 0.0.39 or higher (https://minecraft.curseforge.com/projects/cubicworldgen) and MinecraftForge 14.23.3.2658 or higher.

This addon allow to generate ores again for cubic type worlds in already generated and saved on disk cubes.

It in unnecessary to have this addon on client side. Clients without this mod will be able to join a server with this mod installed.

# Ore regeneration

## How to use
In existing world create folder at path *<world_save_folder>/data/cwgfarplaneview/* and place normal CubicWorldGen external preset file (custom_generator_settings.json) where you define only ores which you want to be generated in existing cubes. If you want to limit areas, where new ores will be generated, use cubeAreas and define ores in sub-preset.

## What this addon will do
On a first world load or on running command `/cwgfarplaneview reset 0 addon` will scan world folder for existing cubes and add coordinates to memory. After further server restarts (or world reloads in client game) addon will use saved data. On every CubeWatchEvent (which is fired whenever cube is needed to be send to client) addon will check if coordinates of cube match set in memory, remove it from set and launch ore generators according preset.

# Block replacing

## How to use
In existing world create folder at path <world_save_folder>/data/cwgfarplaneview/ and place replace_config.json file where you define what blockstate should be replaced with what blockstate. Download provided example to learn correct syntax: http://www.mediafire.com/file/kqybjlp7jx40ywl/replace_config.json
Alternatively run command /cwgfarplaneview replace-config <dimension_id> <config_string>.
<config_string> is a same string as provided in file, except it should not contain any spaces. After that simply run command /cwgfarplaneview replace <dimension_id> to start a process.

## What this addon will do
On running command /cwgfarplaneview replace <dimension_id> addon will scan world folder for existing cubes. Each cube will be loaded from disk, scanned for defined block state and if cube contain this block state, it will be replaced with substitute.

# Server commands
List of server commands added by mod:

* `/cwgfarplaneview remaining <dimension_id>` - shows remaining cubes, which need to be populated with ores in <dimension_id>. The Overworld dimension ID is 0.
* `/cwgfarplaneview reset <dimension_id>` - rescan save folder to re-add all saved cube coordinates in population queue. You don't need to launch this command unless cube repopulation process was already done and you need to start a new one.
* `/cwgfarplaneview stop <dimension_id>` - completely stop population process.
* `/cwgfarplaneview finish <dimension_id>` - do all cubes at once.
* `/cwgfarplaneview replace-config <dimension_id> <config_string>` - overwrite replacer config for dimension.
* `/cwgfarplaneview replace <dimension_id>` - replace defined blockstate in every saved cube according previously loaded config.
