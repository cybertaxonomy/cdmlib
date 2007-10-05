

/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.event.ICdmEventListenerRegistration;

/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
public interface IService extends ICdmEventListenerRegistration{
	//FIXME: Does every service insert objects? If not, the ICdmEventListenerRegistration support should not be on ALL services.
}
