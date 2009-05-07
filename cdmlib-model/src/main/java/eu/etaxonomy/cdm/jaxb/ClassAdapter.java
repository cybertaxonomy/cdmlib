package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ClassAdapter extends XmlAdapter<String, Class> {

	@Override
	public String marshal(Class clazz) throws Exception {
		return clazz.getCanonicalName();
	}

	@Override
	public Class unmarshal(String string) throws Exception {
		return Class.forName(string);
	}

}
