package jas.refactor;

import jas.common.DefaultProps;
import jas.common.ImportedSpawnList;

import java.io.File;
import java.util.HashMap;

import net.minecraft.world.World;

public class WorldSettings {
	private WorldProperties worldProperties;

	SpawnSettings defaultSpawnSettings;
	private HashMap<Integer, SpawnSettings> dimSpawnOverrides;

	public WorldSettings(File modConfigDirectoryFile, World world, ImportedSpawnList importedSpawnList) {
		worldProperties = new WorldProperties();
		loadWorldSettings(modConfigDirectoryFile, world);
		saveWorldSettings(modConfigDirectoryFile, world);
	}

	public void loadWorldSettings(File modConfigDirectoryFile, World world) {
		File defaultWorldSettingDirectory = new File(DefaultProps.WORLDSETTINGSDIR
				+ worldProperties.getFolderConfiguration().saveName + "/");
		defaultSpawnSettings = new SpawnSettings(worldProperties, defaultWorldSettingDirectory);
	}

	public void saveWorldSettings(File modConfigDirectoryFile, World world) {

	}
}
