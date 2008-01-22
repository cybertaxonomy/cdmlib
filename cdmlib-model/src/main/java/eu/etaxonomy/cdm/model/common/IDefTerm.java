package eu.etaxonomy.cdm.model.common;

import java.util.List;

import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import au.com.bytecode.opencsv.CSVWriter;

public interface IDefTerm {

	public abstract void readCsvLine(List<String> csvLine);

	public abstract void writeCsvLine(CSVWriter writer);

	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public abstract TermVocabulary getVocabulary();

	public abstract void setVocabulary(TermVocabulary newVocabulary);

}