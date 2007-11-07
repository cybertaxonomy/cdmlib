

/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
public interface IService{
}
