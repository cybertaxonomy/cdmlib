package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import org.hibernate.Hibernate;

import net.sf.dozer.util.mapping.CustomFieldMapperIF;
import net.sf.dozer.util.mapping.classmap.ClassMap;
import net.sf.dozer.util.mapping.fieldmap.FieldMap;

public class HibernateProxyFieldMapper implements CustomFieldMapperIF {

	public boolean mapField(Object source, Object destination, Object sourceFieldValue, ClassMap classMap, FieldMap fieldMapping) {
		if(sourceFieldValue != null && Hibernate.isInitialized(sourceFieldValue)) {
		  return false;
		} else {
			fieldMapping.writeDestValue(destination, null);
			return true;
		}
		
	}
}
