package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
	
	public NameTO getName(UUID uuid) throws CdmObjectNonExisting{
		TaxonNameBase tnb = getCdmTaxonNameBase(uuid);
		NameTO n = nameAssembler.getTO(tnb);
		return n;
	}
	
	public NameSTO getSimpleName(UUID uuid) throws CdmObjectNonExisting{
		TaxonNameBase tnb = getCdmTaxonNameBase(uuid);
		NameSTO n = nameAssembler.getSTO(tnb);
		return n;
	}
	
	public TaxonTO getTaxon(UUID uuid) throws CdmObjectNonExisting{
		TaxonBase tb = taxonDAO.findByUuid(uuid);
		if (tb==null){
			throw new CdmObjectNonExisting(uuid.toString(), TaxonBase.class);
		}
		TaxonTO t = taxonAssembler.getTO(tb);
		return t;
	}
	
	public TaxonSTO getSimpleTaxon(UUID uuid) throws CdmObjectNonExisting{
		TaxonBase tb = getCdmTaxonBase(uuid);
		TaxonSTO t = taxonAssembler.getSTO(tb);
		return t;
	}
	
	public Class whatis(UUID uuid) throws CdmObjectNonExisting{
		return this.getClass();
	}

	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, UUID sec,Set<UUID> higherTaxa, boolean matchAnywhere, boolean onlyAccepted, int page, int pagesize) {
		Random random = new Random();
		ResultSetPageSTO<TaxonSTO> rs = new ResultSetPageSTO<TaxonSTO>();
		rs.setPageSize(pagesize);
		rs.setPageNumber(page);
		// random results
		int x = random.nextInt(30);
		rs.setTotalResultsCount(x);
		for (int i=0; i<rs.getResultsOnPage(); i++){
			TaxonSTO tx = taxonAssembler.getRandom();
			rs.getResults().add(tx);
		}
		// result set metadata
		return rs;
	}

	public ResultSetPageSTO<TaxonSTO> getAcceptedTaxon(UUID uuid) throws CdmObjectNonExisting {
		TaxonBase tb = getCdmTaxonBase(uuid);
		ResultSetPageSTO<TaxonSTO> rs = new ResultSetPageSTO<TaxonSTO>();
		rs.setPageNumber(1);
		rs.setPageSize(25);
		if (tb.getClass().equals(Taxon.class)){
			TaxonSTO t = taxonAssembler.getSTO(tb);
			rs.addResultToFirstPage(t);
		}else{
			Synonym s = (Synonym)tb;
			Set<Taxon> taxa = s.getAcceptedTaxa();
			for (Taxon tx: taxa){
				TaxonSTO t = taxonAssembler.getSTO(tx);
				rs.addResultToFirstPage(t);
			}
		}
		return rs;
	}

	public List<TreeNode> getChildrenTaxa(UUID uuid) throws CdmObjectNonExisting {
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		Taxon tx = getCdmTaxon(uuid);
		for (Taxon t : tx.getTaxonomicChildren()){
			result.add(taxonAssembler.getTreeNode(t));
		}
		return result;
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

	public ReferenceTO getReference(UUID uuid) throws CdmObjectNonExisting{
		ReferenceBase ref = getCdmReferenceBase(uuid);
		ReferenceTO r =  refAssembler.getTO(ref);
		return r;
	}
	public ReferenceSTO getSimpleReference(UUID uuid) throws CdmObjectNonExisting{
		ReferenceBase ref = getCdmReferenceBase(uuid);
		ReferenceSTO r =  refAssembler.getSTO(ref);
		return r;
	}
	public List<TreeNode> getRootTaxa(UUID uuid) {
		List<Taxon> taxa = taxonDAO.getRootTaxa(null);
		return taxonAssembler.getTreeNodeList(taxa.toArray(new Taxon[0]));
	}

	public Set<ReferencedEntityBaseSTO> getTypes(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = false)
	public void saveTaxon(Taxon t){
		taxonDAO.save(t);
	}
	
	public ResultSetPageSTO<TaxonSTO> getAternativeTaxa(UUID uuid)
			throws CdmObjectNonExisting {
		// TODO Auto-generated method stub
		return null;
	}
	public List<NameSTO> getSimpleNames(Set<UUID> uuids){
		List<NameSTO> nameList = new ArrayList<NameSTO>();
		for (UUID u: uuids){
			try {
				nameList.add(getSimpleName(u));
			} catch (CdmObjectNonExisting e) {
				logger.warn("Name UUID "+u+" does not exist!");
			}
		}
	return nameList;
	}
	public List<ReferenceSTO> getSimpleReferences(Set<UUID> uuids) {
		List<ReferenceSTO> refList = new ArrayList<ReferenceSTO>();
		for (UUID u: uuids){
			try {
				refList.add(getSimpleReference(u));
			} catch (CdmObjectNonExisting e) {
				logger.warn("Reference UUID "+u+" does not exist!");
			}
		}
		return refList;
	}
	public List<TaxonSTO> getSimpleTaxa(Set<UUID> uuids){
	List<TaxonSTO> taxList = new ArrayList<TaxonSTO>();
	for (UUID u: uuids){
		try {
			taxList.add(getSimpleTaxon(u));
		} catch (CdmObjectNonExisting e) {
			logger.warn("Taxon UUID "+u+" does not exist!");
		}
	}
	return taxList;
	}

}
