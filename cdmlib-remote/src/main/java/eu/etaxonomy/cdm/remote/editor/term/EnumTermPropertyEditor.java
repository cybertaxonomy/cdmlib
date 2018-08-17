/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.editor.term;

import java.beans.PropertyEditorSupport;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;

/**
 * This PropertyEditor translates a single string into a {@link eu.etaxonomy.cdm.model.common.IEnumTerm} instance.
 * The instance can be identified by the value of the
 * {@link eu.etaxonomy.cdm.model.common.IKeyTerm#getKey() key} property.
 *
 * @author a.kohlbecker
 * @since Jun 25, 2013
 *
 */
public class EnumTermPropertyEditor<T extends IEnumTerm<T>> extends PropertyEditorSupport {

    protected final EnumeratedTermVoc<T> delegateVoc;

    public EnumTermPropertyEditor(EnumeratedTermVoc<T> delegateVoc){
        super();
        this.delegateVoc = delegateVoc;
    }

    @Override
    public void setAsText(String text) {
        setValue(textToTerm(text));
    }

    /**
     * @param text
     * @return
     */
    protected T textToTerm(String text) {
        T term = delegateVoc.getByKey(text);
        if(term == null){
            throw new java.lang.IllegalArgumentException("No EnumTerm instance in " + delegateVoc.toString() + " found for the key: " + text);
        }
        return term;
    }
}
