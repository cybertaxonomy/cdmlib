/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.molecular;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoBaseImpl;
import eu.etaxonomy.cdm.persistence.dao.molecular.ISequenceDao;

/**
 * @author pplitzner
 * @since 11.03.2014
 */
@Repository
public class SequenceDaoHibernateImpl extends AnnotatableDaoBaseImpl<Sequence> implements ISequenceDao{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public SequenceDaoHibernateImpl() {
        super(Sequence.class);
    }
}