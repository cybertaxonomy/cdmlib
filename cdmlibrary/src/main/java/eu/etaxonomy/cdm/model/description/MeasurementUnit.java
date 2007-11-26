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
import eu.etaxonomy.cdm.model.common.Enumeration;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * This class contains the measurement units such as "centimeter" or "degree
 * Celsius"
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
public class MeasurementUnit extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(MeasurementUnit.class);

	public MeasurementUnit(String term, String label, Enumeration enumeration) {
		super(term, label, enumeration);
		// TODO Auto-generated constructor stub
	}
}