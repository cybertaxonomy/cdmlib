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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.SimpleImport;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

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
 * @date 14.03.2017
 */
@Component
public class RepresentationCsvImport extends SimpleImport<RepresentationCsvImportConfigurator, File>{
    private static final long serialVersionUID = -5600766240192189822L;
    private static Logger logger = Logger.getLogger(RepresentationCsvImport.class);

    /**
     * {@inheritDoc}
     */
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
            Set<Representation> representations = new HashSet<>();
            UUID langUuid = config.getLanguageUuid();
            DefinedTermBase<?> languageTerm = getTermService().find(langUuid);
            if (!languageTerm.isInstanceOf(Language.class)){
                logger.warn("Language not recognized. Skip import");
                csvReader.close();
                return;
            }
            Language language = CdmBase.deproxy(languageTerm, Language.class);

            int i = 0;
            for (String[] strs : lines){
                Representation rep = handleSingleLine(config, strs, language, i);
                if (rep != null){
                    representations.add(rep);
                }
                i++;
            }
            csvReader.close();
            getTermService().saveOrUpdateRepresentations(representations);

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
    private Representation handleSingleLine(RepresentationCsvImportConfigurator config, String[] strs,
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
        String label = strs[2];
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
        return representation;
    }

    /**
     * @param uuid
     * @return
     */
    private TermBase getTerm(UUID uuid) {
        TermBase result = getTermService().find(uuid);
        if (result == null){
            result = getVocabularyService().find(uuid);
        }
        return result;
    }
}