/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.compare;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;

/**
 * A {@link Comparator} for derivatives in an occurrence tree.
 * This comparator primarily works on the short label (usually
 * collection code and accession number). Therefore it should
 * not be taken for comparing top level units in the tree
 * which you usually compare by the full label.
 *
 * @author muellera
 * @since 21.02.2024
 */
public class DerivedUnitDtoComparator implements Comparator<DerivedUnitDTO> {

    private static final DerivedUnitDtoComparator instance = new DerivedUnitDtoComparator();

    public static final DerivedUnitDtoComparator INSTANCE() {
        return instance;
    }

    @Override
    public int compare(DerivedUnitDTO o1, DerivedUnitDTO o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        //compare short
        String lable1 = getLabel(o1);
        String lable2 = getLabel(o2);
        int c = lable1.compareTo(lable2);
        return c;
    }

    private String getLabel(DerivedUnitDTO dto) {
        if (StringUtils.isNotEmpty(dto.getSpecimenShortTitle())) {
            return dto.getSpecimenShortTitle();
        } else if (StringUtils.isNoneEmpty(dto.getLabel())) {
            return dto.getLabel();
        } else if (dto.getUuid() != null){
            //this should not happen therefore we can handle it here then handle explicitly in main method
            return dto.getUuid().toString();
        } else {
            return "ZZZZZ";
        }
    }

}
