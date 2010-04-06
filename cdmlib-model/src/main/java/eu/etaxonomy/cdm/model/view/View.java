/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view;


import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import java.util.*;

import javax.persistence.*;

/**
 * use ARCHIVE view/dataset to maintain an archive. All members of that view will
 * never be changed
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:01
 */
@Entity(name="CDM_VIEW")
public class View extends CdmBase implements IReferencedEntity{
	private static final long serialVersionUID = 3668860188614455213L;
	private static final Logger logger = Logger.getLogger(View.class);
	
	private String name;
	private String description;
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private ReferenceBase reference;
	@OneToMany(fetch = FetchType.LAZY)
	private Set<View> superViews = new HashSet<View>();
	@Transient
	private Set<CdmBase> members = new HashSet<CdmBase>();
	@Transient
	private Set<CdmBase> nonMembers = new HashSet<CdmBase>();
	
	public String getName(){
		logger.debug("getName");
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}

	public String getDescription(){
		return this.description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}

	@Transient
	public ReferenceBase getCitation() {
		return getReference();
	}

	public ReferenceBase getReference() {
		return reference;
	}

	public void setReference(ReferenceBase reference) {
		this.reference = reference;
	}
	
	public Set<View> getSuperViews() {
		return superViews;
	}

	public void addSuperView(View superView) {
		this.superViews.add(superView);
	}
	public void removeSuperView(View superView) {
		this.superViews.remove(superView);
	}
	
	public Set<CdmBase> getMembers() {
		return members;
	}

	public void addMember(CdmBase member) {
		this.members.add(member);
	}
	public void removeMember(ICdmBase member) {
		this.members.remove(member);
	}

	public Set<CdmBase> getNonMembers() {
		return nonMembers;
	}

	public void addNonMember(CdmBase member) {
		this.nonMembers.add(member);
	}
	public void removeNonMember(ICdmBase member) {
		this.nonMembers.remove(member);
	}
}