

/**
 * 
 */
package eu.etaxonomy.cdm.api.service;


import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
public interface IService<T extends CdmBase>{


}