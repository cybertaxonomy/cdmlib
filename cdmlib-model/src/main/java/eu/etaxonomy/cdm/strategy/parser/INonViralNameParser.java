package eu.etaxonomy.cdm.strategy.parser;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.IStrategy;


/**
 * Supplies several parser methods for non viral taxon name strings and for nomenclatural
 * reference strings. If the parser method for taxon names is not successful
 * the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getHasProblem() hasProblem} flag
 * of the {@link eu.etaxonomy.cdm.model.name.TaxonNameBase TaxonNameBase} instance will be set. 
 * Some of the parser methods for taxon names create also a TaxonNameBase
 * instance while other ones only fill the result into an existing
 * TaxonNameBase instance.
 * 
 * @author a.mueller
 *
 */
public interface INonViralNameParser<T extends TaxonNameBase> extends IStrategy {
	
	/**
	 * Parses the taxon name String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * 
	 * @param simpleName the scientific name string without authorship, year, reference etc.
	 * @param rank
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials  
	 */
	public T parseSimpleName(String simpleName, Rank rank);

	/**
	 * Parses the taxon name String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * 
	 * @param simpleName the scientific name string without authorship, year, reference etc.
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseSimpleName(String simpleName);
	
	

	/**
	 * Parses the taxon name String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 * 
	 * @param fullName the string containing the scientific name with authorship but without year, reference etc.
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials. 
	 */
	public T parseFullName(String fullName);

	/**
	 * Parses the taxon name String and returns a TaxonNameBase. 
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
 	 * 
	 * @param fullName the string containing the scientific name with authorship but without year, reference etc.
	 * @param rank
	 * @return TaxonNameBase name, with name.rank = rank for all Uninomials and name.rank = Rank.GENUS for rank = null  
	 */
	public T parseFullName(String fullName, NomenclaturalCode nomCode, Rank rank);

	/**
 	 * Parses the taxon name String and fills the result into the existing TaxonNameBase nameToBeFilled. 
	 * Name related fields are set to default (null for Strings and other objects like Authors and References and false for booleans).
	 * NameRelations are not changed.
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * No change is done to nameToBeFilled if fullName == null.
 	 * 
	 * @param fullName the string containing the scientific name with authorship but without year, reference etc.
	 * @param rank
	 * @param nameToBeFilled The TaxonNameBaseToBeFilled
	 */
	public void parseFullName(T nameToBeFilled, String fullName, Rank rank, boolean makeEmpty);

	/**
	 * @param fullReference the string containing the scientific name with authorship, year, reference etc.
	 * @return
	 */
	public T parseFullReference(String fullReference);
	
	/**
	 * @param fullReference the string containing the scientific name with authorship, year, reference etc.
	 * @param nomCode
	 * @param rank
	 * @return
	 */
	public T parseFullReference(String fullReference, NomenclaturalCode nomCode, Rank rank);

	/**
	 * @param nameToBeFilled
	 * @param fullReference the string containing the scientific name with authorship, year, reference etc.
	 * @param rank
	 * @param makeEmpty
	 */
	public void parseFullReference(T nameToBeFilled, String fullReference, Rank rank, boolean makeEmpty);
	
}
