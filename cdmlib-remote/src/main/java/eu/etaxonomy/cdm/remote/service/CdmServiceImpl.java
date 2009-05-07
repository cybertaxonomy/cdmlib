/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureTreeDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.dto.FeatureTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTreeTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferencedEntityBaseSTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.dto.assembler.AnnotationAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.DescriptionAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.NameAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.ReferenceAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.TaxonAssembler;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

@Service
@Transactional(readOnly = true)
public class CdmServiceImpl implements ICdmService {
	static Logger logger = Logger.getLogger(CdmServiceImpl.class);

	@Autowired
	private ReferenceAssembler refAssembler;	
	@Autowired
	private NameAssembler nameAssembler;	
	@Autowired
	private TaxonAssembler taxonAssembler;
	@Autowired
	private DescriptionAssembler descriptionAssembler;
	@Autowired
	private AnnotationAssembler annotationAssembler;
	@Autowired
	private ITaxonDao taxonDAO;
	@Autowired
	private ITaxonService taxonService;
	@Autowired
	private ITaxonNameDao nameDAO;
	@Autowired
	private IReferenceDao refDAO;
	@Autowired
	private IDescriptionDao descriptionDAO;
	@Autowired
	private IFeatureTreeDao featureTreeDAO;
	@Autowired
	private IFeatureDao featureDAO;
	
	
//FIXME commented out below, since refactoring is urgently needed see ticket#593 http://dev.e-taxonomy.eu/trac/ticket/593
//	@Autowired
//	private AnnotatableDaoImpl<AnnotatableEntity> annotatableDao;
	
	//--------------------------
	@Autowired
	private IReferenceService referenceService;
	
	
	private final int MAXRESULTS = 500;
	
	private static List<OrderHint> defaultReferenceOrder = new ArrayList();

	
	
//	private CdmEntityDaoBase entityDAO = new CdmEntityDaoBase<CdmBase>();
		
	
	public CdmServiceImpl() {
		
		super();
		defaultReferenceOrder.add(new OrderHint("authorTeam.persistentTitleCache", OrderHint.SortOrder.ASCENDING));
		defaultReferenceOrder.add(new OrderHint("year", OrderHint.SortOrder.ASCENDING));
		defaultReferenceOrder.add(new OrderHint("persistentTitleCache", OrderHint.SortOrder.ASCENDING));
	}

	
	/**
	 * find matching taxonbase instance or throw CdmObjectNonExisting exception.
	 * never returns null!
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting 
	 */
	private TaxonBase getCdmTaxonBase(UUID uuid) throws CdmObjectNonExisting{
		TaxonBase tb = taxonDAO.findByUuid(uuid);
		if (tb==null){
			throw new CdmObjectNonExisting(uuid.toString(), TaxonBase.class);
		}
		return tb;
	}
	private Taxon getCdmTaxon(UUID uuid) throws CdmObjectNonExisting{
		Taxon t = null;
		try {
			t = (Taxon) getCdmTaxonBase(uuid);
		} catch (ClassCastException e) {
			throw new CdmObjectNonExisting(uuid.toString(), Taxon.class);
		}
		return t;
	}
	private TaxonNameBase getCdmTaxonNameBase(UUID uuid) throws CdmObjectNonExisting{
		TaxonNameBase tnb = nameDAO.findByUuid(uuid);
		if (tnb==null){
			throw new CdmObjectNonExisting(uuid.toString(), TaxonNameBase.class);
		}
		return tnb;
	}
	private ReferenceBase getCdmReferenceBase(UUID uuid) throws CdmObjectNonExisting{
		ReferenceBase ref = refDAO.findByUuid(uuid);		
		if (ref==null){
			throw new CdmObjectNonExisting(uuid.toString(), ReferenceBase.class);
		}
		return ref;
	}
	
	public NameTO getName(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		TaxonNameBase tnb = getCdmTaxonNameBase(uuid);
		NameTO n = nameAssembler.getTO(tnb, locales);
		return n;
	}
	
	public NameSTO getSimpleName(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		TaxonNameBase tnb = getCdmTaxonNameBase(uuid);
		NameSTO n = nameAssembler.getSTO(tnb, locales);
		return n;
	}
	
	public TaxonTO getTaxon(UUID taxonUuid, UUID featureTreeUuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		TaxonBase tb = taxonDAO.findByUuid(taxonUuid);
		if (tb==null){
			throw new CdmObjectNonExisting(taxonUuid.toString(), TaxonBase.class);
		}
		FeatureTree featureTree = null;
		if (featureTreeUuid != null){
			featureTree = featureTreeDAO.findByUuid(featureTreeUuid);
			if (featureTree == null){
				throw new CdmObjectNonExisting(featureTreeUuid.toString(), FeatureTree.class);
			}
		}else{
			logger.info("No featureTree at this point. Building default tree with all available features.");
			featureTree = FeatureTree.NewInstance(featureDAO.list());
		}
		TaxonTO t = taxonAssembler.getTO(tb, featureTree, locales);
		return t;
	}
	
	public TaxonSTO getSimpleTaxon(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		TaxonBase tb = getCdmTaxonBase(uuid);
		TaxonSTO t = taxonAssembler.getSTO(tb, locales);
		return t;
	}
	
	public Class whatis(UUID uuid) throws CdmObjectNonExisting{
		//CdmBase cb = entityDAO.findByUuid(uuid);
		return this.getClass();
	}

	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, Set<UUID> sec, Set<UUID> higherTaxa, MatchMode matchMode, boolean onlyAccepted, int page, int pagesize, Enumeration<Locale> locales) {

		ITaxonServiceConfigurator taxonServiceConfig =  TaxonServiceConfiguratorImpl.NewInstance();
		
		taxonServiceConfig.setPageSize(pagesize);
		taxonServiceConfig.setPageNumber(page);
		taxonServiceConfig.setMatchMode(matchMode);
		taxonServiceConfig.setDoSynonyms(!onlyAccepted);
		taxonServiceConfig.setDoTaxaByCommonNames(true);
		taxonServiceConfig.setDoTaxa(true);
		taxonServiceConfig.setSearchString(q);
		
		if(sec.size()>0){
			for (UUID uuid : sec){
				ReferenceBase referenceBase = refDAO.findByUuid(uuid);
				taxonServiceConfig.setReferenceBase(referenceBase);
				break;
			}			
		}

		Pager<IdentifiableEntity> resultSetPage = taxonService.findTaxaAndNames(taxonServiceConfig);
		
		// use DTOs instead
		ResultSetPageSTO<TaxonSTO> dtoResultSetPage = new ResultSetPageSTO<TaxonSTO>();

		dtoResultSetPage.setPageNumber(page);
		dtoResultSetPage.setPageSize(pagesize);
		dtoResultSetPage.setTotalResultsCount(resultSetPage.getCount());
		
		dtoResultSetPage.setResultsOnPage(resultSetPage.getCount());
		for (IdentifiableEntity taxonBase : resultSetPage.getRecords()){
			dtoResultSetPage.getResults().add(taxonAssembler.getSTO((TaxonBase)taxonBase, locales));
		}
		
		return dtoResultSetPage;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.service.ICdmService#getAcceptedTaxon(java.util.Set, java.util.Enumeration)
	 */
	public Hashtable<String, List<TaxonSTO>> getAcceptedTaxa(Set<UUID> uuids, Enumeration<Locale> locales) throws CdmObjectNonExisting {
		
		Hashtable<String, List<TaxonSTO>> stoTable = new Hashtable<String, List<TaxonSTO>>();
		for (UUID uuid : uuids) {
			List<TaxonSTO> stoList = new ArrayList<TaxonSTO>();
			TaxonBase tb = getCdmTaxonBase(uuid);
			
			if (tb.getClass().equals(Taxon.class)){
				TaxonSTO t = taxonAssembler.getSTO(tb, locales);
				stoList.add(t);
			}else{ 
				Synonym s = (Synonym)tb;
				Set<Taxon> taxa = s.getAcceptedTaxa();
				for (Taxon tx: taxa){
					TaxonSTO t = taxonAssembler.getSTO(tx, locales);
					stoList.add(t);
				}
			}
			stoTable.put(uuid.toString(), stoList);
		}
		return stoTable;
	}

	public List<TreeNode> getChildTaxa(UUID uuid) throws CdmObjectNonExisting {
		Taxon tx = getCdmTaxon(uuid);
		return taxonAssembler.getTreeNodeListSortedByName(tx.getTaxonomicChildren());
	}
	
	//TODO get rid of the bloodyRankLabelMap ------ can be deleted once the FIXME in getPathToRoot is solved
	private static Hashtable<String, String> bloodyRankLabelMap = new Hashtable<String, String>();	
	static{
		bloodyRankLabelMap.put("Subfamily", "Subfamilia");
		bloodyRankLabelMap.put("Family", "Familia");
		bloodyRankLabelMap.put("Suborder", "Subordo");
		bloodyRankLabelMap.put("Order", "Ordo");
	}
		
	public List<TreeNode> getParentTaxa(UUID uuid) throws CdmObjectNonExisting {
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		Taxon tx = getCdmTaxon(uuid);
		result.add(taxonAssembler.getTreeNode(tx));
		Rank rank = Rank.GENUS();
		
		while(tx.getTaxonomicParent() != null){
		
			Taxon parent = tx.getTaxonomicParent();
			//FIXME orderindex in parentTaxon.getName().getRank() is not set !!!
			// original: if(rank != null && rank.isLower(parentTaxon.getName().getRank())){
			// Preliminary solution below:
			if(rank != null){
				try {
					String bloodyRankLabel = bloodyRankLabelMap.get(parent.getName().getRank().getLabel());
					if(bloodyRankLabel == null){
						bloodyRankLabel = parent.getName().getRank().getLabel();
					}
					Rank compareToRank = Rank.getRankByName(bloodyRankLabel);
					if(rank.isLower(compareToRank)){
						break;
					}
				} catch (UnknownCdmTypeException e) {
					logger.error(e);
				}
			}
			
			result.add(taxonAssembler.getTreeNode(parent));
			tx=parent;
		}
		return result;
	}

	public ReferenceBase getReference(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		/*ReferenceBase ref = getCdmReferenceBase(uuid);
		ReferenceTO r =  refAssembler.getTO(ref, locales); */
		ReferenceBase r = referenceService.getReferenceByUuid(uuid);
		/* Initialise required collections .. */
		return r;
	}
	
	public Pager<ReferenceBase> listReferences(Integer pageSize, Integer pageNumber) throws CdmObjectNonExisting {
		
		return referenceService.getAllReferences(pageSize, pageNumber, defaultReferenceOrder);	
	}
	
	
	public ReferenceSTO getSimpleReference(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		ReferenceBase ref = getCdmReferenceBase(uuid);
		ReferenceSTO r =  refAssembler.getSTO(ref, locales);
		return r;
	}
	public List<TreeNode> getRootTaxa(UUID uuid) throws CdmObjectNonExisting {
		ReferenceBase sec = null;
		if(uuid != null){
			sec = getCdmReferenceBase(uuid);
		}
		//List<Taxon> rt = taxonDAO.getRootTaxa(Rank.GENUS(), sec, null, true, false);
		List<Taxon> rt = taxonDAO.getRootTaxa(sec, null, true, false);
		return taxonAssembler.getTreeNodeListSortedByName(rt);
	}

	public Set<ReferencedEntityBaseSTO> getTypes(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = false)
	public void saveTaxon(Taxon t){
		taxonDAO.save(t);
	}
	
	public ResultSetPageSTO<TaxonSTO> getAternativeTaxa(UUID uuid, Enumeration<Locale> locales)
			throws CdmObjectNonExisting {
		// TODO Auto-generated method stub
		return null;
	}
	public List<NameSTO> getSimpleNames(Set<UUID> uuids, Enumeration<Locale> locales){
		List<NameSTO> nameList = new ArrayList<NameSTO>();
		for (UUID u: uuids){
			try {
				nameList.add(getSimpleName(u, locales));
			} catch (CdmObjectNonExisting e) {
				logger.warn("Name UUID "+u+" does not exist!");
			}
		}
	return nameList;
	}
	public List<ReferenceSTO> getSimpleReferences(Set<UUID> uuids, Enumeration<Locale> locales) {
		List<ReferenceSTO> refList = new ArrayList<ReferenceSTO>();
		for (UUID u: uuids){
			try {
				refList.add(getSimpleReference(u,locales));
			} catch (CdmObjectNonExisting e) {
				logger.warn("Reference UUID "+u+" does not exist!");
			}
		}
		return refList;
	}
	public List<TaxonSTO> getSimpleTaxa(Set<UUID> uuids, Enumeration<Locale> locales){
		List<TaxonSTO> taxList = new ArrayList<TaxonSTO>();
		for (UUID u: uuids){
			try {
				taxList.add(getSimpleTaxon(u,locales));
			} catch (CdmObjectNonExisting e) {
				logger.warn("Taxon UUID "+u+" does not exist!");
			}
		}
		return taxList;
	}
	public List<FeatureTO> getFeatures(Enumeration<Locale> locales) throws CdmObjectNonExisting {
		List<FeatureTO> featureList = new ArrayList<FeatureTO>();
		
		for (Feature feature : featureDAO.list()){
			featureList.add(descriptionAssembler.getTO(feature, locales));
		}
		
		return featureList;
	}
	public List<FeatureTreeTO> getFeatureTrees(Enumeration<Locale> locales)
			throws CdmObjectNonExisting {
		List<FeatureTreeTO> featureTreeList = new ArrayList<FeatureTreeTO>();
		
		for (FeatureTree featureTree : featureTreeDAO.list()){
			featureTreeList.add(descriptionAssembler.getTO(featureTree, locales));
		}
		
		return featureTreeList;
	}
	
//FIXME commented out below, since refactoring is urgently needed see ticket#593 http://dev.e-taxonomy.eu/trac/ticket/593
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.remote.service.ICdmService#getAnnotations(java.util.UUID)
//	 */
//	public AnnotationTO getAnnotation(UUID uuid, Enumeration<Locale> locales)
//			throws CdmObjectNonExisting {
//		List<AnnotationTO> annotationList = new ArrayList<AnnotationTO>();
//		
//		// TODO the cast should be removed, but i (niels) don't know how at the moment
//		AnnotatableEntity annotatableEntity = (AnnotatableEntity) annotatableDao.findByUuid(uuid);
//		
//		
//		return annotationAssembler.getTO(annotatableEntity, locales);
//	}
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.remote.service.ICdmService#saveAnnotation(java.util.UUID, eu.etaxonomy.cdm.model.common.Annotation)
//	 */
//	@Transactional(readOnly=false)
//	public UUID saveAnnotation(UUID uuid, Annotation annotation)
//			throws CdmObjectNonExisting {
//		
//		AnnotatableEntity annotatableEntity = (AnnotatableEntity) annotatableDao.findByUuid(uuid);
//		
//		annotatableEntity.addAnnotation(annotation);
//		
//		UUID updatedUuid = annotatableDao.saveOrUpdate(annotatableEntity);
//		annotatableDao.flush();
//		
//		return updatedUuid;
//	}
}
