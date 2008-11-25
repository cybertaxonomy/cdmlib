/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * 
 * @author n.hoffmann
 * @created 14.07.2008
 * @version 1.0
 *
 */
public class NameTypeDesignationSTO extends TypeDesignationSTO {
	private static Logger logger = Logger
	.		getLogger(NameTypeDesignationSTO.class);
	
	private NameSTO typeSpeciesName;
	private ArrayList<NameSTO> typifiedNames = new ArrayList<NameSTO>();
	

	private IdentifiedString status;
	private boolean isRejectedType;
	private boolean isConservedType;
	private boolean isLectoType;
	private boolean isNotDesignated;
	
	/**
	 * @return the typeSpecies
	 */
	public NameSTO getTypeSpeciesName() {
		return typeSpeciesName;
	}
	/**
	 * @param typeSpecies the typeSpecies to set
	 */
	public void setTypeSpeciesName(NameSTO typeSpeciesName) {
		this.typeSpeciesName = typeSpeciesName;
	}
	
	/**
	 * @return the typifiedName
	 */
	public ArrayList<NameSTO> getTypifiedName() {
		return typifiedNames;
	}
	/**
	 * @param typifiedName the typifiedName to set
	 */
	public void setTypifiedName(NameSTO typifiedName) {
		this.typifiedNames.add(typifiedName);
	}
	
	/**
	 * @param typifiedName the typifiedName to set
	 */
	public void addTypifiedName(NameSTO typifiedName) {
		this.typifiedNames.add(typifiedName);
	}
	
	/**
	 * @return the status
	 */
	public IdentifiedString getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(IdentifiedString status) {
		//this.status = status;
		logger.warn("Method not implemented yet.");
	}
	/**
	 * @return the isRejectedType
	 */
	public boolean isRejectedType() {
		return isRejectedType;
	}
	/**
	 * @param isRejectedType the isRejectedType to set
	 */
	public void setRejectedType(boolean isRejectedType) {
		this.isRejectedType = isRejectedType;
	}
	/**
	 * @return the isConservedType
	 */
	public boolean isConservedType() {
		return isConservedType;
	}
	/**
	 * @param isConservedType the isConservedType to set
	 */
	public void setConservedType(boolean isConservedType) {
		this.isConservedType = isConservedType;
	}
	/**
	 * @return the isLectoType
	 */
	public boolean isLectoType() {
		return isLectoType;
	}
	/**
	 * @param isLectoType the isLectoType to set
	 */
	public void setLectoType(boolean isLectoType) {
		this.isLectoType = isLectoType;
	}
	/**
	 * @return the isNotDesignated
	 */
	public boolean isNotDesignated() {
		return isNotDesignated;
	}
	/**
	 * @param isNotDesignated the isNotDesignated to set
	 */
	public void setNotDesignated(boolean isNotDesignated) {
		this.isNotDesignated = isNotDesignated;
	}
}
