package eu.etaxonomy.cdm.remote.editor.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;


/**
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 */
abstract public class EnumTermList<T extends IEnumTerm<T>> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;

    Class<T> enumTermClass;

    protected EnumTermList(Class<T> enumTermClass) {
        this.enumTermClass = enumTermClass;
    }

    public Set<T> asSet() {
        HashSet<T> tmpSet = new HashSet<T>(this.size());

        Iterator<T> e = iterator();
        while (e.hasNext()) {
            tmpSet.add(e.next());
        }
        return tmpSet;
    }

    public EnumTermListPropertyEditor<T> propertyEditor() {
        return new EnumTermListPropertyEditor<T>(
                EnumeratedTermVoc.getVoc(enumTermClass));
    }

}
