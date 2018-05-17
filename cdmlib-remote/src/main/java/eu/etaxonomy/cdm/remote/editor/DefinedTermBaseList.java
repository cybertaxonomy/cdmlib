package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;


/**
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 */
public class DefinedTermBaseList<T extends DefinedTermBase> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;

    public Set<T> asSet() {
        HashSet<T> tmpSet = new HashSet<T>(this.size());

        Iterator<T> e = iterator();
        while (e.hasNext()) {
            tmpSet.add(e.next());
        }
        return tmpSet;
    }

}
