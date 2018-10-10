/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author a.kohlbecker
 * @since Oct 10, 2018
 *
 */
public interface ITaxonGraphHibernateListener extends PostInsertEventListener, PostUpdateEventListener {

    @Override
    boolean requiresPostCommitHanding(EntityPersister persister);

    @Override
    void onPostInsert(PostInsertEvent event);

    @Override
    void onPostUpdate(PostUpdateEvent event);

    boolean unRegisterProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass);

    boolean registerProcessClass(Class<? extends BeforeTransactionCompletionProcess> processClass);

}
