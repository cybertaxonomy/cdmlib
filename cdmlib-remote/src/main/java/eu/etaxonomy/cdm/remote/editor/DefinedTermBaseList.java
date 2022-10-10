/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;


/**
 * @author a.kohlbecker
 * @since Jun 25, 2013
 */
public class DefinedTermBaseList<T extends DefinedTermBase> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;

    public Set<T> asSet() {
        HashSet<T> tmpSet = new HashSet<>(this.size());

        Iterator<T> e = iterator();
        while (e.hasNext()) {
            tmpSet.add(e.next());
        }
        return tmpSet;
    }
}