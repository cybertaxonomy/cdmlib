// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.biocase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Generates an XML query according to the BioCASe protocol.
 * @author pplitzner
 * @date 13.09.2013
 *
 */
public class BioCaseQueryGenerator {
/*
<?xml version='1.0' encoding='UTF-8'?>
<request xmlns='http://www.biocase.org/schemas/protocol/1.3'>
  <header><type>search</type></header>
  <search>
    <requestFormat>http://www.tdwg.org/schemas/abcd/2.06</requestFormat>
    <responseFormat start='0' limit='10'>http://www.tdwg.org/schemas/abcd/2.06</responseFormat>
      <filter>
        <like path='/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString'>A*</like>
      </filter>
      <count>false</count>
  </search>
</request>
*/

    private static final String FALSE = "false";
    private static final String TAXON_NAME_PATH_ABCD_2_0 = "/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString";
    private static final String PATH = "path";
    private static final String LIMIT = "limit";
    private static final String START = "start";
    private static final String ABCD_SCHEMA_2_0 = "http://www.tdwg.org/schemas/abcd/2.06";
    private static final String COUNT = "count";
    private static final String LIKE = "like";
    private static final String FILTER = "filter";
    private static final String RESPONSE_FORMAT = "responseFormat";
    private static final String REQUEST_FORMAT = "requestFormat";
    private static final String SEARCH = "search";
    private static final String TYPE = "type";
    private static final String HEADER = "header";
    private static final String REQUEST = "request";
    private static final String NAMESPACE = "http://www.biocase.org/schemas/protocol/1.3";
    private static final String LOCALITY_PATH_ABCD_2_0 = null;
    private static final String HERBARIUM_PATH_ABCD_2_0 = null;
    private static final String COUNTRY_PATH_ABCD_2_0 = null;
    private static final String COLLECTOR_NUMBER_PATH_ABCD_2_0 = null;
    private static final String COLLECTOR_PATH_ABCD_2_0 = null;
    private static final String ACCESSION_NUMBER_PATH_ABCD_2_0 = null;

    /**
     * Generates an XML query according to the BioCASe protocol.
     * @param query the {@link BioCaseQuery} to transform to XML
     * @return the query XML {@link Document} according BioCASe protocol
     */
    public Document generateXMLQuery(BioCaseQuery query){
        Document document = new Document();
        Element elRequest = new Element(REQUEST, Namespace.getNamespace(NAMESPACE));
        Element elHeader = new Element(HEADER);
        Element elType = new Element(TYPE);
        Element elSearch = new Element(SEARCH);
        Element elRequestFormat = new Element(REQUEST_FORMAT);
        Element elResponseFormat = new Element(RESPONSE_FORMAT);
        Element elFilter = new Element(FILTER);
        Element elCount = new Element(COUNT);

        document.setRootElement(elRequest);
        elRequest.addContent(elHeader);
        elHeader.addContent(elType);
        elType.addContent(SEARCH);

        elRequest.addContent(elSearch);
        elSearch.addContent(elRequestFormat);
        elRequestFormat.addContent(ABCD_SCHEMA_2_0);

        elSearch.addContent(elResponseFormat);
        elResponseFormat.setAttribute(START, "0");
        elResponseFormat.setAttribute(LIMIT, "10");
        elResponseFormat.addContent(ABCD_SCHEMA_2_0);

        elSearch.addContent(elFilter);

        if(query.accessionNumber!=null && !query.accessionNumber.trim().isEmpty()){
            addAccessionNumberFilter(elFilter, query.accessionNumber);
        }
        if(query.collector!=null && !query.collector.trim().isEmpty()){
            addCollectorFilter(elFilter, query.collector);
        }
        if(query.collectorsNumber!=null && !query.collectorsNumber.trim().isEmpty()){
            addCollectorsNumberFilter(elFilter, query.collectorsNumber);
        }
        if(query.country!=null && !query.country.trim().isEmpty()){
            addCountryFilter(elFilter, query.country);
        }
        //TODO: implement
//        if(query.date!=null){
//            addDateFilter(elFilter, query.date);
//        }
        if(query.herbarium!=null && !query.herbarium.trim().isEmpty()){
            addHerbariumFilter(elFilter, query.herbarium);
        }
        if(query.locality!=null && !query.locality.trim().isEmpty()){
            addLocalityFilter(elFilter, query.locality);
        }
        if(query.taxonName!=null && !query.taxonName.trim().isEmpty()){
            addTaxonNameFilter(elFilter, query.taxonName);
        }

        elSearch.addContent(elCount);
        elCount.addContent(FALSE);

        return document;
    }

    private void addLocalityFilter(Element elFilter, String locality) {
        addFilter(elFilter, locality, LOCALITY_PATH_ABCD_2_0);
    }

    private void addHerbariumFilter(Element elFilter, String herbarium) {
        addFilter(elFilter, herbarium, HERBARIUM_PATH_ABCD_2_0);
    }

    private void addCountryFilter(Element elFilter, String country) {
        addFilter(elFilter, country, COUNTRY_PATH_ABCD_2_0);
    }

    private void addCollectorsNumberFilter(Element elFilter, String collectorsNumber) {
        addFilter(elFilter, collectorsNumber, COLLECTOR_NUMBER_PATH_ABCD_2_0);
    }

    private void addCollectorFilter(Element elFilter, String collector) {
        addFilter(elFilter, collector, COLLECTOR_PATH_ABCD_2_0);
    }

    private void addAccessionNumberFilter(Element elFilter, String accessionNumber) {
        addFilter(elFilter, accessionNumber, ACCESSION_NUMBER_PATH_ABCD_2_0);
    }

    private void addTaxonNameFilter(Element filterElement, String taxonName){
        addFilter(filterElement, taxonName, TAXON_NAME_PATH_ABCD_2_0);
    }

    private void addFilter(Element filterElement, String taxonName, String path){
        Element elLike = new Element(LIKE);
        filterElement.addContent(elLike);
        elLike.setAttribute(PATH, path);
        elLike.addContent(taxonName);
    }
}
