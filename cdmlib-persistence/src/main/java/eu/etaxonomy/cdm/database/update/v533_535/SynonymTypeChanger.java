/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v533_535;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 13.12.2022
 */
public class SynonymTypeChanger extends SchemaUpdaterStepBase {

    private static final String step = "Change synonym type to enum";

    public static SynonymTypeChanger NewInstance (List<ISchemaUpdaterStep> stepList){
        return new SynonymTypeChanger(stepList);
    }

    private Map<UUID,String> map = new HashMap<>();

    private SynonymTypeChanger(List<ISchemaUpdaterStep> stepList) {
        super(stepList, step);
        map.put(UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e"), "SYN");
        map.put(UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2"), "HOM");
        map.put(UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084"), "HET");
        map.put(UUID.fromString("cb5bad12-9dbc-4b38-9977-162e45089c11"), "INS");
        map.put(UUID.fromString("f55a574b-c1de-45cc-9ade-1aa2e098c3b5"), "ING");
        map.put(UUID.fromString("089c1926-eb36-47e7-a2d1-fd5f3918713d"), "INE");
        map.put(UUID.fromString("7c45871f-6dc5-40e7-9f26-228318d0f63a"), "POT");
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        for (UUID uuid : map.keySet()) {
            String enumKey = map.get(uuid);
            update(datasource, caseType, uuid, enumKey);
        }

        String sqlUpdate = " UPDATE @@TaxonBase@@ "
                + " SET type = 'SYN' "
                + " WHERE type_id IS NULL ";
        datasource.executeUpdate(caseType.replaceTableNames(sqlUpdate));

        sqlUpdate = " UPDATE @@TaxonBase_AUD@@ "
                + " SET type = 'SYN' "
                + " WHERE type_id IS NULL AND REVTYPE <> 2 ";
        datasource.executeUpdate(caseType.replaceTableNames(sqlUpdate));
    }

    private void update(ICdmDataSource datasource, CaseType caseType,
            UUID uuid, String enumKey) throws SQLException {

        String sqlGetId = " SELECT id "
                + " FROM @@DefinedTermBase@@ "
                + " WHERE uuid = '" + uuid + "'";
        Integer id = (Integer)datasource.getSingleValue(caseType.replaceTableNames(sqlGetId));

        String sqlUpdate = " UPDATE @@TaxonBase@@ "
                + " SET type = '" + enumKey + "'"
                + " WHERE type_id = " + id;
        datasource.executeUpdate(caseType.replaceTableNames(sqlUpdate));

        sqlUpdate = " UPDATE @@TaxonBase_AUD@@ "
                + " SET type = '" + enumKey + "'"
                + " WHERE type_id = " + id;
        datasource.executeUpdate(caseType.replaceTableNames(sqlUpdate));

    }
}