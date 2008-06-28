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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.criterion.MatchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;
import eu.etaxonomy.cdm.remote.dto.ReferencedEntityBaseSTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.dto.assembler.NameAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.ReferenceAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.TaxonAssembler;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

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
	private ITaxonDao taxonDAO;
	@Autowired
	private ITaxonNameDao nameDAO;
	@Autowired
	private IReferenceDao refDAO;
	
	private final int MAXRESULTS = 500;
	
//	private CdmEntityDaoBase entityDAO = new CdmEntityDaoBase<CdmBase>();
		
	
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
	
	public TaxonTO getTaxon(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		TaxonBase tb = taxonDAO.findByUuid(uuid);
		if (tb==null){
			throw new CdmObjectNonExisting(uuid.toString(), TaxonBase.class);
		}
		TaxonTO t = taxonAssembler.getTO(tb, locales);
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

	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, UUID sec, Set<UUID> higherTaxa, ITitledDao.MATCH_MODE matchMode, boolean onlyAccepted, int page, int pagesize, Enumeration<Locale> locales) {
		ResultSetPageSTO<TaxonSTO> rs = new ResultSetPageSTO<TaxonSTO>();

		rs.setPageNumber(page);
		rs.setTotalResultsCount((int)taxonDAO.countMatchesByName(q, matchMode, onlyAccepted));
//		if(MAXRESULTS > 0 && rs.getTotalResultsCount() > MAXRESULTS){
//			rs.setTotalResultsCount(-1);
//			return rs;
//		}
		// TODO: add other criteria. Has to be done in DAO...
		List<TaxonBase> results = taxonDAO.findByTitle(q, matchMode, page, pagesize, null);
		rs.setPageSize(results.size());
		for (TaxonBase tb : results){
			TaxonSTO tx = taxonAssembler.getSTO(tb, locales);
			rs.getResults().add(tx);
		}
		return rs;
	}

	public List<TaxonSTO> getAcceptedTaxon(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting {
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
		return stoList;
	}

	public List<TreeNode> getChildrenTaxa(UUID uuid) throws CdmObjectNonExisting {
		Taxon tx = getCdmTaxon(uuid);
		return taxonAssembler.getTreeNodeListSortedByName(tx.getTaxonomicChildren());
	}

	public List<TreeNode> getParentTaxa(UUID uuid) throws CdmObjectNonExisting {
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		Taxon tx = getCdmTaxon(uuid);
		result.add(taxonAssembler.getTreeNode(tx));
		while(tx.getTaxonomicParent() != null){
			Taxon parent = tx.getTaxonomicParent();
			result.add(taxonAssembler.getTreeNode(parent));
			tx=parent;
		}
		return result;
	}

	public ReferenceTO getReference(UUID uuid, Enumeration<Locale> locales) throws CdmObjectNonExisting{
		ReferenceBase ref = getCdmReferenceBase(uuid);
		ReferenceTO r =  refAssembler.getTO(ref, locales);
		return r;
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
		return taxonAssembler.getTreeNodeListSortedByName(taxonDAO.getRootTaxa(sec));
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

}
