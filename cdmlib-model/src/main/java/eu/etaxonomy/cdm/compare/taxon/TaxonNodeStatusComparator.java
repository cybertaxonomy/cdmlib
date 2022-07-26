/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;

/**
 * This TaxonNodeStatus {@link Comparator comparator} compares two
 * {@link TaxonNodeStatus} as required by TaxonNode comparators like
 * {@link TaxonNodeByNameComparator} and {@link TaxonNodeByRankAndNameComparator}.
 * <BR>
 * For this the status {@link TaxonNodeStatus#DOUBTFUL} is considered no be equal
 * to NONE or Included as usually doubtful taxon nodes are not put to the end.
 *
 * @author a.mueller
 * @date 26.07.2022
 */
public class TaxonNodeStatusComparator implements Comparator<TaxonNodeStatus> {

    private static TaxonNodeStatusComparator singleton;

    public static final TaxonNodeStatusComparator INSTANCE() {
        if (singleton == null) {
            singleton = new TaxonNodeStatusComparator();
        }
        return singleton;
    }

    @Override
    public int compare(TaxonNodeStatus status1, TaxonNodeStatus status2) {

        //doubtful status is not ordered explicitly, see also TaxonNodeByRankAndNameComparator
        //TODO change this if we add a NONE/INCLUDED status
        status1 = status1 == TaxonNodeStatus.DOUBTFUL? null : status1;
        status2 = status2 == TaxonNodeStatus.DOUBTFUL? null : status2;

        if (CdmUtils.nullSafeEqual(status1, status2)){
            return 0;
        }else if (status1 == null){
            return -1;  //null status or doubtful should be put on top
        }else if (status2 == null){
            return 1;
        }else {
            //for now we use the natural order
            return status1.compareTo(status2);
        }
    }
}