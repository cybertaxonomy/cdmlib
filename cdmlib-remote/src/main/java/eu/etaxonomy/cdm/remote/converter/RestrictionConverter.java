/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.converter;

import java.io.IOException;

import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.etaxonomy.cdm.persistence.dao.common.Restriction;

/**
 * Converter implementation to read a {@link Restriction} from its
 * json serialization.
 *
 * @author a.kohlbecker
 * @since Jun 3, 2019
 *
 */
public class RestrictionConverter implements Converter<String, Restriction<?>>  {

    private ObjectMapper objectMapper;

    public RestrictionConverter (ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Restriction<?> convert(String source) {

            try {
                return objectMapper.readValue(source, Restriction.class);
            } catch (JsonParseException | JsonMappingException e) {
                throw new IllegalArgumentException(e);
            }catch (IOException e) {
                // TODO more specific unchecked exception type?
                throw new RuntimeException(e);
            }

    }

}
