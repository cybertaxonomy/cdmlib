/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

import java.util.Set;


public interface ISourceable<T extends IOriginalSource> {

	/**
	 * Returns the set of original sources (citations) for this sourceable object
	 * @return
	 */
	public Set<T> getSources();

	/**
	 * Adds a set of original sources (citations) for this sourceable object
	 * @return
	 */
	public void addSources(Set<T> sources);
	/**
	 * Adds a source to this object
	 * @param source
	 */
	public void addSource(T source);


	/**
	 * Adds a source that is newly created by its components. If all components except for the
	 * type are <code>null</null> no source is added.
	 *
     * @param type the {@link OriginalSourceType type} of the source
     * @param idInSource the id used in the source
     * @param idNamespace the namespace for the id in the source
     * @param reference the source as a {@link Reference reference}
     * @param microReference the details (e.g. page number) in the reference
	 */
	public T addSource(OriginalSourceType type, String id, String idNamespace, Reference reference, String microReference);

    /**
     * @param type
     * @param reference
     * @param microReference
     * @param originalInformation
     * @return the added source
     */
	public T addSource(OriginalSourceType type, Reference reference, String microReference, String originalInformation);

	/**
	 * Adds a source which links to another CDM object as source.
	 * @param target the target CDM source
	 * @return the aggregation source
	 */
	public T addAggregationSource(ICdmTarget target);

	/**
	 * Removes a source from this object
	 * @param source
	 */
	public void removeSource(T source);

    /**
     * Adds a {@link IOriginalSource source} of type {@link OriginalSourceType#Import}
     * to this object.
     *
     * @param idInSource the id used in the source
     * @param idNamespace the namespace for the id in the source
     * @param reference the source as a {@link Reference reference}
     * @param microReference the details (e.g. page number) in the reference
     */
	public T addImportSource(String id, String idNamespace, Reference reference, String microReference);

    /**
     * Adds a {@link IOriginalSource source} of type {@link OriginalSourceType#PrimaryTaxonomicSource}
     * to this object.
     *
     * @param reference the source as a {@link Reference reference}
     * @param microReference the details (e.g. page number) in the reference
     */
	public T addPrimaryTaxonomicSource(Reference reference, String microReference);

    /**
     * Adds a {@link IOriginalSource source} of type
     * {@link OriginalSourceType#PrimaryTaxonomicSource}
     * to this object.
     *
     * @param reference the source as a {@link Reference reference}
     */
	public T addPrimaryTaxonomicSource(Reference reference);

}
