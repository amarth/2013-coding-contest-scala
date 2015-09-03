package ca.kijiji.contest;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Map with value order. It uses {@link MapValueComparator MapValueComparator}
 * and helper map to put key-value pairs based in value order.
 *
 * @param <K extends Comparable<K>> the type of map's keys
 * @param <V extends Comparable<V>> the type of map's values
 *
 * @author  Pavel Lukashenka
 */

public class ValueSortedMap<K extends Comparable<K>, V extends Comparable<V>> extends TreeMap<K, V> {

    private Map<K, V> valueSource = new HashMap<K, V>();

    public ValueSortedMap(MapValueComparator<K, V> comparator) {
        super(comparator);
        comparator.setOrderInfoMap(valueSource);
    }

    @Override
    public V put(K key, V value) {
        valueSource.put(key, value);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V valueFromSuper = super.remove(key);
        valueSource.remove(key);
        return valueFromSuper;
    }
}