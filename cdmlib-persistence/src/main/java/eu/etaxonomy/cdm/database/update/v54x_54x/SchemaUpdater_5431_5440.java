/**
 * Copyright (C) 2023 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v54x_54x;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermCreator;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author a.mueller
 * @date 2024-06-04
 */
public class SchemaUpdater_5431_5440 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_43_01;
	//TODO
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_44_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5430_5431.NewInstance();
    }

	public static SchemaUpdater_5431_5440 NewInstance() {
		return new SchemaUpdater_5431_5440();
	}

	SchemaUpdater_5431_5440() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
//		String tableName;
//		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#9755 Adapt grex representation
        stepName = "Adapt representation for grex (infraspec.)";
        UUID uuidTerm = UUID.fromString("08dcb4ff-ac58-48a3-93af-efb3d836ac84");
        String description = null;
        String label = null;
        String abbrev = "grex";
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, description, label, abbrev, uuidLanguage, true);

        //#10553
        stepName = "Adapt representation for annotation type 'technical' -> 'internal'";
        uuidTerm = UUID.fromString("6a5f9ea4-1bdd-4906-89ad-6e669f982d69");
        description = "internal";
        label = "internal";
        abbrev = null;
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm, description, label, abbrev, uuidLanguage);

        //#10492 Add term 'undefined annotation type'
        stepName = "Add term 'undefined annotation type'";
        uuidTerm = UUID.fromString("3ccf04c8-2739-43ad-ab53-de4b83b56e8b");
        UUID uuidAnnotationTypeVoc = UUID.fromString("ca04609b-1ba0-4d31-9c2e-aa8eb2f4e62d");
        description = "untyped";
        label = "untyped";
        abbrev = null;
        boolean isOrdered = false;
        @SuppressWarnings("rawtypes")
        Class<? extends DefinedTermBase> termClass = AnnotationType.class;
        TermType termType = TermType.AnnotationType;
        String idInVocabuary = null;
        String symbol1 = null;
        String symbol2 = null;
        TermCreator.NewTermInstance(stepList, uuidTerm, uuidAnnotationTypeVoc,
                description, label, abbrev, isOrdered, idInVocabuary, symbol1, symbol2,
                termClass, termType);

        //#10492 add annotation type 'undefined' where type is null
        stepName = "Add annotation type 'undefined' where type is null";
        String sql = " UPDATE @@Annotation@@ a "
                + " SET annotationType_id = (SELECT id FROM @@DefinedTermBase@@ WHERE uuid = '3ccf04c8-2739-43ad-ab53-de4b83b56e8b') "
                + " WHERE annotationType_id IS NULL ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, sql);

        //#10553
        stepName = "Adapt representation for annotation type 'undefined'";
        uuidTerm = UUID.fromString("3ccf04c8-2739-43ad-ab53-de4b83b56e8b");
        description = "Undefined (no specific annotation type selected)";
        label = "- (undefined)";
        abbrev = "-";
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm, description, label, abbrev, uuidLanguage);

        return stepList;
    }
}