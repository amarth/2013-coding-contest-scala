package ca.kijiji.contest;

import java.io.InputStream;
import java.util.Map;
import java.util.SortedMap;

public class ParkingTicketsStats {
    public static SortedMap<String, Integer> sortStreetsByProfitability(InputStream parkingTicketsStream) {
        Map<String, Integer> streetFineMap =
                ParkingTicketsCounter$.MODULE$.sortStreetsByProfitability(parkingTicketsStream);

        ValueSortedMap<String, Integer> result =
                new ValueSortedMap<String, Integer>(new MapValueComparator<String, Integer>());
        result.putAll(streetFineMap);

        return result;
    }
}