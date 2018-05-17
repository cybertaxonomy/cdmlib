/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
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
 * @since 2013-12-02
 *
 */
public interface IDescribable<T extends DescriptionBase<?>> extends IAnnotatableEntity {

	/**
	 * Returns the the set of descriptions attached to <code>this</code>
	 * {@link IDescribable}.
	 * @return
	 */
	public Set<T> getDescriptions();

	/**
	 * Adds a new description to the {@link #getDescriptions() set of descriptions}
	 * attached to <code>this</code> {@link IDescribable}.
	 * @param description
	 */
	public void addDescription(T description);

    /**
     * Removes one element from the set of {@link DescriptionBase descriptions} assigned
     * to <i>this</i> {@link IDescribable}.
     *
     * @param  description  the description which should be removed
     * @see                 #getDescriptions()
     * @see                 #addDescription(TaxonDescription)
     * @see                 eu.etaxonomy.cdm.model.description.TaxonDescription#getTaxon()
     */
    public void removeDescription(T description);

}
