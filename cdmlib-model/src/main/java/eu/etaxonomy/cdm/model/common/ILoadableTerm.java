package eu.etaxonomy.cdm.model.common;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.bytecode.opencsv.CSVWriter;

public interface ILoadableTerm<T extends DefinedTermBase> {

	/**
	 * Fills the {@link ILoadableTerm term} with contents from a csvLine. If the csvLine represents the default language
	 * the csvLine attributes are merged into the existing default language and the default Language is returned.
	 * @param csvLine
	 * @return
	 */
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms);

	public  void writeCsvLine(CSVWriter writer, T term);
}