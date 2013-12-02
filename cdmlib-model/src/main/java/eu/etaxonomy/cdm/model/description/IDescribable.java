/**
 * 
 */
package eu.etaxonomy.cdm.model.description;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.IAnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * This interface is a common interface for all classes which
 * allow adding of {@link DescriptionBase descriptions}.
 * 
 * NOTE: For now we let it inherit only from {@link IAnnotatableEntity}
 * although all implementing classes are {@link IdentifiableEntity}.
 * The reason is that we want to be careful as maybe in future 
 * we may also want to have {@link IAnnotatableEntity annotatable entities}
 * with descriptions attached.
 * However, if this causes problems we may let {@link IDescribable}
 * inherit from {@link IIdentifiableEntity} in future.
 * 
 * @author a.mueller
 * @created 2013-12-02
 *
 */
public interface IDescribable<T extends DescriptionBase<?>> extends IAnnotatableEntity {

	public Set<T> getDescriptions();
	
	public void addDescription(T description);
	
}
