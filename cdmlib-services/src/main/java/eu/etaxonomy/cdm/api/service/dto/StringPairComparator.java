package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares instances of string {@link Pair}s ({@code Pair<String, String>})
 * @author pplitzner
 * @date Oct 5, 2015
 *
 */
public class StringPairComparator implements Comparator<Pair<String, String>>, Serializable {

    private static final long serialVersionUID = -5915469863898807071L;

    @Override
    public int compare(Pair<String, String> o1, Pair<String, String> o2) {
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }
        if (o1 != null && o2 != null) {
            return o1.getA().compareTo(o2.getA());
        }
        return 0;
    }
}