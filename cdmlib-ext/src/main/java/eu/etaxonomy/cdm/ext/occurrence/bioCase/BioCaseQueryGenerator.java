/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.bioCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

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


<?xml version='1.0' encoding='UTF-8'?>
<request xmlns='http://www.biocase.org/schemas/protocol/1.3'>
  <header><type>search</type></header>
  <search>
    <requestFormat>http://www.tdwg.org/schemas/abcd/2.06</requestFormat>
    <responseFormat start='0' limit='10'>http://www.tdwg.org/schemas/abcd/2.06</responseFormat>
      <filter>
<equals path='/DataSets/DataSet/Units/Unit/UnitID'>B -W 11422 -01 0</equals>
      </filter>
      <count>false</count>
  </search>
</request>

<?xml version='1.0' encoding='UTF-8'?>
<request xmlns='http://www.biocase.org/schemas/protocol/1.3'>
  <header><type>search</type></header>
  <search>
    <requestFormat>http://www.tdwg.org/schemas/abcd/2.1</requestFormat>
    <responseFormat start='0' limit='10'>http://www.tdwg.org/schemas/abcd/2.1</responseFormat>
      <filter>
<like path='/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString'>A*</like>
      </filter>
      <count>false</count>
  </search>
</request>
*/

    private static final String ABCD_SCHEMA_2_0 = "http://www.tdwg.org/schemas/abcd/2.06";
    private static final String ABCD_SCHEMA_2_1 = "http://www.tdwg.org/schemas/abcd/2.1";
    private static final String FALSE = "false";
    private static final String PATH = "path";
    private static final String LIMIT = "limit";
    private static final String START = "start";
    private static final String COUNT = "count";
    private static final String LIKE = "like";
    private static final String EQUALS = "equals";
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String FILTER = "filter";
    private static final String RESPONSE_FORMAT = "responseFormat";
    private static final String REQUEST_FORMAT = "requestFormat";
    private static final String SEARCH = "search";
    private static final String TYPE = "type";
    private static final String HEADER = "header";
    private static final String REQUEST = "request";
    private static final String NAMESPACE = "http://www.biocase.org/schemas/protocol/1.3";
    private static final String UNIT_PATH = "/DataSets/DataSet/Units/Unit";
    private static final String UNIT_ID_PATH_ABCD_2_0 = UNIT_PATH + "/UnitID";
    private static final String TAXON_NAME_PATH_ABCD_2_0 = UNIT_PATH + "/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString";
    private static final String LOCALITY_PATH_ABCD_2_0 = UNIT_PATH + "/Gathering/LocalityText";
    private static final String HERBARIUM_PATH_ABCD_2_0 = UNIT_PATH + "/SourceID";
    private static final String COUNTRY_PATH_ABCD_2_0 = UNIT_PATH + "/Gathering/Country/Name";
    private static final String COLLECTOR_NUMBER_PATH_ABCD_2_0 = UNIT_PATH + "/CollectorsFieldNumber";
    private static final String COLLECTOR_PATH_ABCD_2_0 = UNIT_PATH + "/Gathering/Agents/GatheringAgentsText";
    private static final String ACCESSION_NUMBER_PATH_ABCD_2_0 = UNIT_PATH + "/SpecimenUnit/Accessions/AccessionNumber";
    private static final String CAT_PATH_ABCD_2_0 = UNIT_PATH + "/CAT";
    private static final String ASSOCIATION_UNIT_ID_ABCD_2_0 = UNIT_PATH +"/Associations/UnitAssociation/AssociatedUnitID";
    private static final String ASSOCIATION_UNIT_ID_ABCD_2_1 = UNIT_PATH +"/Associations/UnitAssociation/UnitID";

    /**
     * Generates an XML query according to the BioCASe protocol.
     * @param query the {@link OccurenceQuery} to transform to XML
     * @param abcdSchema The abcdSchema with which you want to query
     * @return the query XML {@link Document} according to the BioCASe protocol
     */
    public static Document generateXMLQuery(OccurenceQuery query, String abcdSchema){
        if(abcdSchema==null){
            abcdSchema = ABCD_SCHEMA_2_0;
        }
        Document document = new Document();
        Element elRequest = new Element(REQUEST, Namespace.getNamespace(NAMESPACE));
        Element elHeader = new Element(HEADER);
        Element elType = new Element(TYPE);
        Element elSearch = new Element(SEARCH);
        Element elRequestFormat = new Element(REQUEST_FORMAT);
        Element elResponseFormat = new Element(RESPONSE_FORMAT);
        Element elFilter = new Element(FILTER);
        Element elAnd = new Element(AND);

        Element elCount = new Element(COUNT);

        document.setRootElement(elRequest);
        elRequest.addContent(elHeader);
        elHeader.addContent(elType);
        elType.addContent(SEARCH);

        elRequest.addContent(elSearch);
        elSearch.addContent(elRequestFormat);
        elRequestFormat.addContent(abcdSchema);

        elSearch.addContent(elResponseFormat);
        elResponseFormat.setAttribute(START, "0");
        elResponseFormat.setAttribute(LIMIT, "100");
        elResponseFormat.addContent(abcdSchema);

        elSearch.addContent(elFilter);


        if(query.tripleIds!=null ){
            if (query.tripleIds.size() == 1){
                String unitId[] = query.tripleIds.iterator().next();
                Element elOr = new Element(OR);
                addEqualsFilter(elOr, unitId[0], ACCESSION_NUMBER_PATH_ABCD_2_0);
                addEqualsFilter(elOr, unitId[0], CAT_PATH_ABCD_2_0);
                addEqualsFilter(elOr, unitId[0], UNIT_ID_PATH_ABCD_2_0);
                elFilter.addContent(elOr);
                //addEqualsFilter(elAnd, unitId[0], UNIT_ID_PATH_ABCD_2_0);
            }else{
                Element elOr = new Element(OR);
                for (String[] unitId: query.tripleIds){
                    Element elOrAccessionNumber = new Element(OR);
                    addEqualsFilter(elOrAccessionNumber, unitId[0], ACCESSION_NUMBER_PATH_ABCD_2_0);
                    addEqualsFilter(elOrAccessionNumber, unitId[0], CAT_PATH_ABCD_2_0);
                    addEqualsFilter(elOrAccessionNumber, unitId[0], UNIT_ID_PATH_ABCD_2_0);
                    elOr.addContent(elOrAccessionNumber);
                   //addEqualsFilter(elOr, unitId[0], UNIT_ID_PATH_ABCD_2_0);
                }
                elFilter.addContent(elOr);
            }
        }else{
            elFilter.addContent(elAnd);

            if(query.accessionNumber!=null && !query.accessionNumber.trim().isEmpty()){
                Element elOr = new Element(OR);
                addLikeFilter(elOr, query.accessionNumber, ACCESSION_NUMBER_PATH_ABCD_2_0);
                addLikeFilter(elOr, query.accessionNumber, CAT_PATH_ABCD_2_0);
                addLikeFilter(elOr, query.accessionNumber, UNIT_ID_PATH_ABCD_2_0);
                elAnd.addContent(elOr);
            }
            if(query.collector!=null && !query.collector.trim().isEmpty()){
                addLikeFilter(elAnd, query.collector, COLLECTOR_PATH_ABCD_2_0);
            }
            if(query.collectorsNumber!=null && !query.collectorsNumber.trim().isEmpty()){
                addLikeFilter(elAnd, query.collectorsNumber, COLLECTOR_NUMBER_PATH_ABCD_2_0);
            }
            if(query.country!=null && !query.country.trim().isEmpty()){
                addLikeFilter(elAnd, query.country, COUNTRY_PATH_ABCD_2_0);
            }
            //TODO: implement
    //        if(query.date!=null){
    //            addFilter(elFilter, query.date);
    //        }
            if(query.herbarium!=null && !query.herbarium.trim().isEmpty()){
                addLikeFilter(elAnd, query.herbarium, HERBARIUM_PATH_ABCD_2_0);
            }
            if(query.locality!=null && !query.locality.trim().isEmpty()){
                addLikeFilter(elAnd, query.locality, LOCALITY_PATH_ABCD_2_0);
            }
            if(query.taxonName!=null && !query.taxonName.trim().isEmpty()){
                addLikeFilter(elAnd, query.taxonName, TAXON_NAME_PATH_ABCD_2_0);
            }
        }
        elSearch.addContent(elCount);
        elCount.addContent(FALSE);

        return document;
    }

    /**
     * Generates an XML query according to the BioCASe protocol.
     * @param query the {@link OccurenceQuery} to transform to XML
     * @param abcdSchema The abcdSchema with which you want to query
     * @return the query XML {@link Document} according to the BioCASe protocol
     */
    public static Document generateXMLQueryForSiblings(OccurenceQuery query, String abcdSchema){
        if(abcdSchema==null){
            abcdSchema = ABCD_SCHEMA_2_0;
        }
        Document document = new Document();
        Element elRequest = new Element(REQUEST, Namespace.getNamespace(NAMESPACE));
        Element elHeader = new Element(HEADER);
        Element elType = new Element(TYPE);
        Element elSearch = new Element(SEARCH);
        Element elRequestFormat = new Element(REQUEST_FORMAT);
        Element elResponseFormat = new Element(RESPONSE_FORMAT);
        Element elFilter = new Element(FILTER);
        Element elAnd = new Element(AND);

        Element elCount = new Element(COUNT);

        document.setRootElement(elRequest);
        elRequest.addContent(elHeader);
        elHeader.addContent(elType);
        elType.addContent(SEARCH);

        elRequest.addContent(elSearch);
        elSearch.addContent(elRequestFormat);
        elRequestFormat.addContent(abcdSchema);

        elSearch.addContent(elResponseFormat);
        elResponseFormat.setAttribute(START, "0");
        elResponseFormat.setAttribute(LIMIT, "100");
        elResponseFormat.addContent(abcdSchema);

        elSearch.addContent(elFilter);
        elFilter.addContent(elAnd);


        if(query.accessionNumber!=null && !query.accessionNumber.trim().isEmpty()){
            if (abcdSchema.equals(ABCD_SCHEMA_2_1)){
                addLikeFilter(elAnd, query.accessionNumber, ASSOCIATION_UNIT_ID_ABCD_2_1);
            }else{
                addLikeFilter(elAnd, query.accessionNumber, ASSOCIATION_UNIT_ID_ABCD_2_0);
            }
        }

        elSearch.addContent(elCount);
        elCount.addContent(FALSE);
        System.out.println(document.toString());
        return document;
    }

    private static void addLikeFilter(Element filterElement, String taxonName, String path){
        Element elLike = new Element(LIKE);
        filterElement.addContent(elLike);
        elLike.setAttribute(PATH, path);
        elLike.addContent(taxonName);
    }

    private static void addEqualsFilter(Element filterElement, String taxonName, String path){
        Element elEquals = new Element(EQUALS);
        filterElement.addContent(elEquals);
        elEquals.setAttribute(PATH, path);
        elEquals.addContent(taxonName);
    }
}
