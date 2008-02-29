package eu.etaxonomy.cdm.remote.service;

import java.util.UUID;

/**
 * Exception for cases when a CDM object identified by its UUID is not existing
 * or the UUID is malformed and not even a valid UUID 
 * @author markus
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
