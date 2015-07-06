/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.common.ICdmBase;



public class SaveEntityListener implements SaveOrUpdateEventListener {
	private static final long serialVersionUID = -4295612947856041686L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SaveEntityListener.class);

	@Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event)	throws HibernateException {
		Object entity = event.getObject();

        if (entity != null){

            Class<?> entityClazz = entity.getClass();
			if(ICdmBase.class.isAssignableFrom(entityClazz)) {

				ICdmBase cdmBase = (ICdmBase)entity;
				cdmBase.setCreated(new DateTime());

			}
        }
	}
}
