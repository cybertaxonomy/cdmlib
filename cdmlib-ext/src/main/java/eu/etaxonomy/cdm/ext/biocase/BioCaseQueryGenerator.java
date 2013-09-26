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
    private static final String taxonNamePath_ABCD_2_0 = "/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString";
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

    public Document generateXMLQuery(BioCaseQuery query){
        Document document = new Document();
        Element elRequest = new Element(REQUEST, Namespace.getNamespace(NAMESPACE));
        Element elHeader = new Element(HEADER);
        Element elType = new Element(TYPE);
        Element elSearch = new Element(SEARCH);
        Element elRequestFormat = new Element(REQUEST_FORMAT);
        Element elResponseFormat = new Element(RESPONSE_FORMAT);
        Element elFilter = new Element(FILTER);
        Element elLike = new Element(LIKE);
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
        elFilter.addContent(elLike);
        elLike.setAttribute(PATH, taxonNamePath_ABCD_2_0);
        elLike.addContent(query.taxonName);

        elSearch.addContent(elCount);
        elCount.addContent(FALSE);

        return document;
    }
}
