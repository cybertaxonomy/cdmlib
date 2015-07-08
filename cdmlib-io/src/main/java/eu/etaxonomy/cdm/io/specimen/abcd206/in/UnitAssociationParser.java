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
import java.io.InputStream;
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

    public UnitAssociationWrapper parse(Element unitAssociation, Abcd206ImportState state){
        //unit id
        String unitId = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"UnitID"));
        //data access point
        URI datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"DatasetAccessPoint"));
        if(datasetAccessPoint==null){
            datasetAccessPoint = AbcdParseUtility.parseFirstUri(unitAssociation.getElementsByTagName(prefix+"Comment"));
        }
        //association type
        String associationType = AbcdParseUtility.parseFirstTextContent(unitAssociation.getElementsByTagName(prefix+"AssociationType"));

        String unableToLoadMessage = String.format("Unable to load unit %s from %s", unitId, datasetAccessPoint);
        if(datasetAccessPoint!=null){
            BioCaseQueryServiceWrapper serviceWrapper = new BioCaseQueryServiceWrapper();
            OccurenceQuery query = new OccurenceQuery(unitId);
            try {
                InputStream inputStream = serviceWrapper.query(query, datasetAccessPoint);
                if(inputStream!=null){
                    state.getConfig().setSource(inputStream);
                    NodeList associatedUnits = null;
                    try {
                        associatedUnits = AbcdParseUtility.parseUnitsNodeList(state);
                    } catch (Exception e) {
                        String exceptionMessage = "An exception occurred during parsing of associated units!";
                        logger.error(exceptionMessage, e);
                        report.addException(exceptionMessage, e);
                    }

                    if(associatedUnits!=null && associatedUnits.getLength()>1){
                        String moreThanOneUnitFoundMessage = String.format("More than one unit was found for unit association to %s", unitId);
                        logger.warn(moreThanOneUnitFoundMessage);
                        report.addInfoMessage(moreThanOneUnitFoundMessage);
                    }

                    return new UnitAssociationWrapper(associatedUnits, associationType);
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

}
