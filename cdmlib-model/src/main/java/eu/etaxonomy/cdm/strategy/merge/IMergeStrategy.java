/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.strategy.merge;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.ICdmBase;


/**
 * @author a.mueller
 * @created 31.07.2009
 * @version 1.0
 */
public interface IMergeStrategy {

	public MergeMode getMergeMode(String propertyName);

	/**
	 * Sets the merge mode for property propertyName
	 * @param propertyName
	 * @param mergeMode
	 * @throws MergeException 
	 */
	public void setMergeMode(String propertyName, MergeMode mergeMode) throws MergeException;
	
	/**
	 * Sets the default merge mode for all properties.
	 * The default merge mode is used if no more specific merge mode is defined for a certain property.
	 * @param mergeMode
	 */
	public void setDefaultMergeMode(MergeMode defaultMergeMode);
	
	/**
	 * Sets the default merge mode for all collection properties.
	 * The default collection merge mode is used if no specific merge mode is defined for a certain collection property.
	 * @param mergeMode
	 */
	public void setDefaultCollectionMergeMode(MergeMode defaultCollectionMergeMode);

	
	/**
	 * Merges mergeSecond into mergeFirst.
	 * Returns a set of CdmBases that became orphant during the merge process and
	 * therefore must be deleted from a persistent context.
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @throws MergeException
	 */
	public <T extends IMergable> Set<ICdmBase> invoke(T mergeFirst, T mergeSecond) throws MergeException;

	/**
	 * Merges mergeSecond into mergeFirst.
	 * Returns a set of CdmBases that became orphant during the merge process and
	 * therefore must be deleted from a persistent context.
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @param clonedObjects a set of objects that needed to be cloned during merge. 
	 * This set will be filled during merge and should preferably be empty at the beginning 
	 * @throws MergeException
	 */
	public <T extends IMergable> Set<ICdmBase> invoke(T mergeFirst, T mergeSecond, Set<ICdmBase> clonedObjects) throws MergeException;

	/**
	 * If set to true the merge will only reallocate all references that point
	 * to the second entity to the first entity. All data attached to the 
	 * second entity will be completely lost. All data attached to the first 
	 * entity will be saved as it is. All other {@link MergeMode} information will
	 * be neglected.<BR>
	 * The second entity will finally be deleted.
	 * 
	 * #see {@link #setOnlyReallocateLinks(boolean)}
	 */
	 public boolean isOnlyReallocateReferences(); 
	
//	 Removed as this is not yet implemented in underlying classes
//	/**
//	 * Sets the onlyReallocateReferences parameter. See {@link #isOnlyReallocateReferences()}
//	 * for the parameters semantics.
//	 * @see #isOnlyReallocateReferences()
//	 * @param onlyReallocateReferences
//	 */
//	public void setOnlyReallocateLinks(boolean onlyReallocateReferences);
	
}
