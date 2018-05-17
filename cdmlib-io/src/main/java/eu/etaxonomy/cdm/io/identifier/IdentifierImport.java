/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.SimpleImport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.TermType;

/**
 * Imports identifiers for a certain {@link IdentifiableEntity} class from a csv
 * file. The class and the identifier type are defined in the configurator.
 *
 * The csv file has to follow the following format:
 * <BR><BR>
 * 1. field: uuid of the {@link IdentifiableEntity} of class defined in configurator<BR>
 * 2. The value of the identifier (of type defined in configurator)<BR><BR>
 *
 * NOTE: This import was first written for #6542
 * NOTE 2: TODO It was observed that the last line was not imported.
 *
 * @author a.mueller
 * @since 25.03.2017
 */
@Component
public class IdentifierImport
        extends SimpleImport<IdentifierImportConfigurator, File>{

    private static final long serialVersionUID = 5797541146159665997L;
    private static Logger logger = Logger.getLogger(IdentifierImport.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(IdentifierImportConfigurator config) {
        try {
            InputStreamReader inputReader = config.getSource();
            CSVReader csvReader = new CSVReader(inputReader, ';');
            List<String[]> lines = csvReader.readAll();
            if (lines.isEmpty()){
                logger.info("Import file is empty");
                csvReader.close();
                return;
            }

            @SuppressWarnings("rawtypes")
            Set<IdentifiableEntity> entitiesToSave = new HashSet<>();

            UUID identifierTypeUuid = config.getIdentifierTypeUuid();

            DefinedTermBase<?> identifierType = getTermService().find(identifierTypeUuid);
            if (identifierType == null || identifierType.getTermType() != TermType.IdentifierType){
                logger.warn("IdentifierType not recognized. Skip import");
                csvReader.close();
                return;
            }
            DefinedTerm idType = CdmBase.deproxy(identifierType, DefinedTerm.class);

            int i = 0;
            for (String[] strs : lines){
                IdentifiableEntity<?> entity = handleSingleLine(config, strs, idType, i);
                if (entity != null){
                    entitiesToSave.add(entity);
                }
                i++;
            }
            csvReader.close();

            //not needed as the objects update automatically during transactions in handleSingleLine()
//            getCommonService().saveOrUpdate(entitiesToSave);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param config configurator
     * @param strs text array for given line
     * @param i line counter
     * @return
     */
    private IdentifiableEntity<?> handleSingleLine(IdentifierImportConfigurator config,
            String[] strs, DefinedTerm idType, int i) {

        //no data
        if (strs.length < 1){
            String message = String.format(
                    "No data available in line %d. Skipped", i);
            logger.warn(message);
            return null;
        }

        //entity uuid
        String uuidStr = strs[0];
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (Exception e) {
            String message = String.format(
                    "Entity identifier not recognized as UUID in line %d. Skipped. Value was: %s", i, uuidStr);
            logger.warn(message);
            return null;
        }

        TransactionStatus tx = this.startTransaction();
        IdentifiableEntity<?> entity = getEntityFromRepository(config, uuid);
        if (entity == null){
            String message = String.format(
                    "Entity for uuid %s could not be found in line %d. Skipped", uuid.toString(), i);
            logger.warn(message);
            return null;
        }

        //identifier value
        if (strs.length < 2){
            String message = String.format(
                    "Record in line %d has no identifier value information. Skipped.", i);
            logger.warn(message);
            this.commitTransaction(tx);
            return null;
        }
        String value = null;
        if (isNotBlank(strs[1])){
            value = strs[1];
        }else if (config.isIgnoreEmptyIdentifier()){
            String message = String.format(
                    "Record in line %d has empty identifier value information. Skipped.", i);
            logger.debug(message);
            this.commitTransaction(tx);
            return null;
        }

        Identifier<?> identifier = Identifier.NewInstance(value, idType);
        entity.addIdentifier(identifier);

        this.commitTransaction(tx);
        return entity;
    }

    private IdentifiableEntity<?> getEntityFromRepository(IdentifierImportConfigurator config, UUID uuid) {
        IdentifiableEntity<?> result = getCommonService().find(config.getCdmClass(), uuid);
        return result;
    }
}