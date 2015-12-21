// $Id$
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
 * The format of the string can be configured by using the {@link FormatKey} enum.
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
        AMPLIFICATION_LABEL,
    }

    public String format(Object object, FormatKey... formatKeys);

}
