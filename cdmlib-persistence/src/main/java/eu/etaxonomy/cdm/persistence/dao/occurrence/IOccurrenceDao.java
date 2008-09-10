/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.List;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
public interface IOccurrenceDao extends IIdentifiableDao<SpecimenOrObservationBase> {
	
	public List<Collection> getCollectionByCode(String code);
}
