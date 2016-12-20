/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.bytecode.opencsv.CSVWriter;

public interface ILoadableTerm<T extends IDefinedTerm>{

	/**
	 * Fills the {@link ILoadableTerm term} with contents from a csvLine. If the csvLine represents the default language
	 * the csvLine attributes are merged into the existing default language and the default Language is returned.
	 * @param csvLine
	 * @return
	 */
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId);

	public  void writeCsvLine(CSVWriter writer, T term);
}
