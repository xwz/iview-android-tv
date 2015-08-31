package io.github.xwz.abciview;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wei on 27/08/15.
 */
public class ImmutableMap {

    /**
     * Create a map as list of key value pairs of the argument.
     * <pre>
     * Map<Integer, Integer> pairs = ImmutableMap.of(1,3, 2, 3);
     * </pre>
     */
    public static <K, V> Map<K, V> of(Object... keyValPair) {
        Map<K, V> map = new LinkedHashMap<K, V>();

        if (keyValPair.length % 2 != 0) {
            throw new IllegalArgumentException("Keys and values must be pairs.");
        }

        for (int i = 0; i < keyValPair.length; i += 2) {
            map.put((K) keyValPair[i], (V) keyValPair[i + 1]);
        }
        return map;
    }
}
