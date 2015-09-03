package ca.kijiji.contest;

import java.util.Comparator;
import java.util.Map;

/**
 * A comparison class, which uses helper map to impose a value ordering on some
 * map of objects. Comparator can be used to create a value-sorted map(such
 * as {@link ValueSortedMap ValueSortedMap}. Source of values to compare must be
 * passed to the class instance with {@link MapValueComparator#setOrderInfoMap
 * setOrderInfoMap} method. In case of equality of values, comparator utilizes
 * natural order of the keys.
 *
 * @param <K extends Comparable<K>> the type of map's keys that may be compared
 * @param <V extends Comparable<V>> the type of map's values that may be compared
 *
 * @author  Pavel Lukashenka
 */

public class MapValueComparator<K extends Comparable<K>, V extends Comparable<V>> implements Comparator<K> {

    private Map<K, V> orderInfoSource;

    public Map<K, V> setOrderInfoMap(Map<K, V> orderInfoSource) {
        return this.orderInfoSource = orderInfoSource;
    }

    public int compare(K o1, K o2) {
        V v1 = orderInfoSource.get(o1);
        V v2 = orderInfoSource.get(o2);
        return v1.equals(v2) ? o1.compareTo(o2) : v2.compareTo(v1);
    }
}
