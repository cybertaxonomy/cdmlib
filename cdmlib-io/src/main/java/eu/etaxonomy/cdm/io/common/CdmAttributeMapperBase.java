/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 05.08.2008
 * @version 1.0
 */
public abstract class CdmAttributeMapperBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmAttributeMapperBase.class);
	
	public abstract Set<String> getSourceAttributes();
	
	public abstract Set<String> getDestinationAttributes();
	
	public abstract List<String> getSourceAttributeList();
	
	public abstract List<String> getDestinationAttributeList();
	
}