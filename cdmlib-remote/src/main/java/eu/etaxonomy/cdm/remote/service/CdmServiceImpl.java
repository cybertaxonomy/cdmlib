package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.dto.assembler.NameSTOAssembler;
import eu.etaxonomy.cdm.remote.dto.assembler.TaxonSTOAssembler;

@Component
public class CdmServiceImpl implements CdmService {

	@Autowired
	private NameSTOAssembler nameSTOAssembler;	
	@Autowired
	private TaxonSTOAssembler taxonSTOAssembler;
	
	public NameTO getName(UUID uuid) {
		NameTO n=new NameTO();
		n.setFullname("Bella berolina subsp. rosa");
		n.setUuid(uuid.toString());
		return n;
	}
	public NameSTO getNameSTO() {
		return nameSTOAssembler.getRandom();
	}
	
	public TaxonTO getTaxon(UUID uuid) {
		TaxonTO t=new TaxonTO();
		t.setUuid(uuid.toString());
		return t;
	}
	public TaxonSTO getTaxonSTO() {
		return taxonSTOAssembler.getRandom();
	}
	
	public Class whatis(UUID uuid) {
		return this.getClass();
	}

	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, UUID sec,Set<UUID> higherTaxa, boolean matchAnywhere, boolean onlyAccepted, int pagesize, int page) {
		Random random = new Random();
		ResultSetPageSTO<TaxonSTO> rs = new ResultSetPageSTO<TaxonSTO>();
		rs.setPageSize(pagesize);
		rs.setPageNumber(page);
		// random results
		int x = random.nextInt(30);
		rs.setTotalResultsCount(x);
		for (int i=0; i<rs.getResultsOnPage(); i++){
			TaxonSTO tx = getTaxonSTO();
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
