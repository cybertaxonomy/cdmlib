/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@Entity
public class LivingIndividual extends Occurrence {
	static Logger logger = Logger.getLogger(LivingIndividual.class);
	private Set<LivingIndividual> parents = new HashSet();
	private Set<LivingIndividual> offspring = new HashSet();
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<LivingIndividual> getParents() {
		return parents;
	}
	protected void setParents(Set<LivingIndividual> parents) {
		this.parents = parents;
	}
	public void addParents(LivingIndividual parent) {
		this.parents.add(parent);
	}
	public void removeParents(LivingIndividual parent) {
		this.parents.remove(parent);
	}
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<LivingIndividual> getOffspring() {
		return offspring;
	}
	protected void setOffspring(Set<LivingIndividual> offspring) {
		this.offspring = offspring;
	}
	public void addOffspring(LivingIndividual offspring) {
		this.offspring.add(offspring);
	}
	public void removeOffspring(LivingIndividual offspring) {
		this.offspring.remove(offspring);
	}


}