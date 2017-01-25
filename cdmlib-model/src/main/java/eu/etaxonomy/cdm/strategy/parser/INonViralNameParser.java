/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.parser;

import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.IStrategy;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;


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
public interface INonViralNameParser<T extends INonViralName> extends IStrategy {

	/**
	 * Parses the taxon name String and returns a TaxonNameBase.
	 * If the String is not parseable the "hasProblem" bit is set to true.
 	 * Returns null if fullName == null.
	 *
	 * @param simpleName the scientific name string without authorship, year, reference etc.
	 * @param rank
	 * @return TaxonNameBase, with rank = Rank.GENUS for all Uninomials
	 */
	public T parseSimpleName(String simpleName, NomenclaturalCode code, Rank rank);

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
	 * <ol>
	 * 		<li><strong>Status:</strong> First the last part of the string is checked if it represents a nomenclatural status (e.g. nom. inval.).
	 * If so, this part of the string is separated and the according status is added to the name.</li>
	 * 		<li><strong>Name:</strong> The starting part of the remaining string is checked if it represent a name according to the rules
	 * of the underlying nomenclatural code.</li>
	 * 		<li>
	 * 			<ol>
	 * 				<li>Non-atomized Name: If the name can not be parsed the title cache as well as the full
	 * title cache of the name is filled and the hasProblem flag is set to <code>true</code>. The same applies
	 * if the name can be parsed but is followed by a not empty String that does not start with a
	 * reference separator ("," of " in ").</li>
	 * 				<li>Atomized name: Otherwise the name part is separated and parsed. The according name attributes are set and
	 * the name's protectedTitleCache flag is set to <code>false</code>.</li>
	 * 			</ol>
	 *		</li>
	 * 		<li><strong>Reference:</strong> From the remaining string the reference separator is separated.
	 * The remaining string is parsed for beeing a valid (according to the parsers rules) reference String.
	 * 			<ol>
	 * 				<li>If the reference part could not be parsed, the references title cache is set by the remaining string and the
	 * references protected title cache is set to <code>true</code>.</li>
	 * 				<li>If the reference could be parsed the reference is separated and parsed. The according reference attributes are
	 * set and the reference's protectedTitleCache flag as well as the hasProblem flag is set to <code>false</code>.
	 * Then, and only then, the name's hasProblem flag is set to <code>false</code>.</li>
	 * 			</ol>
	 * 		</li>
	 * </ol>
	 *
	 * @param nameToBeFilled
	 * 				an existing name object
	 * @param fullReference
	 * 				the string containing the scientific name with authorship, year, reference etc.
	 * @param rank
	 * @param makeEmpty
	 * 				if this parameter is set to true, the name objects will nulled. All information
	 * 				formerly attached to this name will be lost.
	 */
	public void parseReferencedName(T nameToBeFilled, String fullReference, Rank rank, boolean makeEmpty);

	public void parseAuthors(INonViralName nonViralName, String authorString) throws StringNotParsableException;

}
