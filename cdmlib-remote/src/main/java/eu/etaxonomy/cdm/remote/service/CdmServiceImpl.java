package eu.etaxonomy.cdm.remote.service;

import java.util.List;
import java.util.UUID;

import javax.jws.WebService;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

public class CdmServiceImpl implements CdmService {

	@WebService
	public class Hello {
	  public String sayHi(String name) {
	    return "Hello " + name;
	  }
	}
	
	public NameTO getName(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	public TaxonTO getTaxon(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TaxonSTO> listNames(String beginsWith, int page,
			boolean onlyAccepted, int pagesize) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class whatis(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
