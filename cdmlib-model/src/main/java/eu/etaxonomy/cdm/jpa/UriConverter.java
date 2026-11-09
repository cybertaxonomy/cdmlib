package eu.etaxonomy.cdm.jpa;

import java.net.URISyntaxException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.hibernate.TypeMismatchException;

import eu.etaxonomy.cdm.common.URI;

@Converter
public class UriConverter implements AttributeConverter<URI, String> {
    @Override
    public String convertToDatabaseColumn(URI attribute) {
        return attribute == null ? null : attribute.toString();
    }
    @Override
    public URI convertToEntityAttribute(String dbData) {
        if (dbData == null) { return null; }
        try { return new URI(dbData); }
        catch (URISyntaxException e) { throw new TypeMismatchException(e.getMessage()); }
    }
}
