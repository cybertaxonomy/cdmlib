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
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.ext.occurrence.bioCase.BioCaseQueryServiceWrapper;

/**
 * @author pplitzner
 \* @since 16.06.2015
 *
 */
public class UnitAssociationParser {

    private static final Logger logger = Logger.getLogger(UnitAssociationParser.class);

    private final String prefix;

    private final SpecimenImportReport report;

    private final ICdmRepository cdmAppController;

    public UnitAssociationParser(String prefix, SpecimenImportReport report, ICdmRepository cdmAppController) {
        this.prefix = prefix;
        this.report = report;
        this.cdmAppController = cdmAppController;
    }

    public UnitAssociationWrapper parse(Element unitAssociation){
        if(prefix.equals("abcd:")){

        }
        else if(prefix.equals("abcd21:")){

        }
        //unit id
        String unitId = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"UnitID"));
        if(unitId==null){
            unitId = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"AssociatedUnitID"));//abcd 2.0.6
        }
        //institution code
        String institutionCode = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"SourceInstitutionCode"));
        if(institutionCode==null){
            institutionCode = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"AssociatedUnitSourceInstitutionCode"));//abcd 2.0.6
        }
        //institution name
        String institutionName = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"SourceName"));
        if(institutionName==null){
            institutionName = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"AssociatedUnitSourceName"));//abcd 2.0.6
        }
        //data access point
        URI datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"DatasetAccessPoint"), report);
        if(datasetAccessPoint==null){
            datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"Comment"), report);//abcd 2.0.6
        }
        //association type
        String associationType = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"AssociationType"));

        String unableToLoadMessage = String.format("Unable to load unit %s from %s", unitId, datasetAccessPoint);
        if(unitId!=null && datasetAccessPoint!=null){
            BioCaseQueryServiceWrapper serviceWrapper = new BioCaseQueryServiceWrapper();
            Set<String[]> unitIds = new HashSet<String[]>();
            String[] unitIdArray = {unitId};
            unitIds.add(unitIdArray);
            OccurenceQuery query = new OccurenceQuery(unitIds);
            try {
                InputStream inputStream = serviceWrapper.query(query, datasetAccessPoint);
                if(inputStream!=null){
                    UnitAssociationWrapper unitAssociationWrapper = null;
                    try {
                        unitAssociationWrapper = AbcdParseUtility.parseUnitsNodeList(inputStream, report);
                    } catch (Exception e) {
                        String exceptionMessage = "An exception occurred during parsing of associated units!";
                        logger.error(exceptionMessage, e);
                        report.addException(exceptionMessage, e);
                    }

                    if(unitAssociationWrapper!=null){
                        unitAssociationWrapper.setAssociationType(associationType);
                        unitAssociationWrapper.setAccesPoint(datasetAccessPoint);
                        if(unitAssociationWrapper.getAssociatedUnits()!=null && unitAssociationWrapper.getAssociatedUnits().getLength()>1){
                            String moreThanOneUnitFoundMessage = String.format("More than one unit was found for unit association to %s", unitId);
                            logger.warn(moreThanOneUnitFoundMessage);
                            report.addInfoMessage(moreThanOneUnitFoundMessage);
                        }
                    }
                    return unitAssociationWrapper;
                }
                else{
                    logger.error(unableToLoadMessage);
                    report.addInfoMessage(unableToLoadMessage);
                }
            } catch (ClientProtocolException e) {
                logger.error(unableToLoadMessage, e);
                report.addInfoMessage(unableToLoadMessage);
            } catch (IOException e) {
                logger.error(unableToLoadMessage, e);
                report.addInfoMessage(unableToLoadMessage);
            }
        }
        else{
            report.addInfoMessage(unableToLoadMessage);
        }
        return null;
    }

    public UnitAssociationWrapper parseSiblings(String unitID, URI datasetAccessPoint){

        String unableToLoadMessage = String.format("Unable to load unit %s from %s", unitID, datasetAccessPoint);
        if(unitID!=null && datasetAccessPoint!=null){
            BioCaseQueryServiceWrapper serviceWrapper = new BioCaseQueryServiceWrapper();


            OccurenceQuery query = new OccurenceQuery(unitID);


                InputStream inputStream;
                try {
                    inputStream = serviceWrapper.querySiblings(query, datasetAccessPoint);

                    if(inputStream!=null){
                    UnitAssociationWrapper unitAssociationWrapper = null;
                    try {
                        unitAssociationWrapper = AbcdParseUtility.parseUnitsNodeList(inputStream, report);

                    } catch (Exception e) {
                        String exceptionMessage = "An exception occurred during parsing of associated units!";
                        logger.error(exceptionMessage, e);
                        report.addException(exceptionMessage, e);
                    }


                    return unitAssociationWrapper;
                    }
                    else{
                        logger.error(unableToLoadMessage);
                        report.addInfoMessage(unableToLoadMessage);
                    }
                } catch (ClientProtocolException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
            else{
                report.addInfoMessage(unableToLoadMessage);
            }
        return null;
    }

}
