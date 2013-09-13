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
		try {
		    this.field = type.getDeclaredField(fieldName);
		} catch(NoSuchFieldException nsfe) {
			Class superClass = type.getSuperclass();
			while(!superClass.equals(CdmBase.class)) {
				try{
					this.field = superClass.getDeclaredField(fieldName);
					break;
				} catch(NoSuchFieldException nsfe1) { }
				superClass = superClass.getSuperclass();
			}
			if(this.field == null) {
				throw nsfe;
			}
		}
		this.field.setAccessible(true);
		
	}
    
    public Class<? extends CdmBase> getType() {
    	return this.type;
    }
    
    public String getFieldName() {
    	return this.fieldName;
    }
    
}
