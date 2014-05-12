// $Id$
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

import eu.etaxonomy.cdm.model.common.CdmBase;
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

}