package eu.etaxonomy.cdm.strategy.parser;

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
	public T parseReferencedName(String fullReference);
	
	/**
	 * @see INonViralNameParser.parseFullReference(T nameToBeFilled, String fullReference, Rank rank, boolean makeEmpty)
	 * @param fullReference 
	 * @param nomCode
	 * @param rank
	 * @return
	 */
	public T parseReferencedName(String fullReference, NomenclaturalCode nomCode, Rank rank);

	/**
	 * Parses a String (fullReference) assuming that it represents a taxonomic name, it's reference,
	 * and it's nomenclatural status.<BR>
	 * (1) Status: First the last part of the string is checked if it represents a nomenclatural status (e.g. nom. inval.).
	 * If so, this part of the string is separated and the according status is added to the name.<BR>
	 * (2) <B>Name:</B> The starting part of the remaining string is checked if it represent a name according to the rules 
	 * of the underlying nomenclatural code.<BR> 
	 * (2a) Non-atomized Name: If the name can not be parsed the title cache as well as the full
	 * title cache of the name is filled and the hasProblem flag is set to <code>true</code>. The same applies
	 * if the name can be parsed but is followed by a not empty String that does not start with a 
	 * reference separator ("," of " in ").<BR>
	 * (2b) Atomized name: Otherwise the name part is separated and parsed. The according name attributes are set and 
	 * the name's protectedTitleCache flag is set to <code>false</code>. <BR>
	 * (3) Reference: From the remaining string the reference separator is separated.
	 * The remaining string is parsed for beeing a valid (according to the parsers rules) reference String.<BR>
	 * (3a) If the reference part could not be parsed, the references title cache is set by the remaining string and the 
	 * references protected title cache is set to <code>true</code>.
	 * (3b) If the reference could be parsed the reference is separated and parsed. The according reference attributes are
	 * set and the reference's protectedTitleCache flag as well as the hasProblem flag is set to <code>false</code>.
	 * Then, and only then, the name's hasProblem flag is set to <code>false</code>.  
	 * 
	 * @param nameToBeFilled
	 * @param fullReference the string containing the scientific name with authorship, year, reference etc.
	 * @param rank
	 * @param makeEmpty
	 */
	public void parseReferencedName(T nameToBeFilled, String fullReference, Rank rank, boolean makeEmpty);
	
}
