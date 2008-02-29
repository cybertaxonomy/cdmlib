package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;
import eu.etaxonomy.cdm.remote.dto.ReferencedEntityBaseSTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.dto.assembler.NameAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.TaxonAssembler;

@Component
@Transactional(readOnly = true)
public class CdmServiceImpl implements ICdmService {

	@Autowired
	private NameAssembler nameAssembler;	
	@Autowired
	private TaxonAssembler taxonAssembler;
	@Autowired
	private ITaxonDao taxonDAO;

	
	/**
	 * find matching taxonbase instance and take care of errorhandling for springmvc
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
	
	public NameTO getName(UUID uuid) throws CdmObjectNonExisting{
		// FIXME: use real name DAO not taxon DAO!
		NameTO n = nameAssembler.getTO(getCdmTaxonBase(uuid).getName());
		return n;
	}
	
	public TaxonTO getTaxon(UUID uuid) throws CdmObjectNonExisting{
		TaxonTO t = taxonAssembler.getTO(getCdmTaxonBase(uuid));
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

	public Set<ReferenceSTO> getAllSecReferences() {
		// TODO Auto-generated method stub
		return null;
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

	public ReferenceTO getReference(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TreeNode> getRootTaxa(UUID uuid) {
		List<Taxon> taxa = taxonDAO.getRootTaxa(null);
		return taxonAssembler.getTreeNodeList(taxa.toArray(new Taxon[0]));
	}

	public Set<ReferencedEntityBaseSTO> getTypes(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}



}
