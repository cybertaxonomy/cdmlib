// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.PreferenceServiceImpl;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.UpdateResult.Status;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.persistence.dao.molecular.ISequenceDao;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
@Service
@Transactional(readOnly = true)
public class SequenceServiceImpl extends AnnotatableServiceBase<Sequence, ISequenceDao> implements ISequenceService{
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    @Autowired
    IOccurrenceService occurrenceService;

    @Override
    @Autowired
    protected void setDao(ISequenceDao dao) {
        this.dao = dao;
    }

    @Override
    public UpdateResult moveSingleRead(Sequence from, Sequence to, SingleRead singleRead) {
        UpdateResult result = new UpdateResult();
        from.removeSingleRead(singleRead);
        saveOrUpdate(from);
        to.addSingleRead(singleRead);
        saveOrUpdate(to);
        result.setStatus(Status.OK);
        result.addUpdatedObject(from);
        result.addUpdatedObject(to);
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public UpdateResult moveSingleRead(UUID fromUuid, UUID toUuid, UUID singleReadUuid) {
        SingleRead singleRead = null;
        Sequence from = CdmBase.deproxy(dao.load(fromUuid), Sequence.class);
        Sequence to = CdmBase.deproxy(dao.load(toUuid), Sequence.class);
        for(SingleRead sr : from.getSingleReads()) {
            if(sr.getUuid().equals(singleReadUuid)) {
                singleRead = sr;
                break;
            }
        }
        return moveSingleRead(from, to , singleRead);
    }

    @Override
    public Map<SingleRead, Collection<Sequence>> getSingleReadSequencesMap() {
        Map<SingleRead, Collection<Sequence>> singleReadToSequences = new HashMap<SingleRead, Collection<Sequence>>();
        for(Sequence sequence:list(Sequence.class, null, null, null, null)){
            for(SingleRead singleRead:sequence.getSingleReads()){
                Collection<Sequence> sequences = singleReadToSequences.get(singleRead);
                if(sequences==null){
                    sequences = new ArrayList<Sequence>();
                }
                sequences.add(sequence);
                singleReadToSequences.put(singleRead, sequences);
            }
        }
        return singleReadToSequences;
    }

    @Override
    @Transactional(readOnly = false)
    public DeleteResult delete(UUID fromUuid, SpecimenDeleteConfigurator config) {
        return occurrenceService.deleteDerivateHierarchy(dao.load(fromUuid),config);
    }
}
