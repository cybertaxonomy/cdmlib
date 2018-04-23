/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.editor;

import java.beans.PropertyEditorSupport;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.ITermService;

/**
 * @author a.kohlbecker
 * @since 30.06.2009
 * @deprecated use TermBasePropertyEditor instead
 */
@Deprecated
public class NamedAreaPropertyEditor extends PropertyEditorSupport  {

    @Autowired
    private ITermService termService;

    @Override
    public void setAsText(String text) {
            setValue(termService.load(UUID.fromString(text)));
    }
}
