/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.functional;

import java.sql.ResultSet;
import java.sql.SQLException;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @since 30.04.2018
 *
 */
public class TestSource {
    public static void main(String[] arg){
        Source source = EDAPHOBASE8();
        ResultSet a = source.getResultSet("SELECT count(*) FROM tax_taxon");
        try {
            a.next();
            long size = a.getLong(1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static Source EDAPHOBASE8(){
        String dbms = Source.POSTGRESQL9;  //TODO 10
        String strServer = "130.133.70.26";  //BGBM-PESISQL
        String strDB = "cdm_edapho";
        int port = 5432; // 5433;
        String userName = "postgres";
        return  makeSource(dbms, strServer, strDB, port, userName, null);
    }

    private static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd ){
        //establish connection
        Source source = null;
        source = new Source(dbms, strServer, strDB);
        source.setPort(port);

        pwd = AccountStore.readOrStorePassword(dbms, strServer, userName, pwd);
        source.setUserAndPwd(userName, pwd);
        // write pwd to account store
        return source;
    }

}
