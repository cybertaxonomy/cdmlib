/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * This PropertyEditor translates concatenated lists string identifiers into
 * {@link DefinedTermBase} instances. The instances can be identified either by
 * UUID or by ID The separator for concatenations is the colon: ','
 *
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 * @deprecated see {@link TermBasePropertyEditor}
 */
@Deprecated
public class TermBaseListPropertyEditor<T extends DefinedTermBase<?>> extends TermBasePropertyEditor<T> {

    public TermBaseListPropertyEditor(ITermService termService){
        super(termService);
    }

    @Override
    public void setAsText(String text) {

        List<DefinedTermBase> termList = null;

        String[] tokens = StringUtils.split(text, ',');
        if(tokens != null && tokens.length > 0){


            try{
                UUID.fromString(tokens[0]);
                // no exception! treat all tokens as UUID
                Set<UUID> uuids = new HashSet<UUID>(tokens.length);
                for(String token : tokens){
                    uuids.add(UUID.fromString(token));
                }
                termList = termService.find(uuids);
            } catch (IllegalArgumentException e1){
                try {
                    Integer.parseInt(tokens[0]);
                    // no exception! treat all tokens as ID

                } catch (IllegalArgumentException e2){
                    Set<Integer> ids = new HashSet<Integer>(tokens.length);
                    for(String token : tokens){
                        ids.add(Integer.parseInt(token));
                    }
                    termList = termService.findById(ids);
                }
            }

            if(termList == null){
                throw new java.lang.IllegalArgumentException("No TermBase instances found for the supplied list of identifiers " + text);
            }

            setValue(termList);
        }
    }

}
