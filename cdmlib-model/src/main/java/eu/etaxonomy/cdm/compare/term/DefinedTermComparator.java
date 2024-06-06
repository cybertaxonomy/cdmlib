/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.term;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;

/**
 * @author muellera
 * @since 10.04.2024
 */
public class DefinedTermComparator implements Comparator<DefinedTermBase<?>> {

    @Override
    public int compare(DefinedTermBase<?> t1, DefinedTermBase<?> t2) {
        if (t1 == null || t2 == null) {
            if (t1 == null) {
                return t2 == null ? 0 : 1;
            } else {
                return -1;
            }
        }

        String label1 = t1.getTitleCache();
        String label2 = t2.getTitleCache();

        int c = CdmUtils.nullSafeCompareTo(label1, label2);
        if (c == 0) {
            c = CdmUtils.nullSafeCompareTo(t1.getIdInVocabulary(), t2.getIdInVocabulary());
        }
        if (c == 0) {
            c = CdmUtils.nullSafeCompareTo(t1.getSymbol(), t2.getSymbol());
        }
        if (c == 0) {
            c = CdmUtils.nullSafeCompareTo(t1.getSymbol2(), t2.getSymbol2());
        }
        if (c == 0) {
            c = CdmUtils.nullSafeCompareTo(t1.getUuid(), t2.getUuid());
        }

        return c;
    }

}
