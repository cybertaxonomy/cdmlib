/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.kohlbecker
 * @since Jun 25, 2013
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
