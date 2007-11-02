/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:25
 */
@Entity
public class PreservationMethod extends DefinedTermBase {
	static Logger logger = Logger.getLogger(PreservationMethod.class);

	@Description("")
	private static final int initializationClassUri = http://rs.tdwg.org/ontology/voc/Collection.rdf#SpecimenPreservationMethodTypeTerm;

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri
	 */
	public void setInitializationClassUri(initializationClassUri){
		;
	}

}