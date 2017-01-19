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

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public class RankPropertyEditor extends PropertyEditorSupport {
	
	public void setAsText(String name) {
		try {
			setValue(Rank.getRankByName(name));
		} catch (UnknownCdmTypeException e) {
			throw new IllegalArgumentException("Unknown Rank "+name);
		}
	}
	
	public String setAsText() {		
		return ((Rank)getValue()).getLabel(); 
	}

}
