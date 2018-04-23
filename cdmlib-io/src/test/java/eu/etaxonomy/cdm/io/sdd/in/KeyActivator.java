/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.in;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.sdd.ikeyplus.IkeyPlusImportConfigurator;

/**
 * @author andreas
 \* @since Sep 19, 2012
 *
 */
public class KeyActivator {



    public static void main(String[] args) throws URISyntaxException{
        CdmDefaultImport<IImportConfigurator> keyImport = new CdmDefaultImport<IImportConfigurator>();

        ICdmDataSource destination = getDestination();

        String yyy = "/eu/etaxonomy/cdm/io/sdd/Cichorieae-fullSDD.xml";

        URL url = KeyActivator.class.getResource(yyy);
        URI uri = url.toURI();
        IImportConfigurator config = IkeyPlusImportConfigurator.NewInstance(uri, destination);


        keyImport.invoke(config);
    }

    private static ICdmDataSource getDestination() {
        DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
        String server = "160.45.63.201";
        String cdmDb = "cdm_edit_cichorieae";
        String user = "edit";
        String pw = AccountStore.readOrStorePassword(server, cdmDb, user, null);
        return CdmDataSource.NewMySqlInstance(server, cdmDb, user, pw);
    }
}
