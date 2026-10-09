package eu.etaxonomy.cdm.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import eu.etaxonomy.cdm.model.common.WikiDataItemId;

@Converter
public class WikiDataItemIdConverter implements AttributeConverter<WikiDataItemId, String> {
    @Override
    public String convertToDatabaseColumn(WikiDataItemId attribute) {
        return attribute == null ? null : attribute.getIdentifierWithQ();
    }
    @Override
    public WikiDataItemId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WikiDataItemId.fromString(dbData);
    }
}
