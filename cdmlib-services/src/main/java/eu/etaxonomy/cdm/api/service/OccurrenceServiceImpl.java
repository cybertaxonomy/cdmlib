/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;

/**
 * @author a.babadshanjan
 * @created 01.09.2008
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceServiceImpl extends IdentifiableServiceBase<SpecimenOrObservationBase> implements IOccurrenceService {

	static private final Logger logger = Logger.getLogger(OccurrenceServiceImpl.class);

	@Autowired
	protected void setDao(IOccurrenceDao dao) {
		this.dao = dao;
	}

	public OccurrenceServiceImpl() {
		logger.debug("Load OccurrenceService Bean");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#getAllspecimenOrObservationBases(int, int)
	 */
	public List<SpecimenOrObservationBase> getAllSpecimenOrObservationBases(
			int limit, int start) {
		return dao.list(limit, start);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IOccurrenceService#saveSpecimenOrObservationBaseAll(java.util.Collection)
	 */
	@Transactional(readOnly = false)
	public Map<UUID, SpecimenOrObservationBase> saveSpecimenOrObservationBaseAll(
			Collection<SpecimenOrObservationBase> specimenOrObservationBaseCollection) {
		return saveCdmObjectAll(specimenOrObservationBaseCollection);
	}

	
	@Transactional(readOnly = false)
	public UUID saveSpecimenOrObservationBase(
			SpecimenOrObservationBase specimenOrObservationBase) {
		return super.saveCdmObject(specimenOrObservationBase);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#generateTitleCache()
	 */
	public void generateTitleCache() {
		// TODO Auto-generated method stub
		logger.warn("Not yet implemented");
	}
}
