// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Comparator that compares two TaxonNode instances by the titleCache of their referenced names. 
 * @author a.kohlbecker
 * @date 24.06.2009
 *
 * TODO DISCUSS: move into model ?
 */
@Component
public class TaxonNodeByNameComparator extends AbstractStringComparator implements Comparator<TaxonNode>, ITaxonNodeComparator<TaxonNode> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public int compare(TaxonNode o1, TaxonNode o2) {
				
		String titleCache1 = null;
		String titleCache2 = null;
		
		if(o1.getTaxon() != null && o1.getTaxon().getName() != null ){
			
			if (o1.getTaxon().getName() instanceof NonViralName){
				NonViralName nonViralName = (NonViralName)o1.getTaxon().getName();
				if (nonViralName.isInfraSpecific()){
					if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
						titleCache1 = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
					}
				}
					
			}
			if (titleCache1 == null){
				titleCache1 = o1.getTaxon().getName().getTitleCache();
			}
		}
		if(o2.getTaxon() != null && o2.getTaxon().getName() != null){
			if (o2.getTaxon().getName() instanceof NonViralName){
				NonViralName nonViralName = (NonViralName)o2.getTaxon().getName();
				if (nonViralName.isInfraSpecific()){
					if (nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
						titleCache2 = nonViralName.getNameCache() + " "+nonViralName.getAuthorshipCache();
					}
				}
					
			}
			if (titleCache2 == null){
				titleCache2 = o2.getTaxon().getName().getTitleCache();
			}
		}
		
		titleCache1 = applySubstitutionRules(titleCache1);
		titleCache2 = applySubstitutionRules(titleCache2);
		
		StringTokenizer s2 = new StringTokenizer(titleCache1, "\""); 
		if (s2.countTokens()>0){
			titleCache1 = null;
		}
		while(s2.hasMoreTokens()){
			titleCache1 += s2.nextToken();
		}
		s2 = new StringTokenizer(titleCache2, "\""); 
		if (s2.countTokens()>0){
			titleCache2 = null;
		}
		
		while(s2.hasMoreTokens()){
			titleCache2 += s2.nextToken();
		}
		
		return titleCache1.compareTo(titleCache2);
	}
		
	
}
