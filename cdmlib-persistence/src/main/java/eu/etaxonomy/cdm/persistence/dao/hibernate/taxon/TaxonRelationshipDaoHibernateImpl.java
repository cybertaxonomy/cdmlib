/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.AnnotatableDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonRelationshipDao;

/**
 * @author a.mueller
 * @since 06.05.2021
 */
@Repository
@Qualifier("taxonRelationshipDaoHibernateImpl")
public class TaxonRelationshipDaoHibernateImpl
        extends AnnotatableDaoImpl<TaxonRelationship>
        implements ITaxonRelationshipDao{

    public TaxonRelationshipDaoHibernateImpl() {
        super(TaxonRelationship.class);
    }

}
