/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.idminter;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author a.kohlbecker
 * @since Dec 12, 2017
 *
 */
public class RegistrationIdentifierMinter implements IdentifierMinter<String> {

    private static final Logger logger = Logger.getLogger(RegistrationIdentifierMinter.class);

    enum Method {
        naturalNumberIncrement
    }

    private SessionFactory factory;
    @Autowired
    protected void setSessionFactory (SessionFactory  factory){
        this.factory = factory;
    }

    Integer minLocalId = 1;

    Integer maxLocalId = Integer.MAX_VALUE;

    String identifierFormatString = null;

    Method method = Method.naturalNumberIncrement;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinLocalId(String min) {
        minLocalId = Integer.valueOf(min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxLocalId(String max) {
        maxLocalId = Integer.valueOf(max);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     *
     */
    protected Identifier<String> mintByNaturalNumberIncrement() {
        Integer localid = null;
        Object result = null;
        StatelessSession session = factory.openStatelessSession();
        try{

            String filter = " where cast(reg.specificIdentifier as int) >= " + minLocalId + " AND cast(reg.specificIdentifier as int) <= " + maxLocalId;
            if(identifierFormatString != null){
                filter += " AND reg.identifier like '" + identifierFormatString.replaceAll("%s", "%") + "'";
            }
            String hql = "select max(cast(reg.specificIdentifier as int)) from Registration as reg" + filter;

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

            Query query = session.createQuery(hql);

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
            Identifier<String> identifier = new Identifier<String>();
            identifier.localId = localid.toString();
            if(identifierFormatString != null){
                identifier.identifier = String.format(identifierFormatString, identifier.localId);
            }
            return identifier;
        }
        return null; // should never happen
    }

    public void setGenerationMethod(Method method){
        this.method = method;
    }

    /**
     * @return the identifierFormatString
     */
    public String getIdentifierFormatString() {
        return identifierFormatString;
    }

    /**
     * @param identifierFormatString the identifierFormatString to set
     */
    public void setIdentifierFormatString(String identifierFormatString) {
        this.identifierFormatString = identifierFormatString;
    }


}
