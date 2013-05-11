package jas.common.spawner.creature.handler.parsing.keys;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Represent the readable Tags for OptionalSettings
 */
public enum Key {
    /* Category Tags */
    spawn("spawn", KeyParserSpawn.class), despawn("despawn", KeyParserDespawn.class),

    /* Properties */
    sky("sky", null), block("block", KeyParserBlock.class), blockFoot("blockFoot", KeyParserBlock.class), light(
            "light", KeyParserLight.class), entityCap("cap", KeyParserEntityCap.class), spawnRange("spawnRange",
            KeyParserSpawnRange.class), maxSpawnRange("maxSpawnRange", KeyParserMaxSpawnRange.class), despawnAge(
            "despawnAge", KeyParserDespawnAge.class), spawnRate("spawnRate", KeyParserSpawnRate.class), blockRange(
            "blockRange", KeyParserBlockRange.class), material("material", null), minSpawnHeight("minSpawnHeight",
            KeyParserMinHeight.class), maxSpawnHeight("maxSpawnHeight", KeyParserMaxHeight.class),

    /* Sub Tags */
    blockRangeX("blockRangeX", null), blockRangeY("blockRangeY", null), blockRangeZ("blockRangeZ", null),

    /**/
    UNKNOWN("", null);

    public final String key;
    private static final HashMap<String, Key> lookupEnum = new HashMap<String, Key>();
    static {
        for (Key key : EnumSet.allOf(Key.class))
            lookupEnum.put(key.key.toUpperCase(), key);
    }

    public KeyParser keyParser;

    Key(String key, Class<? extends KeyParser> keyParserClass) {
        this.key = key;
        if (keyParserClass != null) {
            try {
                keyParser = keyParserClass.getConstructor(new Class[] { KeyParser.class }).newInstance(
                        new Object[] { this });
            } catch (Exception exception) {
                exception.printStackTrace();
                return;
            }
        }
    }

    /**
     * Gets the Key associated with the String. Is not case Sensitive
     * 
     * @param string
     * @return
     */
    public static Key getKeybyString(String string) {
        Key value = lookupEnum.get(string.toUpperCase());
        if (value != null) {
            return value;
        } else {
            return UNKNOWN;
        }
    }
}
