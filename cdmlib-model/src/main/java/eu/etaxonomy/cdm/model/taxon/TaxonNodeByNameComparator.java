// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import java.util.Comparator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.AbstractStringComparator;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * Comparator that compares two TaxonNode instances by the titleCache of their referenced names.
 * @author a.kohlbecker
 * @date 24.06.2009
 *
 */
@Component
public class TaxonNodeByNameComparator extends AbstractStringComparator<TaxonNode> implements Comparator<TaxonNode>, ITaxonNodeComparator<TaxonNode> {

	private static final String HYBRID_SIGN = "\u00D7";

	private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparator.class);

    private boolean ignoreHybridSign = true;
    private boolean sortInfraGenericFirst = true;

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(TaxonNode o1, TaxonNode o2) {

        String titleCache1 = createSortableTitleCache(o1);
        String titleCache2 = createSortableTitleCache(o2);

        if(isIgnoreHybridSign()) {
        	logger.trace("ignoring Hybrid Signs: " + HYBRID_SIGN);
            titleCache1 = titleCache1.replace(HYBRID_SIGN, "");
            titleCache2 = titleCache2.replace(HYBRID_SIGN, "");
        }

        titleCache1 = applySubstitutionRules(titleCache1);
        titleCache2 = applySubstitutionRules(titleCache2);

        // 1
        StringTokenizer s2 = new StringTokenizer(titleCache1, "\"");
        if (s2.countTokens()>0){
            titleCache1 = "";
        }
        while(s2.hasMoreTokens()){
            titleCache1 += s2.nextToken();
        }

        // 2
        s2 = new StringTokenizer(titleCache2, "\"");
        if (s2.countTokens()>0){
            titleCache2 = "";
        }

        while(s2.hasMoreTokens()){
            titleCache2 += s2.nextToken();
        }

        return titleCache1.compareTo(titleCache2);
    }


	private String createSortableTitleCache(TaxonNode taxonNode) {

		String titleCache = null;
		if(taxonNode.getTaxon() != null && taxonNode.getTaxon().getName() != null ){
			TaxonNameBase name = taxonNode.getTaxon().getName();
	        if (name instanceof NonViralName){
            	logger.trace(name + " isNonViralName");
            	NonViralName nonViralName = (NonViralName)name;
                if (name.isInfraSpecific()){
                	logger.trace(name + " isInfraSpecific");
                    if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
                        titleCache = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
                    }
                }
                if (name.isInfraGeneric()){
                	logger.trace(name + " isInfraGeneric");
                	titleCache = nonViralName.getGenusOrUninomial() + " " + nonViralName.getInfraGenericEpithet();
                }
                if (nonViralName.getRank().isSpeciesAggregate()){
                	logger.trace(name + " isSpeciesAggregate");
                	titleCache = nonViralName.getGenusOrUninomial() + " " + nonViralName.getSpecificEpithet();
                }

            }
            if (titleCache == null){
            	logger.trace("titleCache still null, using name.getTitleCache()");
                titleCache = name.getTitleCache();
            }
        }
		if (titleCache == null){
        	logger.trace("titleCache still null, using taxonNode id");
            titleCache = String.valueOf(taxonNode.getId());
        }
        logger.trace("SortableTitleCache: " + titleCache);
		return titleCache;
	}


    public boolean isIgnoreHybridSign() {
        return ignoreHybridSign;
    }

    public void setIgnoreHybridSign(boolean ignore) {
        this.ignoreHybridSign = ignore;
    }

    public boolean isSortInfraGenericFirst() {
        return sortInfraGenericFirst;
    }

    public void setSortInfraGenericFirst(boolean infraGenericFirst) {
        this.sortInfraGenericFirst = infraGenericFirst;
    }

}
