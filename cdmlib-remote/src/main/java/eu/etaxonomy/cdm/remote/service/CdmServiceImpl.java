package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.dto.assembler.NameAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.TaxonAssembler;

@Component
public class CdmServiceImpl implements CdmService {

	@Autowired
	private NameAssembler nameAssembler;	
	@Autowired
	private TaxonAssembler taxonAssembler;
	@Autowired
	private ITaxonDao taxonDAO;

	
	public NameTO getName(UUID uuid) {
		// FIXME: use real name DAO not taxon DAO!
		NameTO n = nameAssembler.getTO(taxonDAO.findByUuid(uuid).getName());
		return n;
	}
	
	public TaxonTO getTaxon(UUID uuid) {
		TaxonTO t = taxonAssembler.getTO(taxonDAO.findByUuid(uuid));
		return t;
	}
	
	public Class whatis(UUID uuid) {
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

	public ResultSetPageSTO<TreeNode> getParentTaxa(String beginsWith,
			boolean onlyAccepted) {
		// TODO Auto-generated method stub
		return null;
	}

}
