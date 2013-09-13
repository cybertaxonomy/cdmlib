package eu.etaxonomy.cdm.persistence.hibernate.replace;

import java.util.List;

import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ReferringObjectMetadata {
	public List<CdmBase> getReferringObjects(CdmBase x,Session session);
	
	public void replace(CdmBase referringObject, CdmBase x, CdmBase y) throws IllegalArgumentException, IllegalAccessException;
	
	public Class<? extends CdmBase> getType();
	
	public String getFieldName();

}
