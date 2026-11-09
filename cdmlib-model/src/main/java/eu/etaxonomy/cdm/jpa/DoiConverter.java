package eu.etaxonomy.cdm.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import eu.etaxonomy.cdm.common.DOI;

@Converter
public class DoiConverter implements AttributeConverter<DOI, String> {
    @Override
    public String convertToDatabaseColumn(DOI attribute) {
        return attribute == null ? null : attribute.toString();
    }
    @Override
    public DOI convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DOI.fromString(dbData);
    }
}
