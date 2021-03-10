/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache;

import eu.etaxonomy.cdm.model.name.TaxonName;

/**
 * Tag types for atomised taxon name strings and atomised reference citation strings.
 * Used by {@link TaggedText}.
 *
 * @author a.kohlbecker
 * @since 13.12.2007
 *
 */
public enum TagEnum {

	/**
	 *  A taxon name including genus name, epithet etc.
	 */
	name,
	/**
	 *  A name rank abbreviation, e.g. subsp.
	 */
	rank,
	/**
	 * Non-atomised addition to a taxon name or a taxon not ruled by a nomenclatural code
	 */
	appendedPhrase,
	/**
	 * The authors of a reference, also used in taxon names
	 */
	authors,
	/**
	 * a reference
	 */
	reference,
	/**
	 * Volume, page number etc. of a reference
	 */
	microreference,
	/**
	 * Publication year of a reference
	 */
	year,
	/**
	 *  A full taxon name including all name information and nomenclatural
	 *  reference information
	 */
	fullName,
	/**
	 *  The nomenclatural status of a name
	 */
	nomStatus,
	/**
	 *  A separator to separate two tags () .
	 *  A separator should include all needed whitespaces. So adding of whitespace
	 *  is not needed if a separator is given.
	 */
	separator,
	/**
     *  A separator that needs to be added to the previous text if and only if
     *  the previous tag is used (not filtered) and if it is not the last tag
     *  in the list.<BR>
     *  A post-separator should include all needed whitespaces. So adding of whitespace
     *  is not needed if a post-separator is given.
     */
    postSeparator,
	/**
	 *  The hybrid sign.
	 */
	hybridSign,
    /**
     * a secundum reference (for TaxonBase)
     */
    secReference,
    /**
     * a secundum micro reference (for TaxonBase)
     */
    secMicroReference,
//    /**
//     * a secundum reference as used in a misapplied name (for TaxonRelationship)
//     */
//    sensuReference,
//    /**
//     * a secundum micro reference as used in a misapplied name (for TaxonRelationship)
//     */
//    sensuMicroReference,
    /**
     * a reference as used in a relation (e.g. TaxonRelationship reference)
     */
    relSecReference,
    /**
     * a micro reference as used in a relation (e.g. TaxonRelationship micro reference)
     */
    relSecMicroReference,
    /**
     * Any type of label
     */
    label,
    specimenOrObservation,
    typeDesignation,
    /**
     * Any symbol representing information, used e.g. for representing relationships
     * like taxon relationships.
     */
    symbol,
	;


	public boolean isName(){
		return this == name;
	}
	public boolean isRank(){
		return this == rank;
	}
	public boolean isAuthors(){
		return this == authors;
	}
	public boolean isAppendedPhrase(){
		return this == appendedPhrase;
	}
	public boolean isReference(){
		return this == reference;
	}
	public boolean isYear(){
		return this == year;
	}
	public boolean isFullName(){
		return this == fullName;
	}

	public boolean isNomStatus(){
		return this == nomStatus;
	}
	/**
	 * @return <code>true</code> if type is separator or postSeparator
	 */
	public boolean isSeparator(){
		return this == separator || this == postSeparator;
	}
	public boolean isHybridSign(){
		return this == hybridSign;
	}

	/**
	 * @return <code>true</code> if the tag is usually used in the pure name part
	 * of a {@link TaxonName}. This does not include author information. It is still
	 * unclear if {@link #isAppendedPhrase()} should be part. Currently it is.
	 * {@link #isSeparator()} is also unclear, currently it is not.
	 */
	public boolean isNameCachePart() {
        return isName()||isRank()||isHybridSign()||isAppendedPhrase();
    }

}
