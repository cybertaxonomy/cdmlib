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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                Restriction restriction = objectMapper.readValue(source, Restriction.class);
                // the below loop is detects UUID string representations and converts them to UUIDs
                // such conversion is needed for all user types, we are only handling UUIDs here quickly and dirty
                // TODO think about the best solution, handle the string to object conversion in
                // CdmEntityDaoBase.createRestriction(String propertyName, Object value, MatchMode matchMode) ?
                List<Object> convertedValues = new ArrayList<>(restriction.getValues().size());
                for(Object val : restriction.getValues()){
                    if(val.toString().matches("([a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)")){
                        convertedValues.add(UUID.fromString(val.toString()));
                    } else {
                        convertedValues.add(val);
                    }
                }
                restriction.setValues(convertedValues);
                return restriction ;
            } catch (JsonParseException | JsonMappingException e) {
                throw new IllegalArgumentException(e);
            }catch (IOException e) {
                // TODO more specific unchecked exception type?
                throw new RuntimeException(e);
            }

    }

}
