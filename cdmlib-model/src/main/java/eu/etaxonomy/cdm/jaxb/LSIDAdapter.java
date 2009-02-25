package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import eu.etaxonomy.cdm.model.common.LSID;


public class LSIDAdapter extends XmlAdapter<String, LSID>{

	public String marshal(LSID lsid) throws Exception {
		return lsid.getLsid();
	}

	public LSID unmarshal(String string) throws Exception {
		return new LSID(string);
	}

}
