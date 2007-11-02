/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * This class contains possible modulations for the InfoItems such as "variance",
 * "maximum", "often", "probably"
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:16
 */
@Entity
public class Modifier extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(Modifier.class);

}