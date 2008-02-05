package eu.etaxonomy.cdm.remote.service;

import com.sdicons.json.mapper.*; 
import com.sdicons.json.model.JSONValue;
import com.sdicons.json.serializer.marshall.*; 

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.jws.WebService;

import net.sf.json.JSONObject;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

public class CdmServiceImpl implements CdmService {

	public NameTO getName(String uuid) {
		NameTO n=new NameTO();
		n.setFullname("Bella berolina subsp. rosa");
		n.setUuid(uuid);
		n.addNameToken(new TaggedText(TagEnum.name,"Bella"));
		n.addNameToken(new TaggedText(TagEnum.name,"berolina"));
		n.addNameToken(new TaggedText(TagEnum.name,"subsp."));
		try {
			JSONValue jObj1 = JSONMapper.toJSON(n);
			JSONObject jObj2 = JSONObject.fromObject( n );  
			n.setUpdatedBy(jObj1.render(false));
			n.setCreatedBy(jObj2.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
