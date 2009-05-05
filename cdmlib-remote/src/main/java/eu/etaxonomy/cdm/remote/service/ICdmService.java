/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.service;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.dto.AnnotationTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTreeTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.NameTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;
import eu.etaxonomy.cdm.remote.dto.ReferencedEntityBaseSTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

/**
 * Methods adopted from ws_method as found in
 * http://dev.e-taxonomy.eu/svn/trunk/drupal/modules/cdm_dataportal/cdm_api.module
 * 
 * For URL mappings to these services please see:
 * src/main/webapp/WEB-INF/cdmrest-servlet.xml
 * 
 * @author m.doering
 * @author a.kohlbecker
 * @version 1.0
 * @created 30.1.2008
 */
public interface ICdmService {
	
	/**
	 * @param uuid
	 * @return 
	 * @throws CdmObjectNonExisting
	 */
	public Class whatis(UUID uuid) throws CdmObjectNonExisting;

	/**
	 * @param uuid
	 * @param locales TODO
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public NameTO getName(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;// throws BusinessLogicException;
	
	/**
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public NameSTO getSimpleName(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;// throws BusinessLogicException;
	
	/**
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public List<NameSTO> getSimpleNames(Set<UUID> uuids, Enumeration<Locale> locales) throws CdmObjectNonExisting;// throws BusinessLogicException;

	/**
	 * @param uuid
	 * @param enumeration 
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public TaxonTO getTaxon(UUID taxonUuid, UUID featureTreeUuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;
	
	/**
	 * @param uuid
	 * @return 
	 * @throws CdmObjectNonExisting
	 */
	public TaxonSTO getSimpleTaxon(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;
	
	/**
	 * @param uuid
	 * @return 
	 * @throws CdmObjectNonExisting
	 */
	public List<TaxonSTO> getSimpleTaxa(Set<UUID> uuids, Enumeration<Locale> locales) throws CdmObjectNonExisting;
	
	
	/**
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public ResultSetPageSTO<TaxonSTO> getAternativeTaxa(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;

	/**
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting 
	 */
	public ReferenceBase getReference(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;
	
	/**
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting 
	 */
	public Pager<ReferenceBase> listReferences(Integer pageSize, Integer pageNumber) throws CdmObjectNonExisting;
	
	
	public ReferenceSTO getSimpleReference(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;

	/**
	 * @param uuid
	 * @return
	 */
	public List<ReferenceSTO> getSimpleReferences(Set<UUID> uuids, Enumeration<Locale> locales);

	/**
	 * @param uuid
	 *            the name UUID
	 * @return a Set of type designation which are assigned to the name given
	 *         as parameter. The Set may contain {@link NameTypeDesignationSTO}
	 *         and {@link SpecimenTypeDesignationSTO}
	 */
	public Set<ReferencedEntityBaseSTO> getTypes(UUID uuid);

	/**
	 * @param uuid
	 *            the UUID of a {@link eu.etaxonomy.model.taxon.TaxonBase}
	 * @return the accepted taxon of a synonym given as
	 *         parameter uuid. If the synonym specified by uuid is itself the
	 *         accepted taxon, this one will be returned.
	 *         
	 *         For pro parte synonyms there might exist more than 1 accepted taxon,
	 *         therefore we return a List
	 * @throws CdmObjectNonExisting 
	 */
	public Hashtable<String, List<TaxonSTO>> getAcceptedTaxa(Set<UUID> uuids, Enumeration<Locale> locales) throws CdmObjectNonExisting;

	/**
	 * Find taxa matching the query defined by the given parameters.
	 * 
	 * @param q
	 *            name query string
	 * @param sec
	 *            the UUID of the concept reference
	 * @param higherTaxa
	 *            Set of taxon UUIDs, if higherTaxa are defined only taxa which
	 *            are included in one of these taxa are taken in to account
	 * @param matchMode
	 *            TODO ...
	 * @param onlyAccepted
	 *            return only taxa which are accepted in the sence of the
	 *            concept reference as given by parameter sec
	 * @param pagesize
	 *            maximum number of items per result page
	 * @param page
	 *            the number of the page in request
	 * @return a ResultSetPageSTO<TaxonSTO> instance
	 */
	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, Set<UUID> sec,
			Set<UUID> higherTaxa, MatchMode matchMode, boolean onlyAccepted,
			int pagesize, int page, Enumeration<Locale> locales);

	/**
	 * Searches the concept taxon tree for all parent taxa by walking the tree
	 * from the taxon which is referenced by the parameter uuid down to its
	 * root. The reference taxon will also be included into the returned set of
	 * {@link TreeNode} instances.
	 * 
	 * @param uuid
	 *            the UUID of the {@link eu.etaxonomy.model.taxon.Taxon}
	 * @return A List of all parent taxa including the one referenced by the
	 *         parameter uuid. The {@link TreeNode} elements are ordered in the
	 *         direction from root up to the references taxon.
	 * @throws CdmObjectNonExisting 
	 */
	public List<TreeNode> getParentTaxa(UUID uuid) throws CdmObjectNonExisting;

	/**
	 * Getter to retrieve the children of the taxon referenced by the parameter
	 * uuid.
	 * 
	 * @param uuid
	 *            the UUID of the {@link eu.etaxonomy.model.taxon.Taxon}
	 * @return A List of all taxa which are children in of the referenced taxon
	 *         in the concept tree.
	 * @throws CdmObjectNonExisting 
	 */
	public List<TreeNode> getChildTaxa(UUID uuid) throws CdmObjectNonExisting;

	/**
	 * Gets the root nodes of the taxonomic concept tree for the concept
	 * reference specified by the uuid parameter.
	 * If uuid is null return all available root taxa.
	 * 
	 * @param uuid
	 *            the concept reference uuid
	 * @return the List of root {@link TreeNode}s
	 * @throws CdmObjectNonExisting for non existing reference UUIDs
	 */
	public List<TreeNode> getRootTaxa(UUID uuid) throws CdmObjectNonExisting;

	/**
	 * 
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public List<FeatureTreeTO> getFeatureTrees(Enumeration<Locale> locales) throws CdmObjectNonExisting;

	/**
	 * 
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	public List<FeatureTO> getFeatures(Enumeration<Locale> locales) throws CdmObjectNonExisting;
	
//FIXME commented out below, since refactoring is urgently needed see ticket#593 http://dev.e-taxonomy.eu/trac/ticket/593
//	/**
//	 * 
//	 * @param uuid
//	 * @return
//	 * @throws CdmObjectNonExisting
//	 */
//	public AnnotationTO getAnnotation(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting;
//	
//	/**
//	 * 
//	 * @param uuid
//	 * @param annotation
//	 * @return
//	 * @throws CdmObjectNonExisting
//	 */
//	public UUID saveAnnotation(UUID uuid, Annotation annotation) throws CdmObjectNonExisting;
	
}
