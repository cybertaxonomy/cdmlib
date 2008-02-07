package eu.etaxonomy.cdm.strategy;

import java.util.regex.Pattern;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;


/**
 * @author a.mueller
 *
 */
public interface ITaxonNameParser<T extends TaxonNameBase> extends IStrategy {
	

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials.
	 */
	public T parseFullName(String fullName);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseFullName(String fullName, Rank rank);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials  
	 */
	public T parseSimpleName(String simpleName, Rank rank);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseSimpleName(String simpleName);
	
	
	
	
}
