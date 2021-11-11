/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class represents the result of a delete action.
 *
 * @author a.mueller
 * @since 04.01.2012
 */
public class DeleteResult extends UpdateResult{

    private static final long serialVersionUID = 8856465763413085548L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DeleteResult.class);

	private final Set<CdmBase> relatedObjects = new HashSet<>();
	private Set<CdmBase> deletedObjects = new HashSet<>();

//***************************** GETTER /SETTER /ADDER *************************/

	/**
	 * Related objects that prevent the delete action to take place.
	 */
	public Set<CdmBase> getRelatedObjects() {
		return relatedObjects;
	}
	public void addRelatedObject(CdmBase relatedObject) {
		this.relatedObjects.add(relatedObject);
	}
	public void addRelatedObjects(Set<? extends CdmBase> relatedObjects) {
		this.relatedObjects.addAll(relatedObjects);
	}

	//deleted objects
    public Set<CdmBase> getDeletedObjects() {
        return deletedObjects;
    }
    public void addDeletedObjects(Set<CdmBase> deletedObjects) {
        this.deletedObjects.addAll(deletedObjects);
    }
    public void addDeletedObject(CdmBase deletedObject) {
        this.deletedObjects.add(deletedObject);
    }

// ******************* Methods *************************************************/

    @Override
    public void includeResult(UpdateResult includedResult, boolean excludeStatusAndException){
        super.includeResult(includedResult, excludeStatusAndException);
        if (includedResult instanceof DeleteResult){
            DeleteResult includedDeleteResult = (DeleteResult)includedResult;
            this.addDeletedObjects(includedDeleteResult.getDeletedObjects());
            //Note: we do not include related objects as they loose there context, if needed in some cases in future it should be done paremeterized
        }
    }
}