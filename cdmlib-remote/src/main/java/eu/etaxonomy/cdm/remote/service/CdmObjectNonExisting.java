// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

/**
 * Exception for cases when a CDM object identified by its UUID is not existing
 * or the UUID is malformed and not even a valid UUID 
 * @author m.doering
 *
 */
public class CdmObjectNonExisting extends Exception {
	   public CdmObjectNonExisting(String uuid)
	   { 
	      super("CDM object not existing: "+uuid);
	   }
	   public CdmObjectNonExisting(String uuid, Class clas)
	   { 
	      super("CDM object of class "+ clas.getSimpleName() +" not existing: "+uuid);
	   }
}
