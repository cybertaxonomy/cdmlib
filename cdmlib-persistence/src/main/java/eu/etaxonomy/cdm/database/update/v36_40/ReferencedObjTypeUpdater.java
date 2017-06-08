/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v36_40;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.EventBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTerm;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.MultiAccessKey;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.AmplificationResult;
import eu.etaxonomy.cdm.model.molecular.Cloning;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MaterialOrMethodEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;


/**
 * Updates the xxxObj_type field in Annotations, Markers, Extensions, Identifiers.
 * Not needed anymore as long as we gave up bidirectionality #5743
 *
 * @author a.mueller
 * @date 25.04.2016
 */
public class ReferencedObjTypeUpdater extends SchemaUpdaterStepBase{
	private static final Logger logger = Logger.getLogger(ReferencedObjTypeUpdater.class);

	private static final String stepName = "Update referenced obj_type";

// **************************** STATIC METHODS ********************************/

	public static final ReferencedObjTypeUpdater NewInstance(){
		return new ReferencedObjTypeUpdater(stepName);
	}

	protected ReferencedObjTypeUpdater(String stepName) {
		super(stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

	    //TODO should better be read from eu.etaxonomy.cdm.model.common.package-info @AnyMetaDef
		try {
			Class<AnnotatableEntity>[] annotatableClasses = new Class[]{
			    Institution.class,
			    Person.class,
			    Team.class,
			    CategoricalData.class,
			    CommonTaxonName.class,
			    DescriptionElementSource.class,
			    Distribution.class,
			    Feature.class,
			    FeatureTree.class,
			    IndividualsAssociation.class,
			    MeasurementUnit.class,
			    MediaKey.class,
			    MultiAccessKey.class,
			    NaturalLanguageTerm.class,
			    PolytomousKey.class,
			    PresenceAbsenceTerm.class,
			    QuantitativeData.class,
			    SpecimenDescription.class,
			    State.class,
			    StatisticalMeasure.class,
			    TaxonDescription.class,
			    TaxonInteraction.class,
			    TaxonNameDescription.class,
			    TextData.class,
			    TextFormat.class,
			    WorkingSet.class,
			    Country.class,
			    NamedArea.class,
			    NamedAreaLevel.class,
			    NamedAreaType.class,
			    ReferenceSystem.class,
			    Media.class,
			    Rights.class,
			    RightsType.class,
			    Amplification.class,
			    AmplificationResult.class,
			    Cloning.class,
			    DnaSample.class,
			    PhylogeneticTree.class,
			    Primer.class,
			    Sequence.class,
			    SingleRead.class,
//			    BacterialName.class,
//			    BotanicalName.class,
//			    CultivarPlantName.class,
			    HomotypicalGroup.class,
			    HybridRelationship.class,
			    HybridRelationshipType.class,
			    NameRelationship.class,
			    NameRelationshipType.class,
			    NameTypeDesignationStatus.class,
                NameTypeDesignation.class,
			    NomenclaturalStatus.class,
			    NomenclaturalStatusType.class,
//			    NonViralName.class,
			    Rank.class,
			    SpecimenTypeDesignationStatus.class,
                SpecimenTypeDesignation.class,
//                ViralName.class,
//			    ZoologicalName.class,
                TaxonName.class,
			    Collection.class,
			    DerivationEvent.class,
			    DerivationEventType.class,
			    DerivedUnit.class,
			    DeterminationEvent.class,
			    FieldUnit.class,
			    GatheringEvent.class,
			    MaterialOrMethodEvent.class,
			    MediaSpecimen.class,
			    PreservationMethod.class,
			    Reference.class,
			    Classification.class,
			    Synonym.class,
			    SynonymType.class,
			    Taxon.class,
			    TaxonNode.class,
			    TaxonNodeAgentRelation.class,
			    TaxonRelationship.class,
			    TaxonRelationshipType.class,

			    TermVocabulary.class,
			    OrderedTermVocabulary.class,
			    AnnotationType.class,
			    DefinedTermBase.class,
			    ExtensionType.class,
			    Language.class,
			    MarkerType.class,
			    OrderedTerm.class,
			    Annotation.class,
			    Identifier.class,
			    Credit.class,
			    LanguageString.class,
			    Representation.class,
			    IdentifiableSource.class

			};

			for (Class<AnnotatableEntity> annotatableClass : annotatableClasses){
			    updateSingleClass(datasource, monitor, caseType, annotatableClass);
			}

			return;

		} catch (Exception e) {
		    String message = e.getMessage();
            monitor.warning(message, e);
            logger.warn(message);
            result.addException(e, message, this, "invoke");
            return;
		}
	}

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param annotatableClass2
     * @throws SQLException
     */
    private void updateSingleClass(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, Class<? extends AnnotatableEntity> annotatableClass) throws SQLException {
        if (IdentifiableEntity.class.isAssignableFrom(annotatableClass)){
            updateIdentifableEntitiy(datasource, monitor, caseType, (Class<IdentifiableEntity>)annotatableClass);
        }
        updateAnnotatableEntitiy(datasource, monitor, caseType, annotatableClass);
    }

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param annotatableClass
     * @throws SQLException
     */
    private void updateAnnotatableEntitiy(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            Class<? extends AnnotatableEntity> annotatableClass) throws SQLException {
        String sql, table;

        table = "Annotation";
        sql = "UPDATE @table SET annotatedObj_type = '@simpleClassName' WHERE id IN (SELECT annotations_id FROM @MnTable)";
        sql = setParameters(caseType, sql, table, annotatableClass);
        datasource.executeUpdate(sql);
        replaceAndExecute(datasource, caseType, annotatableClass, sql, table);

        table = "Marker";
        sql = "UPDATE @table SET markedObj_type = '@simpleClassName' WHERE id IN (SELECT markers_id FROM @MnTable)";
        sql = setParameters(caseType, sql, table, annotatableClass);
        datasource.executeUpdate(sql);

    }

    /**
     * @param caseType
     * @param sql
     * @param table
     * @param identifiableClass
     * @param isAudit
     * @return
     */
    private String setParameters(CaseType caseType, String sql, String table, Class<?> identifiableClass) {
        String simpleName = identifiableClass.getSimpleName();
        Class<?> tableClass = getTable(identifiableClass);
        String tableName = tableClass.getSimpleName();
        if (tableName.equals("Rights")){
            tableName = "RightsInfo";
        }
        if (tableName.equals("TaxonName")){
            tableName = "TaxonNameBase";
        }

        String casedTable = caseType.transformTo(table);
        String mnTable = caseType.transformTo(tableName + "_" + table);
        return sql.replace("@table", casedTable)
                .replace("@simpleClassName", simpleName)
                .replace("@MnTable", mnTable);

    }

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param annotatableClass
     * @throws SQLException
     */
    private void updateIdentifableEntitiy(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            Class<? extends IdentifiableEntity> identifiableClass) throws SQLException {
        String sql, table;

        //credits, rights => do not have xxxObj_type field
        //, extensions, identifiers, sources

        //Extensions
        table = "Extension";
        sql = "UPDATE @table SET extendedObj_type = '@simpleClassName' WHERE id IN (SELECT extensions_id FROM @MnTable)";
        replaceAndExecute(datasource, caseType, identifiableClass, sql, table);

        //Identifier
        table = "Identifier";
        sql = "UPDATE @table SET identifiedObj_type = '@simpleClassName' WHERE id IN (SELECT identifiers_id FROM @MnTable)";
        replaceAndExecute(datasource, caseType, identifiableClass, sql, table);

        //OriginalSourceBase
        table = "OriginalSourceBase";
        sql = "UPDATE @table SET sourcedObj_type = '@simpleClassName' WHERE id IN (SELECT sources_id FROM @MnTable)";
        replaceAndExecute(datasource, caseType, identifiableClass, sql, table);

    }

    /**
     * @param datasource
     * @param caseType
     * @param annotatableClass
     * @param sql
     * @param table
     * @throws SQLException
     */
    private void replaceAndExecute(ICdmDataSource datasource, CaseType caseType,
            Class<? extends AnnotatableEntity> annotatableClass, String sql, String table) throws SQLException {
        sql = setParameters(caseType, sql, table, annotatableClass);
        datasource.executeUpdate(sql);
    }

    /**
     * @param identifiableClass
     */
    private Class<?> getTable(Class<?> clazz) {
        Class<?> spezificClass = specificClasses.get(clazz);
        if (spezificClass != null){
            return spezificClass;
        }else{
            boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
            if (isAbstract){
                return clazz;
            }else{
                Class<?> superClass = clazz.getSuperclass();
                //MappedSuperClasses
                if (superClass == EventBase.class
                        || superClass == AnnotatableEntity.class
                        || superClass == IdentifiableEntity.class
                        || superClass == RelationshipBase.class
                        || superClass == ReferencedEntityBase.class
                        || superClass == IdentifiableMediaEntity.class
                        || superClass == LanguageStringBase.class
                         ) {
                    return clazz;
                }
                return getTable(superClass);
            }
        }
    }

    private static Map<Class, Class> specificClasses = new HashMap<>();
    static{
        specificClasses.put(TeamOrPersonBase.class, AgentBase.class);
        specificClasses.put(TermVocabulary.class, TermVocabulary.class);
        specificClasses.put(TypeDesignationStatusBase.class, DefinedTermBase.class);
        specificClasses.put(OrderedTermBase.class, DefinedTermBase.class);
        specificClasses.put(OrderedTermVocabulary.class, TermVocabulary.class);
        specificClasses.put(Rights.class, Rights.class);
        specificClasses.put(RelationshipTermBase.class, DefinedTermBase.class);

    }

}
