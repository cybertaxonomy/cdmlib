/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:50
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RightsTerm")
@XmlRootElement(name = "RightsTerm")
@Entity
public class RightsTerm extends DefinedTermBase<RightsTerm> {
	private static final long serialVersionUID = -5823263624000932116L;
	private static final Logger logger = Logger.getLogger(RightsTerm.class);

	
	/**
	 * Factory method
	 * @return
	 */
	public static RightsTerm NewInstance(){
		logger.debug("NewInstance");
		return new RightsTerm();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static RightsTerm NewInstance(String text, String label, String labelAbbrev){
		return new RightsTerm(text, label, labelAbbrev);
	}
	
	/**
	 * Default Constructor
	 */
	public RightsTerm() {
		super();
	}

	/**
	 * Constructor
	 */
	public RightsTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}


	/**
	 * http://purl.org/dc/terms/accessRights
	 */
	public static final RightsTerm ACCESS_RIGHTS(){
		return null;
	}

	public static final RightsTerm COPYRIGHT(){
		return null;
	}

	public static final RightsTerm LICENSE(){
		return null;
	}

}