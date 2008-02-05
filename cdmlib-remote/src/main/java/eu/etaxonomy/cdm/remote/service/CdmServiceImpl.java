package eu.etaxonomy.cdm.remote.service;

import java.util.List;
import java.util.UUID;

import javax.jws.WebService;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

public class CdmServiceImpl implements CdmService {

	public NameTO getName(String uuid) {
		NameTO n=new NameTO();
		n.setFullname("Bella berolina subsp. rosa");
		n.setUuid(uuid);
		return n;
	}

	public TaxonTO getTaxon(String uuid) {
		TaxonTO t=new TaxonTO();
		t.setUuid(uuid);
		return t;
	}

	public List<TaxonSTO> listNames(String beginsWith, boolean onlyAccepted, int pagesize, int page) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class whatis(String uuid) {
		return this.getClass();
	}


}
