package jas.spawner.modern.spawner.creature.type;

import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.serializer.CreatureTypeSaveObject;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.group.BiomeGroupSaveObject;
import jas.spawner.modern.spawner.biome.group.BiomeGroupSaveObject.BiomeGroupSaveObjectSerializer;
import jas.spawner.modern.world.WorldProperties;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.minecraft.block.material.Material;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.Gson;

public class CreatureTypeRegistry {
	public static final String NONE = "NONE";
	public static final String CREATURE = "CREATURE";
	public static final String MONSTER = "MONSTER";
	public static final String AMBIENT = "AMBIENT";
	public static final String WATERCREATURE = "WATERCREATURE";
	public static final String UNDERGROUND = "UNDERGROUND";
	public static final String OPENSKY = "OPENSKY";
	public static final ImmutableSet<String> defaultTypes;
	static {
		Builder<String> builder = ImmutableSet.<String> builder();
		builder.add(NONE).add(CREATURE).add(MONSTER).add(AMBIENT).add(WATERCREATURE).add(UNDERGROUND).add(OPENSKY);
		defaultTypes = builder.build();
	}
	/** Hashmap containing Creature Types. */
	private ImmutableMap<String, CreatureType> types;

	/**
	 * Default Category Keys. Note that 'NONE' is not a Type but the absence of, i.e. null. This is NOT placed inside
	 * the types Hashmap which should not contain null entries
	 */

	public final BiomeGroupRegistry biomeGroupRegistry;
	public final WorldProperties worldProperties;

	public Iterator<CreatureType> getCreatureTypes() {
		return types.values().iterator();
	}

	public CreatureType getCreatureType(String typeID) {
		return types.get(typeID.toUpperCase());
	}

	public CreatureTypeRegistry(BiomeGroupRegistry biomeGroupRegistry, WorldProperties worldProperties) {
		this.biomeGroupRegistry = biomeGroupRegistry;
		this.worldProperties = worldProperties;
	}

	public void loadFromConfig(File configDirectory) {
		Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { CreatureTypeSaveObject.class },
				new Object[] { new CreatureTypeSaveObject.CreatureTypeSaveObjectSerializer() });

		File creatureTypeFile = CreatureType
				.getFile(configDirectory, worldProperties.getFolderConfiguration().saveName);

		CreatureTypeSaveObject saveObject = GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(creatureTypeFile, false), CreatureTypeSaveObject.class, gson);

		Optional<Collection<CreatureTypeBuilder>> read = saveObject.getTypes();

		HashMap<String, CreatureTypeBuilder> readTypes = new HashMap<String, CreatureTypeBuilder>();
		if (read.isPresent()) {
			for (CreatureTypeBuilder creatureBuilder : read.get()) {
				if (creatureBuilder.typeID != null) {
					readTypes.put(creatureBuilder.typeID, creatureBuilder);
				}
			}
		} else {
			readTypes = new HashMap<String, CreatureTypeBuilder>();
			CreatureTypeBuilder monster = new CreatureTypeBuilder(MONSTER, 1, 70)
			.withSpawnExpression("obj.difficulty()==0||!solidside(1,{0,0,0},{0,-1,0})||liquid({0,0,0},{0,0,0})||normal({0,0,0},{0,0,0})||normal({0,0,0},{0,1,0})||!opaque({0,0,0},{0,-1,0})");
			CreatureTypeBuilder ambient = new CreatureTypeBuilder(AMBIENT, 1, 15);
			CreatureTypeBuilder watercreature = new CreatureTypeBuilder(WATERCREATURE, 1, 15).insideMedium(
					Material.water).withSpawnExpression("!liquid({0,0,0},{0,0,0})||!liquid({0,0,0},{0,-1,0})||normal({0,0,0},{0,1,0})");
			CreatureTypeBuilder underground = new CreatureTypeBuilder(UNDERGROUND, 1, 10)
					.withSpawnExpression("!solidside(1,{0,0,0},{0,-1,0})||liquid({0,0,0},{0,0,0})||normal({0,0,0},{0,0,0})||normal({0,0,0},{0,1,0})||!opaque({0,0,0},{0,-1,0})||sky()");
			CreatureTypeBuilder opensky = new CreatureTypeBuilder(OPENSKY, 1, 10)
					.withSpawnExpression("!solidside(1,{0,0,0},{0,-1,0})||liquid({0,0,0},{0,0,0})||normal({0,0,0},{0,0,0})||normal({0,0,0},{0,1,0})||!opaque({0,0,0},{0,-1,0})||!sky()");
			CreatureTypeBuilder creature = new CreatureTypeBuilder(CREATURE, 400, 10)
					.withChanceToChunkSpawn(0.1f)
					.withSpawnExpression(
							"!solidside(1,{0,0,0},{0,-1,0})||liquid({0,0,0},{0,0,0})||normal({0,0,0},{0,0,0})||normal({0,0,0},{0,1,0})||!opaque({0,0,0},{0,-1,0})||!sky()");
			readTypes.put(monster.typeID, monster);
			readTypes.put(ambient.typeID, ambient);
			readTypes.put(opensky.typeID, opensky);
			readTypes.put(creature.typeID, creature);
			readTypes.put(underground.typeID, underground);
			readTypes.put(watercreature.typeID, watercreature);
		}
		ImmutableMap.Builder<String, CreatureType> builder = ImmutableMap.<String, CreatureType> builder();
		for (CreatureTypeBuilder creatureBuilder : readTypes.values()) {
			builder.put(creatureBuilder.typeID, creatureBuilder.build(biomeGroupRegistry));
		}
		types = builder.build();
	}

	/**
	 * Used to save the currently loaded settings into the Configuration Files
	 * 
	 * If config settings are already present, they will be overwritten
	 */
	public void saveCurrentToConfig(File configDirectory) {
		Gson gson = GsonHelper.createGson(true, new java.lang.reflect.Type[] { CreatureTypeSaveObject.class },
				new Object[] { new CreatureTypeSaveObject.CreatureTypeSaveObjectSerializer() });
		File creatureTypeFile = CreatureType
				.getFile(configDirectory, worldProperties.getFolderConfiguration().saveName);
		GsonHelper.writeToGson(FileUtilities.createWriter(creatureTypeFile, true), new CreatureTypeSaveObject(this),
				gson);
	}

	public void addCreatureType(CreatureTypeBuilder builder) {
		CreatureType type = builder.build(biomeGroupRegistry);
		LinkedHashMap<String, CreatureType> typeMap = new LinkedHashMap<String, CreatureType>();
		if (!typeMap.containsKey(type.typeID)) {
			typeMap.put(type.typeID, type);
			types = ImmutableMap.<String, CreatureType> builder().putAll(typeMap).build();
		}
	}

	public void removeCreatureType(String typeID) {
		LinkedHashMap<String, CreatureType> typeMap = new LinkedHashMap<String, CreatureType>();
		if (typeMap.remove(typeID) != null) {
			types = ImmutableMap.<String, CreatureType> builder().putAll(typeMap).build();
		}
	}

	public void removeCreatureType(CreatureType type) {
		removeCreatureType(type.typeID);
	}

	public void updateCreatureType(CreatureTypeBuilder builder) {
		CreatureType type = builder.build(biomeGroupRegistry);
		
		LinkedHashMap<String, CreatureType> typeMap = new LinkedHashMap<String, CreatureType>();
		typeMap.remove(type.typeID);
		typeMap.put(type.typeID, type);
		types = ImmutableMap.<String, CreatureType> builder().putAll(typeMap).build();
	}
}