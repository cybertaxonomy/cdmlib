// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 * @date 23 Sep 2015
 *
 */
public class PostMergeEntityListener implements MergeEventListener {



    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent)
     */
    @Override
    public void onMerge(MergeEvent event) throws HibernateException {

    }

    /* (non-Javadoc)
     * @see org.hibernate.event.spi.MergeEventListener#onMerge(org.hibernate.event.spi.MergeEvent, java.util.Map)
     */
    @Override
    public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
        // at this point the original entity to merge has already been copied to the result
        // => the result is an exact copy of the original EXCEPT or the the id which is set by hibernate
        // the following code sets the id in the original entity so that it can be used as a return value
        // for the CdmEntityDaoBase.merge(T transientObject, boolean returnTransientEntity) call
        if(event.getOriginal() != null && CdmBase.class.isAssignableFrom(event.getOriginal().getClass()) &&
                event.getResult() != null && CdmBase.class.isAssignableFrom(event.getResult().getClass())) {
            CdmBase original = (CdmBase) event.getOriginal();
            CdmBase result = (CdmBase) event.getResult();
            if(original != null && Hibernate.isInitialized(original) && original.getId() == 0 &&
                    result != null && Hibernate.isInitialized(result) && result.getId() > 0) {
                original.setId(result.getId());
                //FIXME: Once the EventType.SAVE_UPDATE listeners are cleaned up
                //       the same calls should be made on the result object
                //       followed by a copy (uncomment code below) to the
                //       original object
//                try {
//                    BeanUtils.copyProperties(original, result);
//                } catch (IllegalAccessException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
        }
    }

}
