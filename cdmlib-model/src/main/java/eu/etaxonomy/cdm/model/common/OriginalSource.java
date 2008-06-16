/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * Other names/labels/titles (abreviated or not) for the same object (person,
 * reference, source, etc.)
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:22
 */

@Entity
public class OriginalSource extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(OriginalSource.class);
	//The object's ID in the source, where the alternative string comes from
	private String idInSource;
	private IdentifiableEntity sourcedObj;

	/**
	 * Factory method
	 * @return
	 */
	public static OriginalSource NewInstance(){
		return new OriginalSource();
	}
	
	public static OriginalSource NewInstance(String id){
		OriginalSource result = new OriginalSource();
		result.setIdInSource(id);
		return result;
	}
	
	
	/**
	 * Constructor
	 */
	public OriginalSource(){
		super();
	}

/*************** GETTER /SETTER ************************************/
	
	public String getIdInSource(){
		return this.idInSource;
	}
	public void setIdInSource(String idInSource){
		this.idInSource = idInSource;
	}

	@Transient		
	public IdentifiableEntity getSourcedObj() {
		return sourcedObj;
	}
	public void setSourcedObj(IdentifiableEntity sourcedObj) {
		this.sourcedObj = sourcedObj;
	}

}