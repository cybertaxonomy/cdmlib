package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import net.sf.dozer.util.mapping.converters.ConfigurableCustomConverter;
import net.sf.dozer.util.mapping.converters.CustomConverter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.dc.Relation;

/**
 * Default page converter implementation that makes a gross assumption that
 * the metadata are being served on the same server as the human-readable page,
 * and that the urls map to {relatedPagePrefix}{param}/{uuid}
 * 
 * @author ben.clark
 *
 */
public class DefaultRelatedPageConverter implements ConfigurableCustomConverter  {

	private String relatedPagePrefix = "/";
	
	public void setRelatedPagePrefix(String relatedPagePrefix) {
		this.relatedPagePrefix = relatedPagePrefix;
	}

	public Object convert(Object existingDestinationFieldValue,	Object sourceFieldValue, Class destinationClass, Class sourceClass,String param) {
		if(sourceFieldValue == null) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append(relatedPagePrefix);
		
		stringBuffer.append(param + "/");
		stringBuffer.append(((UUID)sourceFieldValue).toString());
		
		Relation relation = new Relation();
		try {
			relation.setResource(new URI(stringBuffer.toString()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return relation;
	}

}
