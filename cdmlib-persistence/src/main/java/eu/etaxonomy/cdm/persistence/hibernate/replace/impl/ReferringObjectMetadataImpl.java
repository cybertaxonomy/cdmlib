package eu.etaxonomy.cdm.persistence.hibernate.replace.impl;

import java.lang.reflect.Field;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;

public abstract class ReferringObjectMetadataImpl implements ReferringObjectMetadata {
	protected Field field;
    protected String fieldName;
    protected Class<? extends CdmBase> type;
    
    public ReferringObjectMetadataImpl(Class fromClass, String propertyName,
			Class<? extends CdmBase> toClass) throws SecurityException, NoSuchFieldException {
		this.type = fromClass;
		this.fieldName = propertyName;
		this.field = type.getDeclaredField(fieldName);
		this.field.setAccessible(true);
		
	}
    
    public Class<? extends CdmBase> getType() {
    	return this.type;
    }
    
    public String getFieldName() {
    	return this.fieldName;
    }
    
}
