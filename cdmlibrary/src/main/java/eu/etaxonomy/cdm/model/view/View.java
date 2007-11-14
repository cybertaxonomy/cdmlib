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
import eu.etaxonomy.cdm.model.Description;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
@Entity
public class View extends CdmBase implements IReferencedEntity{
	static Logger logger = Logger.getLogger(View.class);
	private String name;
	private String description;
	private ReferenceBase reference;
	private Set<View> superViews;
	private Set<CdmBase> members;
	private Set<CdmBase> nonMembers;
	
	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	public String getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
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
	public void setSuperViews(Set<View> superViews) {
		this.superViews = superViews;
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
	private void setMembers(Set<CdmBase> members) {
		this.members = members;
	}
	public void addMember(CdmBase member) {
		this.members.add(member);
	}
	public void removeMember(CdmBase member) {
		this.members.remove(member);
	}

	
	public Set<CdmBase> getNonMembers() {
		return nonMembers;
	}
	private void setNonMembers(Set<CdmBase> nonMembers) {
		this.nonMembers = nonMembers;
	}
	public void addNonMember(CdmBase member) {
		this.nonMembers.add(member);
	}
	public void removeNonMember(CdmBase member) {
		this.nonMembers.remove(member);
	}

}