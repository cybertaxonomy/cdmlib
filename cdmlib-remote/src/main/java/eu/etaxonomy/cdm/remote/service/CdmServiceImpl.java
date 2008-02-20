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
	Random random = new Random();

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
		ResultSetPageSTO<TaxonSTO> rs = new ResultSetPageSTO<TaxonSTO>();
		// random results
		rs.setTotalResultsCount(random.nextInt(30));
		for (int i=0; i<rs.getResultsOnPage(); i++){
			rs.getResults().add(getTaxonSTO());
		}
		// result set metadata
		//rs.setPageSize(pagesize);
		//rs.setPageNumber(page);
		return rs;
	}

	public ResultSetPageSTO<TreeNode> getParentTaxa(String beginsWith,
			boolean onlyAccepted) {
		// TODO Auto-generated method stub
		return null;
	}

}
