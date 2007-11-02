/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:34
 */
@MappedSuperclass
public abstract class AnnotatableEntity extends VersionableEntity {
	static Logger logger = Logger.getLogger(AnnotatableEntity.class);

	private ArrayList markers;
	private ArrayList annotations;

	public ArrayList getMarkers(){
		return markers;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMarkers(ArrayList newVal){
		markers = newVal;
	}

	public ArrayList getAnnotations(){
		return annotations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAnnotations(ArrayList newVal){
		annotations = newVal;
	}

}