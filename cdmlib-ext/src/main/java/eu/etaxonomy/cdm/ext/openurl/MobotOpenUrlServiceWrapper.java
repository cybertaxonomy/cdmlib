/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.openurl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import eu.etaxonomy.cdm.ext.common.SchemaAdapterBase;
import eu.etaxonomy.cdm.ext.common.ServiceWrapperBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Generic ServiceWrapper for OpenUrl 1.0 services, initially implemented to be
 * used with the BHL OpenUrl resolver
 * (http://www.biodiversitylibrary.org/openurl) but might also work with other
 * resolvers which meet the Z39.88-2004 (=OpenURL 1.0) specification
 * <p>
 * For references see:
 * <ul>
 * <li>BHL OpenUrl resolver reference:
 * http://www.biodiversitylibrary.org/openurlhelp.aspx</li>
 * <li>ANSI/NISO Z39.88-2004 (=OpenURL 1.0) specification:
 * http://www.niso.org/kst/reports/standards?step=2&gid=&project_key=
 * d5320409c5160be4697dc046613f71b9a773cd9e</li>
 * </ul>
 *
 * @author a.kohlbecker
 * @since 24.08.2010
 *
 */
public class MobotOpenUrlServiceWrapper extends ServiceWrapperBase<OpenUrlReference> {

    private String urlVersion = "Z39.88-2004";

    public MobotOpenUrlServiceWrapper(){
        addSchemaAdapter(new MobotOpenUrlResponseSchemaAdapter());
    }

    /**
     * BHL uses the response format as specified in the
     * http://code.google.com/p/
     * bhl-bits/source/browse/trunk/portal/OpenUrlUtilities
     * /OpenUrlResponse.cs?r=17 there seems to be no xml schema available
     * though.
     * @param query the MobotOpenUrlQuery object
     * @return
     */
    public List<OpenUrlReference> doResolve(MobotOpenUrlQuery query) {

        List<NameValuePair> pairs = new ArrayList<>();

        // find the appropriate schemadapter using the schemaShortName
        if(query.schemaShortName == null){
            query.schemaShortName = "MOBOT.OpenUrl.Utilities.OpenUrlResponse";
        }
        SchemaAdapterBase<OpenUrlReference> schemaAdapter = schemaAdapterMap.get(query.schemaShortName);
        if (schemaAdapter == null) {
            logger.error("No SchemaAdapter found for " + query.schemaShortName);
        }

        addNameValuePairTo(pairs, "format", "xml");
        addNameValuePairTo(pairs, "url_ver", urlVersion);
        /* info:ofi/fmt:kev:mtx:book or info:ofi/fmt:kev:mtx:journal */
        addNameValuePairTo(pairs, "rft_val_fmt", "info:ofi/fmt:kev:mtx:" + query.refType);
        /* Book title */
        addNameValuePairTo(pairs, "rft.btitle", query.bookTitle);
        /* Journal title */
        addNameValuePairTo(pairs, "rft.jtitle", query.journalTitle);
        /* Author name ("last, first" or "corporation") */
        addNameValuePairTo(pairs, "rft.au", query.authorName);
        /* Author last name */
        addNameValuePairTo(pairs, "rft.aulast", query.authorLastName);
        /* Author first name */
        addNameValuePairTo(pairs, "rft.aufirst", query.authorFirstName);
        /* Author name (corporation) */
        addNameValuePairTo(pairs, "rft.aucorp", query.authorNameCorporation);
        /* Publication details */
        addNameValuePairTo(pairs, "rft.publisher", query.publicationDetails);
        /* Publisher name */
        addNameValuePairTo(pairs, "rft.pub", query.publisherName);
        /* Publication place */
        addNameValuePairTo(pairs, "rft.place", query.publicationPlace);
        /* Publication date (YYYY or YYYY-MM or YYYY-MM-DD) */
        addNameValuePairTo(pairs, "rft.date", query.publicationDate);
        /* ISSN */
        addNameValuePairTo(pairs, "rft.issn", query.ISSN);
        /* ISBN */
        addNameValuePairTo(pairs, "rft.isbn", query.ISBN);
        /* CODEN */
        addNameValuePairTo(pairs, "rft.coden", query.CODEN);
        /* Abbreviation = abbreviated Title */
        addNameValuePairTo(pairs, "rft.stitle", query.abbreviation);
        /* Volume */
        addNameValuePairTo(pairs, "rft.volume", query.volume);
        /* Issue */
        addNameValuePairTo(pairs, "rft.issue", query.issue);
        /* Start page */
        if(query.startPage != null){
            Integer page = parsePageNumber(query.startPage);
            addNameValuePairTo(pairs, "rft.spage", page.toString());
        }
        /* BHL title ID (where XXXX is the ID value)*/
        addNameValuePairTo(pairs, "rft_id" , query.bhlTitleURI);
        /* BHL page ID (where XXXX is the ID value)*/
        addNameValuePairTo(pairs, "rft_id", query.bhlPageURI);

        /* OCLC number (where XXXX is the ID value)*/
        if(query.oclcNumber != null){
            pairs.add(new BasicNameValuePair("rft_id", "info:oclcnum/" +query.oclcNumber));
        }
        /* Lib. of Congress ID (where XXXX is the ID value)*/
        if(query.libofCongressID != null){
            pairs.add(new BasicNameValuePair("rft_id", "info:lccn/" +query.libofCongressID));
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Charset", "UTF-8");

        try {
            URI requestUri = createUri(null, pairs);

            InputStream stream = executeHttpGet(requestUri, requestHeaders);
//			String search = "utf-16";
//			String replace = "UTF-8";
////			stream = StreamUtils.streamReplace(stream, search, replace);
            // fix the "org.xml.sax.SAXParseException: An invalid XML character (Unicode: 0x1) was found" problem
//			stream = StreamUtils.streamReplaceAll(stream, "[\\x00-\\x10]", " ");

            List<OpenUrlReference> referenceList = schemaAdapter.getCmdEntities(stream);
            // TODO : we need to set ReferenceType here unless we know that the field Genre returns the reference type
            for(OpenUrlReference ref : referenceList){
                ref.setReferenceType(query.refType);
            }
            return referenceList;

        } catch (IOException e) {
            // thrown by doHttpGet
            logger.error(e);
        } catch (URISyntaxException e) {
            // thrown by createUri
            logger.error(e);
        }

        return null;

    }

    private Integer parsePageNumber(String startPage) {
        String pageNumbers = startPage.replaceAll("(?i)page|pages|p|p\\.|pp\\.|pp", "");
        String[] pageNumbersTokens = pageNumbers.split("[,-]", 1);
        Integer page = null;
        try {
            if(pageNumbersTokens[0] != null){
                pageNumbersTokens[0] = pageNumbersTokens[0].trim();
            } else {
                throw new NumberFormatException();
            }
            page = Integer.valueOf(pageNumbersTokens[0]);
        } catch (NumberFormatException e) {
            logger.warn("First page number token of " + startPage + " is not a Number", e);
            throw e;
        }
        return page;
    }


    /**
     * @param reference
     *            the OpenUrlReference instance as a starting point for paging.
     * @param forward
     *            integer indicating the number of pages to page forward. An
     *            negative integer will page backwards
     * @return
     * @throws IllegalArgumentException
     *             if the requested page number is not existent or if the field
     *             or if OpenUrlReference.pages is not parsable
     */
    public List<OpenUrlReference> doPage(OpenUrlReference reference, int forward) throws IllegalArgumentException{

        Integer pageNumber = null;
        try{
            if(reference.getPages() != null){
                pageNumber = parsePageNumber(reference.getPages());
            }
        }catch(NumberFormatException e){
            String errorMessage = "Reference has no page number or the field 'pages' is not parsable";
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        MobotOpenUrlQuery query = new MobotOpenUrlQuery();
        query.bhlTitleURI = reference.getTitleUri();
        pageNumber += forward;
        query.startPage = pageNumber.toString();
        query.refType = reference.getReferenceType();
        return doResolve(query);
    }

    public enum ReferenceType{
        book, journal;

        public static ReferenceType getReferenceType(Reference reference){
            if(eu.etaxonomy.cdm.model.reference.ReferenceType.Book.equals(reference.getType())){
                return book;
            }
            else if(eu.etaxonomy.cdm.model.reference.ReferenceType.Journal.equals(reference.getType())){
                return journal;
            }
            else {
                return null;
            }
        }
    }

}
