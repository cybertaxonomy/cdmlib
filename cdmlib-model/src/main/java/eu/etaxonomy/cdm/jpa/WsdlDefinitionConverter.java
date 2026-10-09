/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.jpa;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

import eu.etaxonomy.cdm.model.common.LSIDWSDLLocator;

/**
 * Converter for WSDL {@link Definition} persisted as CLOB.
 *
 * @author ben
 */
@Converter
public class WsdlDefinitionConverter implements AttributeConverter<Definition, String> {

    @Override
    public String convertToDatabaseColumn(Definition attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            StringWriter stringWriter = new StringWriter();
            WSDLWriter writer = wsdlFactory.newWSDLWriter();
            writer.writeWSDL(attribute, stringWriter);
            return stringWriter.getBuffer().toString();
        } catch (WSDLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Definition convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
            Reader reader = new StringReader(dbData);
            WSDLLocator locator = new LSIDWSDLLocator("wsdl", reader, Thread.currentThread().getContextClassLoader());
            return wsdlReader.readWSDL(locator);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
