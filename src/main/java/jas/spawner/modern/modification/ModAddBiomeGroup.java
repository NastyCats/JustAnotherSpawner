package jas.spawner.modern.modification;

import jas.common.JASLog;
import jas.common.JustAnotherSpawner;
import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
import jas.spawner.modern.spawner.biome.structure.StructureHandlerRegistry;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.entry.SpawnListEntry;
import jas.spawner.modern.spawner.creature.entry.SpawnListEntryBuilder;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.minecraft.entity.EntityLiving;

import org.apache.logging.log4j.Level;

import com.google.common.base.Optional;

public class ModAddBiomeGroup extends BaseModification {

	public final String groupName;
	public final Optional<String> configName;
	public final ArrayList<String> contents;

	public ModAddBiomeGroup(String groupName, ArrayList<String> contents) {
		this(groupName, null, contents);
	}

	public ModAddBiomeGroup(String groupName, String configName, ArrayList<String> contents) {
		this.groupName = groupName;
		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
		this.contents = contents;
	}

	@Override
	public void applyModification(BiomeGroupRegistry registry) {
		if (configName.isPresent()) {
			registry.addBiomeGroup(groupName, configName.get(), contents);
		} else {
			registry.addBiomeGroup(groupName, contents);
		}
	}

	@Override
	public void applyModification(BiomeSpawnListRegistry registry) {
		LivingHandlerRegistry livingHandlerRegistry = registry.livingHandlerRegistry;
		BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;
		LivingGroupRegistry livingGroupRegistry = registry.livingGroupRegistry;

		ImportedSpawnList importedSpawnList = JustAnotherSpawner.importedSpawnList();

		/* For all LivingGroups (that are not CreatureType NONE) */
		Collection<LivingHandler> livingHandlers = livingHandlerRegistry.getLivingHandlers();
		for (LivingHandler handler : livingHandlers) {
			if (handler.creatureTypeID.equalsIgnoreCase(CreatureTypeRegistry.NONE)) {
				continue;
			}

			BiomeGroup group = biomeGroupRegistry.getBiomeGroup(groupName);
			SpawnListEntryBuilder spawnListEntry = findVanillaSpawnListEntry(group, handler, importedSpawnList,
					biomeGroupRegistry, livingGroupRegistry);
			registry.addSpawnListEntry(spawnListEntry);
		}
	}

	private SpawnListEntryBuilder findVanillaSpawnListEntry(BiomeGroup group, LivingHandler livingHandler,
			ImportedSpawnList importedSpawnList, BiomeGroupRegistry biomeGroupRegistry,
			LivingGroupRegistry livingGroupRegistry) {
		for (String pckgNames : group.getBiomeNames()) {
			for (Integer biomeID : biomeGroupRegistry.pckgNameToBiomeID().get(pckgNames)) {
				Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
						.getSpawnableCreatureList(biomeID);
				for (String jasName : livingHandler.namedJASSpawnables) {
					Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
						if (spawnListEntry.entityClass.equals(livingClass)) {
							return new SpawnListEntryBuilder(livingHandler.livingID, group.groupID)
									.setWeight(spawnListEntry.itemWeight).setMinChunkPack(spawnListEntry.minGroupCount)
									.setMaxChunkPack(spawnListEntry.maxGroupCount);
						}
					}
				}
			}
		}
		return new SpawnListEntryBuilder(livingHandler.livingID, group.groupID);
	}
}
