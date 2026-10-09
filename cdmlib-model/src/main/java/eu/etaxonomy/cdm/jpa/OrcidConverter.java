package eu.etaxonomy.cdm.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import eu.etaxonomy.cdm.model.agent.ORCID;

@Converter
public class OrcidConverter implements AttributeConverter<ORCID, String> {
    @Override
    public String convertToDatabaseColumn(ORCID attribute) {
        return attribute == null ? null : attribute.getDigitsOnly();
    }
    @Override
    public ORCID convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ORCID.fromString(dbData);
    }
}
