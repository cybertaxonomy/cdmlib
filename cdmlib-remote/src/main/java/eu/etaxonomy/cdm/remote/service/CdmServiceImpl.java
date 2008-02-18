package eu.etaxonomy.cdm.remote.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

@Component
public class CdmServiceImpl implements CdmService {

	public NameTO getName(UUID uuid) {
		NameTO n=new NameTO();
		n.setFullname("Bella berolina subsp. rosa");
		n.setUuid(uuid.toString());
		n.addNameToken(new TaggedText(TagEnum.name,"Bella"));
		n.addNameToken(new TaggedText(TagEnum.name,"berolina"));
		n.addNameToken(new TaggedText(TagEnum.name,"subsp."));
		try {
			JSONObject jObj = JSONObject.fromObject( n );  
			n.setCreatedBy(jObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}

	public TaxonTO getTaxon(UUID uuid) {
		TaxonTO t=new TaxonTO();
		t.setUuid(uuid.toString());
		return t;
	}

	public List<TaxonSTO> listNames(String beginsWith, boolean onlyAccepted, int pagesize, int page) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class whatis(UUID uuid) {
		return this.getClass();
	}


}
