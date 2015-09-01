/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * This PropertyEditor translates a single string identifiers into a
 * {@link DefinedTermBase} instance.
 * The instance can be identified either by UUID or by ID.
 *
 * @author a.kohlbecker
 * @date Jun 25, 2013
 *
 * @deprecated better avoid using this PropertyEditor since it will cause Hibernate to load the
 *             term the data base. Use plain uuids instead for better performance where possible!
 *
 */
@Deprecated
public class TermBasePropertyEditor<T extends DefinedTermBase<?>> extends PropertyEditorSupport {

    protected final ITermService termService;

    public TermBasePropertyEditor(ITermService termService){
        super();
        this.termService = termService;
    }

    @Override
    public void setAsText(String text) {
        setValue(textToTerm(text));
    }

    /**
     * @param text
     * @return
     */
    @SuppressWarnings("unchecked")
    protected T textToTerm(String text) {
        T term = null;
        // 1. try treating as UUID
        try {
            UUID uuid = UUID.fromString(text);
            term = (T) termService.load(uuid);
        } catch (Exception e1) {
         // 1. try treating as ID
            try {
                int id = Integer.parseInt(text);
                term = (T) termService.find(id);
            } catch (Exception e2) {
                /* IGNORE */
            }
        }

        if(term == null){
            throw new java.lang.IllegalArgumentException("No TermBase instance found for the supplied identifier " + text);
        }
        return term;
    }
}
