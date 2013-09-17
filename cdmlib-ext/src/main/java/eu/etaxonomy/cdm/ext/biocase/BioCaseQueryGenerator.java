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

    private Document query;

    public String generateStringQuery(){
        return "<?xml version='1.0' encoding='UTF-8'?><request xmlns='http://www.biocase.org/schemas/protocol/1.3'><header><type>search</type></header><search><requestFormat>http://www.tdwg.org/schemas/abcd/2.06</requestFormat><responseFormat start='0' limit='10'>http://www.tdwg.org/schemas/abcd/2.06</responseFormat><filter><like path='/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString'>A*</like></filter><count>false</count></search></request>";
    }

    //TODO BioCASE seems to NOT like double quotation marks (") for the arguments. They have to be single (')
    public Document generateQuery(){
        query = new Document();
        Element elRequest = new Element("request", Namespace.getNamespace("http://www.biocase.org/schemas/protocol/1.3"));
        Element elHeader = new Element("header");
        Element elType = new Element("type");
        Element elSearch = new Element("search");
        Element elRequestFormat = new Element("requestFormat");
        Element elResponseFormat = new Element("responseFormat");
        Element elFilter = new Element("filter");
        Element elLike = new Element("like");
        Element elCount = new Element("count");

        query.setRootElement(elRequest);
        elRequest.addContent(elHeader);

        elHeader.addContent(elType);
        elType.addContent("search");

        elRequest.addContent(elSearch);

        elSearch.addContent(elRequestFormat);
        elRequestFormat.addContent("http://www.tdwg.org/schemas/abcd/2.06");

        elRequest.addContent(elResponseFormat);

        elResponseFormat.setAttribute("start", "0");
        elResponseFormat.setAttribute("limit", "10");
        elResponseFormat.addContent("http://www.tdwg.org/schemas/abcd/2.06");
        elResponseFormat.addContent(elFilter);

        elFilter.addContent(elLike);

        elLike.setAttribute("path", "/DataSets/DataSet/Units/Unit/Identifications/Identification/Result/TaxonIdentified/ScientificName/FullScientificNameString");
        elLike.addContent("A*");

        elResponseFormat.addContent(elCount);
        elCount.addContent("false");

        return query;
    }
}
