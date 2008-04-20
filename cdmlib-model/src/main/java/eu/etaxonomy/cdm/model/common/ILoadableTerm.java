package eu.etaxonomy.cdm.model.common;

import java.util.List;

import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import au.com.bytecode.opencsv.CSVWriter;

public interface ILoadableTerm extends ICdmBase{

	/**
	 * Fills the {@link ILoadableTerm} with contents from a csvLine. If the csvLine represents the default language
	 * the csvLine attributes are merged into the existing default language and the default Language is returned.
	 * @param csvLine
	 * @return
	 */
	public abstract ILoadableTerm readCsvLine(List<String> csvLine);

	public abstract void writeCsvLine(CSVWriter writer);

	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract TermVocabulary getVocabulary();

	public abstract void setVocabulary(TermVocabulary newVocabulary);

}