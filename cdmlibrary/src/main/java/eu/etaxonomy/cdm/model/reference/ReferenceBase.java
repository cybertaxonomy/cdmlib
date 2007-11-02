/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.reference;


import etaxonomy.cdm.model.agent.Team;
import etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:12
 */
public abstract class ReferenceBase extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(ReferenceBase.class);

	//URIs like DOIs, LSIDs or Handles for this reference 
	@Description("URIs like DOIs, LSIDs or Handles for this reference ")
	private String uri;
	//flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	@Description("flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a nomenclatural reference in a name this flag should be automatically set")
	private boolean isNomenclaturallyRelevant;
	private ArrayList referenceInSource;
	private Team authorTeam;

	public ArrayList getReferenceInSource(){
		return referenceInSource;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setReferenceInSource(ArrayList newVal){
		referenceInSource = newVal;
	}

	public Team getAuthorTeam(){
		return authorTeam;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAuthorTeam(Team newVal){
		authorTeam = newVal;
	}

	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUri(String newVal){
		uri = newVal;
	}

	public boolean isNomenclaturallyRelevant(){
		return isNomenclaturallyRelevant;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNomenclaturallyRelevant(boolean newVal){
		isNomenclaturallyRelevant = newVal;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}

}