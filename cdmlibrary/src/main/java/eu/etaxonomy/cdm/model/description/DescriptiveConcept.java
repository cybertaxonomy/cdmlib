/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Descriptive concepts (properties, object parts, observation methods, etc.)
 * define an optional ontology for descriptions. In contrast to characters,
 * concepts can not be scored in descriptions. - Reusable states and char.
 * dependencies are expressed here as well.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:21
 */
@Entity
public class DescriptiveConcept extends VersionableEntity {
	static Logger logger = Logger.getLogger(DescriptiveConcept.class);

}