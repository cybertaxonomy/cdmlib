// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.ext.occurrence.bioCase.BioCaseQueryServiceWrapper;

/**
 * @author pplitzner
 * @date 16.06.2015
 *
 */
public class UnitAssociationParser {

    private static final Logger logger = Logger.getLogger(UnitAssociationParser.class);

    private final String prefix;

    private final Abcd206ImportReport report;

    private final ICdmApplicationConfiguration cdmAppController;


    public UnitAssociationParser(String prefix, Abcd206ImportReport report, ICdmApplicationConfiguration cdmAppController) {
        this.prefix = prefix;
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public void parse(Element item, Abcd206ImportState state){
        NodeList associationsList = item.getElementsByTagName(prefix+"Associations");
        if(associationsList.getLength()==1 && associationsList.item(0) instanceof Element){
            Element associations = (Element)associationsList.item(0);
            NodeList unitAssociationList = associations.getElementsByTagName(prefix+"UnitAssociation");

            for(int i=0;i<unitAssociationList.getLength();i++){

                Element unitAssociation = (Element)unitAssociationList.item(0);

                //unit id
                String unitId = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"UnitID"));
                //data access point
                URI datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"DatasetAccessPoint"));
                if(datasetAccessPoint==null){
                    datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"Comment"));
                }
                String message = "Unable to load unit "+unitId+" from "+datasetAccessPoint;
                if(datasetAccessPoint!=null){
                    //association type
                    NodeList associationTypeList = unitAssociation.getElementsByTagName(prefix+"AssociationType");

                    BioCaseQueryServiceWrapper serviceWrapper = new BioCaseQueryServiceWrapper();
                    OccurenceQuery query = new OccurenceQuery(unitId);
                    try {
                        serviceWrapper.query(query, datasetAccessPoint);
                    } catch (ClientProtocolException e) {
                        logger.error(message, e);
                    } catch (IOException e) {
                        logger.error(message, e);
                    }
                }
                else{
                    report.addInfoMessage(message);
                }
            }




//            //FIXME: how to handle multiple unit assocations?
//            // maybe check AssociationType but this needs to be stable
//            // for only the first unitAssociation will be used
//            if(unitAssociationList.getLength()>0 && unitAssociationList.item(0) instanceof Element){
//                String collectionName = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"SourceName"));
//                List<Collection> matchingCollections = cdmAppController.getCollectionService().findByTitle(Collection.class, collectionName, MatchMode.EXACT, null, null, null, null, null).getRecords();
//                Collection collection;
//                if(matchingCollections.size()==1){
//                    collection = matchingCollections.iterator().next();
//                }
//                else{
//                    collection = Collection.NewInstance();
//                    collection.setName(collectionName);
//                }
//                String institutionName = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"SourceInstitutionCode"));
//                List<AgentBase> matchingInstitutions = cdmAppController.getAgentService().findByTitle(Institution.class, institutionName, MatchMode.EXACT, null, null, null, null, null).getRecords();
//                Institution institution;
//                if(matchingInstitutions.size()==1){
//                    institution = (Institution) matchingInstitutions.iterator().next();
//                }
//                else{
//                    institution = Institution.NewInstance();
//                    institution.setName(institutionName);
//                }
//            }
        }
    }

}
