package eu.etaxonomy.cdm.strategy.parser;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.IStrategy;


/**
 * @author a.mueller
 *
 */
public interface INonViralNameParser<T extends TaxonNameBase> extends IStrategy {
	
	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @param rank
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials  
	 */
	public T parseSimpleName(String simpleName, Rank rank);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase without Author, Year, Reference etc.
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseSimpleName(String simpleName);
	
	

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials. 
	 */
	public T parseFullName(String fullName);

	/**
	 * Parses the taxonname String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @param rank
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseFullName(String fullName, NomenclaturalCode nomCode, Rank rank);

	/**
 	 * Parses the taxonname String and fills the result into the existing TaxonNameBase nameToBeFilled. 
	 * Name related fields are set to default (null for Strings and other objects like Authors and References and false for booleans).
	 * NameRelations are not changed.
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * No change is done to nameToBeFilled if fullName == null.
	 * @param fullName TaxonNameBase with Author, Year, Reference etc.,
	 * @param rank
	 * @param nameToBeFilled The TaxonNameBaseToBeFilled
	 */
	public void parseFullName(T nameToBeFilled, String fullName, Rank rank, boolean makeEmpty);

	/**
	 * @param fullReference
	 * @return
	 */
	public T parseFullReference(String fullReference);
	
	/**
	 * @param fullReference
	 * @param nomCode
	 * @param rank
	 * @return
	 */
	public T parseFullReference(String fullReference, NomenclaturalCode nomCode, Rank rank);

	/**
	 * @param nameToBeFilled
	 * @param fullReference
	 * @param rank
	 * @param makeEmpty
	 */
	public void parseFullReference(T nameToBeFilled, String fullReference, Rank rank, boolean makeEmpty);
	
}
