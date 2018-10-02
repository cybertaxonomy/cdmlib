/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph.TaxonGraphBeforeTransactionCompleteProcess;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
public class TaxonGraphHibernateListener implements PostInsertEventListener, PostUpdateEventListener {

    private static final long serialVersionUID = 5062518307839173935L;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {

        if(event.getEntity() instanceof TaxonName){
            event.getSession().getActionQueue().registerProcess(new TaxonGraphBeforeTransactionCompleteProcess(event));
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {

        if(event.getEntity() instanceof TaxonName){
            event.getSession().getActionQueue().registerProcess(new TaxonGraphBeforeTransactionCompleteProcess(event));
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return true;
    }

}
