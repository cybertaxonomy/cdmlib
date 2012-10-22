// $Id: NamedAreaPropertyEditor.java 8450 2010-03-19 15:12:17Z a.kohlbecker $
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

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.kohlbecker
 * @date 30.06.2009
 * TODO only TDWG areas supported for now
 */
@Component
public class NamedAreaPropertyEditor extends PropertyEditorSupport  {
	
	public void setAsText(String text) {
			setValue(TdwgArea.getAreaByTdwgAbbreviation(text));  	
	}
}
