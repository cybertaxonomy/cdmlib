/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

/**
 * Implementing classes provide a string representation for a given object.
 * How the the string is built can be configured
 * by using the {@link FormatKey} enum.<br>
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public interface ICdmFormatter {

    public static enum FormatKey{
        /*basics*/
        COMMA,
        OPEN_BRACKET,
        CLOSE_BRACKET,
        SPACE,
        /*identifiable entity*/
        SAMPLE_DESIGNATION,
        /*specimenOrObservationBase*/
        RECORD_BASIS,
        KIND_OF_UNIT,
        /*field unit*/
        FIELD_NUMBER,
        /*gathering event*/
        GATHERING_COUNTRY,
        GATHERING_LOCALITY_TEXT,
        GATHERING_DATE,
        GATHERING_COLLECTOR,
        /*derived unit*/
        COLLECTION_CODE,
        COLLECTION_NAME,
        MOST_SIGNIFICANT_IDENTIFIER,
        ACCESSION_NUMBER,
        BARCODE,
        CATALOG_NUMBER,
        /*media specimen*/
        MEDIA_TITLE,
        MEDIA_ARTIST,
        /*sequence*/
        SEQUENCE_DNA_MARKER,
        /*single read*/
        SINGLE_READ_PRIMER,
        SINGLE_READ_PHEROGRAM_TITLE_CACHE,
        AMPLIFICATION_LABEL,
    }

    /**
     * Returns a string representation of the given object.<br>
	 * The is built according to the formatKeys passed as arguments.<br>
	 * E.g.
	 * <code>
	 * format(derivedUnit, GATHERING_COUNTRY, COMMA, GATHERING_COLLECTOR, COMMA, OPEN_BRACKET, COLLECTION_CODE, CLOSE_BRACKET
	 * </code> will result in something like <i>Peru, L. (B)</i>
	 *
	 * @param object the object which should be formatted as a string representation
	 * @param formatKeys a list of enum values specifying the parts of which the string consists
	 * @return a string representation of the given object according to the chosen enum values
	 */
    public String format(Object object, FormatKey... formatKeys);

    /**
     * Returns a string representation of the given object.<br>
     * <b>Note:</b> Only use this method if the formatKeys for this
     * ICdmFormatter have been set before. Otherwise the string might be empty.
     * @param object the object which should be formatted as a string representation
     * @return a string representation of the given object
     */
    public String format(Object object);

}
