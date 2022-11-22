/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.terms;

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

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.SimpleImport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Imports term representations from a csv file following the format:
 * <BR><BR>
 * 1. field: uuid of term of type {@link TermBase} ({@link TermVocabulary} or {@link DefinedTermBase})<BR>
 * 2. Not used, usually be used for the default language label of the term for better human readability<BR>
 * 3. Label of the {@link Representation representation} (@link {@link Representation#getLabel()})<BR>
 * 4. Description of the {@link Representation representation} ({@link Representation#getDescription()})<BR>
 * 5. AbbrevLabel of the {@link Representation representation} (@link {@link Representation#getAbbreviatedLabel()})<BR>
 *
 * @author a.mueller
 * @since 14.03.2017
 */
@Component
public class RepresentationCsvImport
            extends SimpleImport<RepresentationCsvImportConfigurator, File>{

    private static final long serialVersionUID = -5600766240192189822L;
    private static final Logger logger = LogManager.getLogger();

    @Override
    protected void doInvoke(RepresentationCsvImportConfigurator config) {
        try {
            InputStreamReader inputReader = config.getSource();
            CSVReader csvReader = new CSVReader(inputReader, ',');
            List<String[]> lines = csvReader.readAll();
            if (lines.isEmpty()){
                logger.info("Import file is empty");
                csvReader.close();
                return;
            }

            @SuppressWarnings("rawtypes")
            Set<DefinedTermBase> termsToSave = new HashSet<>();
            @SuppressWarnings("rawtypes")
            Set<TermVocabulary> vocsToSave = new HashSet<>();
            UUID langUuid = config.getLanguageUuid();
            DefinedTermBase<?> languageTerm = getTermService().find(langUuid);
            if (languageTerm == null || !languageTerm.isInstanceOf(Language.class)){
                logger.warn("Language not recognized. Skip import");
                csvReader.close();
                return;
            }
            Language language = CdmBase.deproxy(languageTerm, Language.class);

            int i = 0;
            for (String[] strs : lines){
                TermBase term = handleSingleLine(config, strs, language, i);
                if (term != null){
                    if (term.isInstanceOf(DefinedTermBase.class)){
                        termsToSave.add(CdmBase.deproxy(term, DefinedTermBase.class));
                    }else if (term.isInstanceOf(TermVocabulary.class)){
                        vocsToSave.add(CdmBase.deproxy(term, TermVocabulary.class));
                    }else{
                        csvReader.close();
                        throw new IllegalArgumentException("Term class type not yet supported: " + term.getClass().getSimpleName());
                    }
                }
                i++;
            }
            csvReader.close();
            getTermService().saveOrUpdate(termsToSave);
            getVocabularyService().saveOrUpdate(vocsToSave);

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
    private TermBase handleSingleLine(RepresentationCsvImportConfigurator config, String[] strs,
            Language language, int i) {
        if (strs.length<1){
            String message = String.format(
                    "No data available in line %d. Skipped", i);
            logger.info(message);
            return null;
        }
        String uuidStr = strs[0];
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (Exception e) {
            String message = String.format(
                    "Term identifier not recognized as UUID in line %d. Skipped", i);
            logger.info(message);
            return null;
        }
        TermBase term = getTerm(uuid);
        if (term == null){
            String message = String.format(
                    "Term for uuid %s could not be found in line %d. Skipped", uuid.toString(), i);
            logger.info(message);
            return null;
        }
        if (strs.length<3){
            String message = String.format(
                    "Record in line %d has no representation information. Skipped", i);
            logger.info(message);
            return null;
        }
        //skip column 1, it is for default language label, but not used during import
        String label = null;
        String description = null;
        String abbrev = null;
        if (isNotBlank(strs[2])){
            label = strs[2];
        }
        if (strs.length > 3 && isNotBlank(strs[3])){
            description = strs[3];
        }
        if (strs.length > 4 && isNotBlank(strs[4])){
            abbrev = strs[4];
        }

        boolean hasData = label != null || description != null ||
                abbrev != null;
        if (!hasData){
            String message = String.format(
                    "Record in line %d has no relevant information. Skipped", i);
            logger.info(message);
            return null;
        }

        Representation representation = term.getRepresentation(language);
        if (representation == null){
            representation = Representation.NewInstance(description, label, abbrev, language);
            term.addRepresentation(representation);
        }
        if (label != null || config.isOverrideWithEmpty()){
            representation.setLabel(label);
        }
        if (description != null || config.isOverrideWithEmpty()){
            representation.setText(description);
        }
        if (abbrev != null || config.isOverrideWithEmpty()){
            representation.setAbbreviatedLabel(abbrev);
        }
        return term;
    }

    private TermBase getTerm(UUID uuid) {
        TermBase result = getTermService().find(uuid);
        if (result == null){
            result = getVocabularyService().find(uuid);
        }
        return result;
    }
}