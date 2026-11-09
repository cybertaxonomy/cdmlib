package eu.etaxonomy.cdm.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import eu.etaxonomy.cdm.model.validation.Severity;

@Converter
public class SeverityConverter implements AttributeConverter<Severity, String> {
    @Override
    public String convertToDatabaseColumn(Severity attribute) {
        return attribute == null ? null : attribute.toString();
    }
    @Override
    public Severity convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Severity.forName(dbData);
    }
}
