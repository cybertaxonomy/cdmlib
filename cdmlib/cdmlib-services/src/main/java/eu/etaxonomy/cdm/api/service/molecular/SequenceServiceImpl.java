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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.PreferenceServiceImpl;
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
    private static final Logger logger = Logger.getLogger(PreferenceServiceImpl.class);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
     */
    @Override
    @Autowired
    protected void setDao(ISequenceDao dao) {
        this.dao = dao;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ISequenceService#moveSingleRead(eu.etaxonomy.cdm.model.molecular.Sequence, eu.etaxonomy.cdm.model.molecular.Sequence, eu.etaxonomy.cdm.model.molecular.SingleRead)
     */
    @Override
    public boolean moveSingleRead(Sequence from, Sequence to, SingleRead singleRead) {
        from.removeSingleRead(singleRead);
        saveOrUpdate(from);
        to.addSingleRead(singleRead);
        saveOrUpdate(to);
        return true;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.molecular.ISequenceService#getSingleReadSequenceMap()
     */
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
}
