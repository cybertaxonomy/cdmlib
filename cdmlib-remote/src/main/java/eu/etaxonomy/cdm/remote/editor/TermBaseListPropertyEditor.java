/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.editor;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * This PropertyEditor translates concatenated lists string identifiers
 * into {@link DefinedTermBase} instances.
 * The instances can be identified either by UUID or by ID
 * The separator for concatenations is the colon: ','
 *
 * @author a.kohlbecker
 * @date Jun 25, 2013
 *
 */
public class TermBaseListPropertyEditor<T extends DefinedTermBase<?>> extends TermBasePropertyEditor<T> {

    public TermBaseListPropertyEditor(ITermService termService){
        super(termService);
    }

    @Override
    public void setAsText(String text) {

        String[] tokens = StringUtils.split(text, ',');
        if(tokens != null){
            ArrayList<T> termList = new ArrayList<T>(tokens.length);
            for(String token : tokens){
                termList.add(textToTerm(token));
            }
            setValue(termList);
        }
    }

}
