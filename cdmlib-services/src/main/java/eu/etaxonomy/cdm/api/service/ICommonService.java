// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;


public interface ICommonService extends IService<OriginalSource>{
//	
//	/** find cdmBase by UUID**/
//	public abstract CdmBase getCdmBaseByUuid(UUID uuid);
//
//	/** save a reference and return its UUID**/
//	public abstract UUID saveCdmBase(CdmBase cdmBase);

	/** find cdmBase by UUID**/
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);

	
	/**
	 * Returns all CdmBase objects that reference the referencedCdmBase.
	 * For example, if referencedCdmBase is an agent it may return all taxon names
	 * that have this person as an author but also all books, articles, etc. that have 
	 * this person as an author
	 * @param referencedCdmBase
	 * @return
	 */
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase);
	
	/**
	 * Merges mergeSecond into mergeFirst. All references to mergeSecond will be replaced by references
	 * to merge first. If no merge strategy is defined (null), the DefaultMergeStrategy will be taken as default.
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @param mergeStrategy
	 * @throws MergeException
	 */
	public <T extends IMergable> void   merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException;
	
	/**
	 * Returns all objects that match the object to match according to the given match strategy.
	 * If no match strategy is defined the default match strategy is taken.
	 * @param <T>
	 * @param objectToMatch
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException;
		
	
	public List getHqlResult(String hqlQuery);

}
