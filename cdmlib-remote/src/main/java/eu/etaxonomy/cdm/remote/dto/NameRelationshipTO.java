/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 12:10:57
 *
 */
public class NameRelationshipTO extends ReferencedEntityBaseSTO {
	
	private LocalisedTermSTO type;
	
	private String ruleConsidered;

	// basic data on the referenced Name object:
	private Set<NameSTO> relatedNames = new HashSet<NameSTO>();

	/**
	 * @return the type
	 */
	public LocalisedTermSTO getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LocalisedTermSTO type) {
		this.type = type;
	}

	/**
	 * @return the ruleConsidered
	 */
	public String getRuleConsidered() {
		return ruleConsidered;
	}

	/**
	 * @param ruleConsidered the ruleConsidered to set
	 */
	public void setRuleConsidered(String ruleConsidered) {
		this.ruleConsidered = ruleConsidered;
	}

	/**
	 * @return the name
	 */
	public Set<NameSTO> getRelatedNames() {
		return relatedNames;
	}
	
	public void addName(NameSTO name){
		this.relatedNames.add(name);
	}

	/**
	 * @param name the name to set
	 */
	public void setRelatedNames(Set<NameSTO> relatedNames) {
		this.relatedNames = relatedNames;
	}
	
	
	
}
