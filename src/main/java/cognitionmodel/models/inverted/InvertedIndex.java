package cognitionmodel.models.inverted;


import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

import java.util.*;

public interface InvertedIndex {

    /**
     * Get all available fields in index
     * @return - List of fields
     *
     */

    public List<String> getFields();


    /**
     * Get all values of the field
     * @param field - field in index
     * @return - list of field values
     *
     * @throws IllegalArgumentException if field do not contained in index
     *
     */

    public List<Object> getAllValues(String field);

    /**
     * Get map of all values of the field
     * @param field - field in index
     * @return - treemap with all values and indices of records for every value
     *
     * @throws IllegalArgumentException if field do not contained in index
     */

    public TreeMap getMap(String field);


    public Set<Map.Entry<String, TreeMap<Object, RoaringBitmap>>> getEntrySet();
}
