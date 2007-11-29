/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class DerivationEvent extends EventBase{
	static Logger logger = Logger.getLogger(DerivationEvent.class);

	private Set<PhysicalOrganism> parents = new HashSet();
	private Set<PhysicalOrganism> derived = new HashSet();
	private DerivationEventType type;
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<PhysicalOrganism> getParents() {
		return parents;
	}
	protected void setParents(Set<PhysicalOrganism> parents) {
		this.parents = parents;
	}
	public void addParent(PhysicalOrganism parent) {
		this.parents.add(parent);
	}
	public void removeParent(PhysicalOrganism parent) {
		this.parents.remove(parent);
	}
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<PhysicalOrganism> getDerived() {
		return derived;
	}
	protected void setDerived(Set<PhysicalOrganism> derived) {
		this.derived = derived;
	}
	public void addDerived(PhysicalOrganism parent) {
		this.parents.add(parent);
	}
	public void removeDerived(PhysicalOrganism parent) {
		this.parents.remove(parent);
	}

	
	@ManyToOne
	public DerivationEventType getType() {
		return type;
	}
	public void setType(DerivationEventType type) {
		this.type = type;
	}
}
