

/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
public interface IService<T extends IdentifiableEntity>{

}