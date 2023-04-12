/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;

/**
 * @author a.mueller
 * @date 12.04.2023
 */
@Repository
public class TermCollectionDaoImpl
        extends IdentifiableDaoBase<TermCollection> implements ITermCollectionDao {

    public TermCollectionDaoImpl() {
        super(TermCollection.class);
    }

//   not yet clear if we want TermCollectionDaoImpl also as base class for other dao-s
//    protected TermCollectionDaoImpl(Class<TermCollection> type) {
//        super(type);
//    }
}
