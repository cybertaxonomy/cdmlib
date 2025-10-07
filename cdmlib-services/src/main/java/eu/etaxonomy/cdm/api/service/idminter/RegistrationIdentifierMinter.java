/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.model.agent.Institution;

/**
 * @author a.kohlbecker
 * @since Dec 12, 2017
 */
public class RegistrationIdentifierMinter implements IdentifierMinter<String> {

    private static final Logger logger = LogManager.getLogger();

    private static final UUID PHYCOBANK_BERLIN_UUID = UUID.fromString("29065db7-2d4c-4d15-bfe3-da6e89fc91c0");

    enum Method {
        naturalNumberIncrement
    }

    enum RegistrationCenter {
        PHYCOBANK(PHYCOBANK_BERLIN_UUID);

        private UUID institutionUuid;
        RegistrationCenter(UUID institutionUuid) {
            this.institutionUuid = institutionUuid;
        }
    }

    private SessionFactory factory;

    @Autowired
    protected void setSessionFactory (SessionFactory  factory){
        this.factory = factory;
    }

    @Autowired
    private IAgentService agentService;

    private Integer minLocalId = 1;

    private Integer maxLocalId = Integer.MAX_VALUE;

    private String identifierFormatString = null;

    private Method method = Method.naturalNumberIncrement;

    private Pattern identifierPattern;

    private RegistrationCenter registrationCenter = RegistrationCenter.PHYCOBANK;  //for now hardcoded, needs adaption if other reg.centers should be supported

    private Institution registrationCenterInstitution;

    @Override
    public void setMinLocalId(String min) {
        if (min == null){
            minLocalId = 1;  //default
        }else{
            minLocalId = Integer.valueOf(min);
        }
    }

    @Override
    public void setMaxLocalId(String max) {
        if (max == null){
            maxLocalId = Integer.MAX_VALUE;  //default
        }else{
            maxLocalId = Integer.valueOf(max);
        }
    }

    @Override
    synchronized public Identifier<String> mint() throws OutOfIdentifiersException {

        if(identifierFormatString == null){
            logger.warn("identifierFormatString missing");
        }

        switch(method){
            case naturalNumberIncrement:
                return mintByNaturalNumberIncrement();
            default: throw new RuntimeException("unsupported genreation method: " + method);
        }
    }

    protected Identifier<String> mintByNaturalNumberIncrement() {
        Integer localid = null;
        Object result = null;
        StatelessSession session = factory.openStatelessSession();
        try{

            String filter = " WHERE CAST(reg.specificIdentifier AS int) >= " + minLocalId + " AND CAST(reg.specificIdentifier AS int) <= " + maxLocalId;
            if(identifierFormatString != null){
                filter += " AND reg.identifier LIKE '" + identifierFormatString.replaceAll("%s", "%") + "'";
            }
            String hql = "SELECT MAX(CAST(reg.specificIdentifier AS int)) FROM Registration AS reg" + filter;

            // a query with correlated sub-select should allow to filter out registrations which are not matching the formatString
            // but the hql below is not working as expected:
//          String filter = "";
//          if(identifierFormatString != null){
//              filter += " WHERE reg.identifier like '" + identifierFormatString.replaceAll("%s", "%") + "'";
//          }
//          String hql =
//                  "SELECT "
//                  //+ "max("
//                  // + " cast( "

//                  + " ( SELECT max(cast(reg.specificIdentifier as int)) FROM reg WHERE cast(reg.specificIdentifier as int) >= " + minLocalId + " AND cast(reg.specificIdentifier as int) <= " + maxLocalId + ") "
//                  //+ " as int)"
//                  //+ ")"
//                  + " FROM Registration reg " + filter;

            Query<Object> query = session.createQuery(hql, Object.class);

            result = query.uniqueResult();
        } finally {
            session.close();
        }
        if(result != null){
            localid  = ((Integer)result) + 1;
            if(localid > maxLocalId){
                throw new OutOfIdentifiersException("No available identifiers left in range [" + minLocalId + ", " + maxLocalId + "]");
            }
        } else {
            localid = minLocalId;
        }

        if(localid != null){
            Identifier<String> identifier = new Identifier<>();
            identifier.localId = localid.toString();
            if(identifierFormatString != null){
                identifier.identifier = String.format(identifierFormatString, identifier.getLocalId());
            }else {
                identifier.identifier = String.format("Missing_identifier_format_" + identifier.getLocalId());
            }
            return identifier;
        }
        return null; // should never happen
    }

    public void setGenerationMethod(Method method){
        this.method = method;
    }

    @Override
    public boolean isFromOwnRegistration(String identifierString){
        return identifierPattern().matcher(identifierString).matches();
    }

    public Pattern identifierPattern() {
        if(identifierPattern == null){
            if(identifierFormatString == null){
                identifierPattern = Pattern.compile("\\d*");
            } else {
                String patternString = Matcher.quoteReplacement(identifierFormatString);
                patternString =  "^" + patternString.replace("%s", "\\d+") + "$";
                identifierPattern = Pattern.compile(patternString);
            }
        }
        return identifierPattern;
    }

    public String getIdentifierFormatString() {
        return identifierFormatString;
    }
    public void setIdentifierFormatString(String identifierFormatString) {
        this.identifierFormatString = identifierFormatString;
    }

    public Institution registrationCenter() {
        if (registrationCenterInstitution == null) {
            registrationCenterInstitution = (Institution)agentService.find(registrationCenter.institutionUuid);
        }
        return registrationCenterInstitution;

    }
}