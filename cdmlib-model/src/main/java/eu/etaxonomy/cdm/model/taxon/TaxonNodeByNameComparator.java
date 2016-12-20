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

import eu.etaxonomy.cdm.common.AbstractStringComparator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * Comparator that compares two TaxonNode instances by the titleCache of their referenced names.
 * @author a.kohlbecker
 * @date 24.06.2009
 *
 */
//@Component
public class TaxonNodeByNameComparator extends AbstractStringComparator<TaxonNode> implements Comparator<TaxonNode>, ITaxonNodeComparator<TaxonNode> {

    private static final String HYBRID_SIGN = "\u00D7";

    private static final Logger logger = Logger.getLogger(TaxonNodeByNameComparator.class);

    private boolean ignoreHybridSign = true;
    private boolean sortInfraGenericFirst = true;

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(TaxonNode o1, TaxonNode o2) {

        if (o1 == null && o2 == null) {
            return 0;
        }
        else if (o1 == null) {
            return 1;
        }
        else if (o2 == null) {
            return -1;
        }
        if (o1.equals(o2)){
        	return 0;
        }

        String titleCache1 = createSortableTitleCache(o1);
        String titleCache2 = createSortableTitleCache(o2);

        if(isIgnoreHybridSign()) {
            if (logger.isTraceEnabled()){logger.trace("ignoring Hybrid Signs: " + HYBRID_SIGN);}
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

        int result = titleCache1.compareTo(titleCache2);
        if (result != 0){
        	return result;
        }else{
        	return o1.getUuid().compareTo(o2.getUuid());
        }
    }


    private String createSortableTitleCache(TaxonNode taxonNode) {

        String titleCache = null;
        if(taxonNode.getTaxon() != null && taxonNode.getTaxon().getName() != null ){
            TaxonNameBase<?,?> name = HibernateProxyHelper.deproxy(taxonNode.getTaxon().getName(), TaxonNameBase.class);

            if (name instanceof NonViralName){
                if (logger.isTraceEnabled()){logger.trace(name + " isNonViralName");}
                NonViralName<?> nonViralName = (NonViralName<?>)name;
                if (nonViralName.getGenusOrUninomial() != null){
                    titleCache = nonViralName.getGenusOrUninomial();
                    if (name.isSpecies() && nonViralName.getSpecificEpithet() != null){
                        titleCache = titleCache + " " + nonViralName.getSpecificEpithet();
                    }
                	if (name.isInfraSpecific() && nonViralName.getSpecificEpithet() != null
                			&& nonViralName.getInfraSpecificEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isInfraSpecific");}
                		titleCache = titleCache + " " + nonViralName.getInfraSpecificEpithet();
                		if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
                			titleCache = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
                		}
                	}
                	if (name.isInfraGeneric() && nonViralName.getInfraGenericEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isInfraGeneric");}
                		titleCache = titleCache + " " + nonViralName.getInfraGenericEpithet();
                	}
                	if (nonViralName.isSpeciesAggregate() && nonViralName.getSpecificEpithet() != null){
                		if (logger.isTraceEnabled()){logger.trace(name + " isSpeciesAggregate");}
                		titleCache = nonViralName.getGenusOrUninomial() + " " + nonViralName.getSpecificEpithet();
                	}
                }

            }
            if (titleCache == null){
                if (logger.isTraceEnabled()){logger.trace("titleCache still null, using name.getTitleCache()");}
                titleCache = name.getTitleCache();
            }
        }
        if (titleCache == null){
            if (logger.isTraceEnabled()){logger.trace("titleCache still null, using taxonNode id");}
            titleCache = String.valueOf(taxonNode.getId());
        }
        if (logger.isTraceEnabled()){logger.trace("SortableTitleCache: " + titleCache);}
//        System.out.println(titleCache);
        return titleCache;
    }


    @Override
    public boolean isIgnoreHybridSign() {
        return ignoreHybridSign;
    }

    @Override
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
