/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.editor.term;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;

/**
 * This PropertyEditor translates concatenated lists string identifiers into
 * {@link eu.etaxonomy.cdm.model.common.IEnumTerm} instances. The instances can be identified by the value of the
 * {@link eu.etaxonomy.cdm.model.common.IKeyTerm#getKey() key} property. The separator for concatenations is the colon: ','
 *
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 */
public class EnumTermListPropertyEditor<T extends IEnumTerm<T>> extends EnumTermPropertyEditor<T> {

    public EnumTermListPropertyEditor(EnumeratedTermVoc<T> delegateVoc){
        super(delegateVoc);
    }

    @Override
    public void setAsText(String text) {

        List<T> termList = null;

        String[] tokens = StringUtils.split(text, ',');
        if(tokens != null && tokens.length > 0){
            termList = new ArrayList<>(tokens.length);
            for(String key : tokens){
                termList.add(super.textToTerm(key));
            }
        }
        setValue(termList);
    }

}
