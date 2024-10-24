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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.SimpleImport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.TermType;

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
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void doInvoke(IdentifierImportConfigurator config) {
        try {
            //read stream
            InputStreamReader inputReader = config.getSource();
            CSVReader csvReader = new CSVReader(inputReader, config.getSeparator());
            List<String[]> lines = csvReader.readAll();
            if (lines.isEmpty()){
                logger.info("Import file is empty");
                csvReader.close();
                return;
            }

            //get identifier type
            IdentifierType idType = getIdentifierType(config, csvReader);
            if (idType == null) {
                return;
            }

            Set<UUID> entityUuidsHandled = new HashSet<>();
            int i = 0;
            for (String[] strs : lines){
                handleSingleLine(config, strs, idType, i, entityUuidsHandled);
                i++;
            }
            csvReader.close();

            //not needed as the objects update automatically during transactions in handleSingleLine()
//            getCommonService().saveOrUpdate(entitiesToSave);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IdentifierType getIdentifierType(IdentifierImportConfigurator config, CSVReader csvReader)
            throws IOException {
        UUID identifierTypeUuid = config.getIdentifierTypeUuid();

        DefinedTermBase<?> identifierType = getTermService().find(identifierTypeUuid);
        if (identifierType == null || identifierType.getTermType() != TermType.IdentifierType){
            logger.warn("IdentifierType not recognized. Skip import");
            csvReader.close();
            return null;
        }
        IdentifierType idType = CdmBase.deproxy(identifierType, IdentifierType.class);
        return idType;
    }

    /**
     * @param config configurator
     * @param strs text array for given line
     * @param i line counter
     * @param entityUuidsHandled
     * @return
     */
    private IdentifiableEntity<?> handleSingleLine(IdentifierImportConfigurator config,
            String[] strs, IdentifierType idType, int i, Set<UUID> entityUuidsHandled) {

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

        //titleCache
        if (strs.length>2){
            String entityCache = entity.getTitleCache();
            String nameField = strs[2];
            boolean ignoreWhitespace = true;
            if (ignoreWhitespace) {
                entityCache = CdmUtils.Nz(entityCache).replace(" ", "");
                nameField = CdmUtils.Nz(nameField).replace(" ", "");
            }
            if (!CdmUtils.nullSafeEqual(entityCache, nameField)){
                String message = String.format(
                        "Record in line %d has different titleCache: " + entityCache +" <-> "+ nameField, i);
                if (entity.isInstanceOf(TaxonName.class)) {
                    String entityNameCache = CdmBase.deproxy(entity, TaxonName.class).getNameCache();
                    entityNameCache = ignoreWhitespace? CdmUtils.Nz(entityNameCache).replace(" ", ""): entityNameCache;
                    if (CdmUtils.nullSafeEqual(entityNameCache, nameField)){
                        message = String.format(
                                "Record in line %d has different titleCache but nameField only has no author: " + entityCache +" <-> "+ nameField, i);
                    }
                }
                logger.warn(message);
            }
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

        Identifier identifier = null;
        //TODO clean redundant code
        if (config.isWarnAndDoNotOverrideIfExists()){
            boolean wasAlreadyImported = entityUuidsHandled.contains(uuid);
            if (wasAlreadyImported){
                String message = String.format(
                        "More than 1 instance for uuid '%s' ("+entity.getTitleCache()+") found in line %d. Updating not possible without deleting previous value as 'update existing' was selected. Record in line was neglected.", uuidStr, i);
                logger.warn(message);
                this.commitTransaction(tx);
                return null;
            }
            Set<Identifier> existingIdentifiers = entity.getIdentifiers(idType.getUuid());
            if (!existingIdentifiers.isEmpty()){
                identifier = existingIdentifiers.iterator().next();
                if (!CdmUtils.nullSafeEqual(identifier.getIdentifier(), value)){
                    String message = String.format(
                            "Existing identifier in line %d differs: " + identifier.getIdentifier() + "(existing)<->" + value   + "(import). Line not imported.", i);
                    logger.warn(message);
                    this.commitTransaction(tx);
                    return null;
                }
            }else{
                addNewIdentifier(idType, entity, value, identifier);
            }

        }else if (config.isUpdateExisting()){
            boolean wasAlreadyImported = entityUuidsHandled.contains(uuid);
            if (wasAlreadyImported){
                String message = String.format(
                        "More than 1 instance for uuid '%s' ("+entity.getTitleCache()+") found in line %d. Updating not possible without deleting previous value as 'update existing' was selected. Record in line was neglected.", uuidStr, i);
                logger.warn(message);
                this.commitTransaction(tx);
                return null;
            }else{
                Set<Identifier> existingIdentifiers = entity.getIdentifiers(idType.getUuid());
                if (existingIdentifiers.size() == 1){
                    identifier = existingIdentifiers.iterator().next();
                    if (!CdmUtils.nullSafeEqual(identifier.getIdentifier(), value)){
                        String message = String.format(
                                "Existing identifier in line %d differs: " + value, i);
                        logger.warn(message);
                        identifier.setIdentifier(value);
                    }
                }else if (existingIdentifiers.size() > 1){
                    String message = String.format(
                            "Taxon name in line %d has more than a single entry for the given identifier type. I can't update the value but added a new record", i);
                    logger.warn(message);
                    addNewIdentifier(idType, entity, value, identifier);
                }else{
                    addNewIdentifier(idType, entity, value, identifier);
                }
            }
        } else {
            addNewIdentifier(idType, entity, value, identifier);
        }
        entityUuidsHandled.add(uuid);

        this.commitTransaction(tx);
        return entity;
    }

    private void addNewIdentifier(IdentifierType idType, IdentifiableEntity<?> entity, String value,
            Identifier identifier) {
        if (identifier == null){
            identifier = Identifier.NewInstance(value, idType);
            entity.addIdentifier(identifier);
        }
    }

    private IdentifiableEntity<?> getEntityFromRepository(IdentifierImportConfigurator config, UUID uuid) {
        IdentifiableEntity<?> result = getCommonService().find(config.getCdmClass(), uuid);
        return result;
    }
}