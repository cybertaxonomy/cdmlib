/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.PublicationBase;
/**
 * @author a.mueller
 * @version 1.0
 * @created 05-Aug-2008 22:06:45
 */
public class PublicationBaseDefaultCacheStrategy <T extends PublicationBase> implements IReferenceBaseCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(PublicationBaseDefaultCacheStrategy.class);
	

	/**
	 * Factory method
	 * @return
	 */
	public static PublicationBaseDefaultCacheStrategy NewInstance(){
		return new PublicationBaseDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private PublicationBaseDefaultCacheStrategy(){
		super();
	}
	
	
	
	protected String beforeYear = ". ";
	protected String afterYear = "";
	protected String afterAuthor = ", ";

	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("763fe4a0-c79f-4f14-9693-631680225ec3");
	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTitleCache(T publicationBase) {
		String result = "";
		if (publicationBase == null){
			return null;
		}
		String titel = CdmUtils.Nz(publicationBase.getTitle()).trim();
		//titelAbbrev
		String titelAbbrevPart = "";
		if (!"".equals(titel)){
			result = titel + blank; 
		}
		//delete .
		while (result.endsWith(".")){
			result = result.substring(0, result.length()-1);
		}
		
		result = addYear(result, publicationBase);
		TeamOrPersonBase team = publicationBase.getAuthorTeam();
		String author = CdmUtils.Nz(team == null ? "" : team.getTitleCache());
		if (! author.equals("")){
			result = author + afterAuthor + result;
		}
		return result;
	}
	

	
	protected String addYear(String string, T ref){
		String result;
		if (string == null){
			return null;
		}
		String year = CdmUtils.Nz(ref.getYear());
		if ("".equals(year)){
			result = string + afterYear;
		}else{
			result = string + beforeYear + year + afterYear;
		}
		return result;
	}



}
