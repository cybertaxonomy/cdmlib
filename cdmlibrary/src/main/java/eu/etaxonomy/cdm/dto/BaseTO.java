/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.dto;

import java.util.Calendar;

/**
 * Data Transfer Object representing derived from the domain object {@link CdmBase}. 
 * 
 * @author a.kohlbecker
 * @author m.doering
 * @version 1.0
 * @created 11.12.2007 11:14:44
 *
 */
public class BaseTO {
	
	private String uuid;
	private Calendar created;
	private String createdBy;
	private Calendar updated;
	private String updatedBy;
	

}
