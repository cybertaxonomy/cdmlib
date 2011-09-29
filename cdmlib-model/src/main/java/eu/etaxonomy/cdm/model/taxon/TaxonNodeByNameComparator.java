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

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.AbstractStringComparator;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * Comparator that compares two TaxonNode instances by the titleCache of their referenced names.
 * @author a.kohlbecker
 * @date 24.06.2009
 *
 */
@Component
public class TaxonNodeByNameComparator extends AbstractStringComparator implements Comparator<TaxonNode>, ITaxonNodeComparator<TaxonNode> {



    private boolean ignoreHybridSign = true;
    private boolean sortInfraGenericFirst = true;

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public int compare(TaxonNode o1, TaxonNode o2) {

        String titleCache1 = null;
        String titleCache2 = null;
        TaxonNameBase name1 = o1.getTaxon().getName();
        TaxonNameBase name2 = o2.getTaxon().getName();
        
              
        if(o1.getTaxon() != null && o1.getTaxon().getName() != null ){
        	       	
            if (o1.getTaxon().getName() instanceof NonViralName){
            	NonViralName nonViralName = (NonViralName)name1;
                if (name1.isInfraSpecific()){
                    if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
                        titleCache1 = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
                    }
                }
                if (name1.isInfraGeneric()){
                	titleCache1 = nonViralName.getGenusOrUninomial() + " " + nonViralName.getInfraGenericEpithet();
                }
                if (nonViralName.getRank().isSpeciesAggregate()){
                	titleCache1 = nonViralName.getGenusOrUninomial() + " " + nonViralName.getSpecificEpithet();
                }

            }
            if (titleCache1 == null){
                titleCache1 = name1.getTitleCache();
            }
        }
        if(o2.getTaxon() != null && o2.getTaxon().getName() != null){
            if (o2.getTaxon().getName() instanceof NonViralName){
            	NonViralName nonViralName = (NonViralName)name2;
                if (nonViralName.isInfraSpecific()){
                    if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
                        titleCache2 = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
                    }
                }
                if (nonViralName.isInfraGeneric()){
                	titleCache2 = nonViralName.getGenusOrUninomial() + " " + nonViralName.getInfraGenericEpithet();
                }
                if (nonViralName.getRank().isSpeciesAggregate()){
                	titleCache2 = nonViralName.getGenusOrUninomial() + " " + nonViralName.getSpecificEpithet();
                }

            }
            if (titleCache2 == null){
                titleCache2 = name2.getTitleCache();
            }
        }

        if(isIgnoreHybridSign()) {
            titleCache1 = titleCache1.replace("\u00D7", "");
            titleCache2 = titleCache2.replace("\u00D7", "");
        }

        titleCache1 = applySubstitutionRules(titleCache1);
        titleCache2 = applySubstitutionRules(titleCache2);

        StringTokenizer s2 = new StringTokenizer(titleCache1, "\"");
        if (s2.countTokens()>0){
            titleCache1 = "";
        }
        while(s2.hasMoreTokens()){
            titleCache1 += s2.nextToken();
        }
        s2 = new StringTokenizer(titleCache2, "\"");
        if (s2.countTokens()>0){
            titleCache2 = "";
        }

        while(s2.hasMoreTokens()){
            titleCache2 += s2.nextToken();
        }
        
        return titleCache1.compareTo(titleCache2);
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
