/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdm2cdm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmApplication;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.ICdmImportSource;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ITaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitionerConcurrent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.AuthorityType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.ExternallyManaged;
import eu.etaxonomy.cdm.model.common.ExternallyManagedImport;
import eu.etaxonomy.cdm.model.common.IIntextReferencable;
import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.IntextReference;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.SingleSourcedEntityBase;
import eu.etaxonomy.cdm.model.common.SourcedEntityBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaMetaData;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.AvailableForIdentifiableBase;
import eu.etaxonomy.cdm.model.term.AvailableForTermBase;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermRelationBase;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * Base class for migrating data from one CDM instance to another.
 *
 * @author a.mueller
 * @since 17.08.2019
 */
public abstract class Cdm2CdmImportBase
        extends CdmImportBase<Cdm2CdmImportConfigurator, Cdm2CdmImportState> {

    private static final long serialVersionUID = 1344722304369624443L;
    private static final Logger logger = LogManager.getLogger();

    protected ICdmApplication sourceRepo(Cdm2CdmImportState state){
        ICdmApplication repo = state.getSourceRepository();
        if (repo == null){
            ICdmImportSource source = state.getConfig().getSource();
            if (source instanceof ICdmRepository){
                repo = (ICdmApplication)source;
            }else if (source instanceof ICdmDataSource){
                System.out.println("start source repo");
                boolean omitTermLoading = true;
                repo = CdmApplicationController.NewInstance((ICdmDataSource)source,
                        DbSchemaValidation.VALIDATE, omitTermLoading);
                state.setSourceRepository(repo);
                System.out.println("end source repo");
            }else{
                throw new IllegalStateException("Unsupported ICdmImportSource type");
            }
        }
        return repo;
    }

    protected  Contact detach(Contact contact, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        contact = CdmBase.deproxy(contact);
        if (contact == null){
            return contact;
        }else{
            return handlePersistedContact(contact, state);
        }
    }

    protected  IIntextReferencable detach(IIntextReferencable cdmBase, boolean onlyForDefinedSignature, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferencable)detach((CdmBase)cdmBase, state);
    }
    protected  IIntextReferenceTarget detach(IIntextReferenceTarget cdmBase, boolean onlyForDefinedSignature, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferenceTarget)detach((CdmBase)cdmBase, state);
    }

    protected <T extends CdmBase> T detach(T cdmBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return detach(cdmBase, false, state);
    }

    protected <T extends CdmBase> T detach(T cdmBase, boolean notFromSource, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        cdmBase = CdmBase.deproxy(cdmBase);
        if (cdmBase == null ){
            return cdmBase;
        }else if(isInCache(cdmBase, state)){
            return getCached(cdmBase, state);
        }else {
            if (state.getExistingObjects(cdmBase.getClass()) == null){
                loadExistingUuids(cdmBase.getClass(), state);
            }
            boolean exists = state.getExistingObjects(cdmBase.getClass()).contains(cdmBase.getUuid());
            if (exists){
                Class<T> clazz = (Class<T>)cdmBase.getClass();
                if (logger.isDebugEnabled()) {logger.debug("Load existing entity: " + clazz + ";" + cdmBase.getUuid());}
                T existingObj = getCommonService().findWithoutFlush(clazz, cdmBase.getUuid());
                if (existingObj != null){
                    cache(existingObj, state);
                    return existingObj;
                }else{
                    logger.warn("Object should exist already but does not exist in target. This should not happen: " + cdmBase.getClass().getSimpleName() + "/" + cdmBase.getUuid());
                }
            }
        }
        if ( !cdmBase.isPersisted()){
            logger.warn("Non persisted object not in cache and not in target DB. This should not happen: " + cdmBase.getUuid());
            return cdmBase; //should not happen anymore; either in cache or in target or persisted in source
        }else{
            return notFromSource? null : (T)handlePersisted(cdmBase, state);
        }
    }

    private Set<UUID> loadExistingUuids(Class<? extends CdmBase> clazz, Cdm2CdmImportState state) {
        List<UUID> list = getCommonService().listUuid(clazz);
        Set<UUID> result = new HashSet<>(list);
        state.putExistingObjects(clazz, result);
        return result;
    }

    protected <A extends CdmBase> CdmBase handlePersisted(A cdmBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
//        System.out.println("handle persisted: " + cdmBase.getClass().getName() + ";id=" + cdmBase.getId());
        if(cdmBase instanceof TaxonNode){
            return handlePersistedTaxonNode((TaxonNode)cdmBase, state);
        }else if(cdmBase instanceof Taxon){
            return handlePersistedTaxon((Taxon)cdmBase, state);
        }else if(cdmBase instanceof Synonym){
            return handlePersistedSynonym((Synonym)cdmBase, state);
        }else if(cdmBase instanceof TaxonName){
            return handlePersistedTaxonName((TaxonName)cdmBase, state);
        }else if(cdmBase instanceof Team){
            return handlePersistedTeam((Team)cdmBase, state);
        }else if(cdmBase instanceof Person){
            return handlePersistedPerson((Person)cdmBase, state);
        }else if(cdmBase instanceof Classification){
            return handlePersistedClassification((Classification)cdmBase, state);
        }else if(cdmBase instanceof Reference){
            return handlePersistedReference((Reference)cdmBase, state);
        }else if(cdmBase instanceof SpecimenOrObservationBase){
            return handlePersistedSpecimenOrObservationBase((SpecimenOrObservationBase)cdmBase, state);
        }else if(cdmBase instanceof IdentifiableSource){
            return handlePersistedIdentifiableSource((IdentifiableSource)cdmBase, state);
        }else if(cdmBase instanceof DescriptionElementSource){
            return handlePersistedDescriptionElementSource((DescriptionElementSource)cdmBase, state);
        }else if(cdmBase instanceof NomenclaturalSource){
            return handlePersistedNomenclaturalSource((NomenclaturalSource)cdmBase, state);
        }else if(cdmBase instanceof NamedSource){
            return handlePersistedNamedSource((NamedSource)cdmBase, state);
        }else if(cdmBase instanceof SecundumSource){
            return handlePersistedSecundumSource((SecundumSource)cdmBase, state);
        }else if(cdmBase instanceof CommonTaxonName){
            return handlePersistedCommonTaxonName((CommonTaxonName)cdmBase, state);
        }else if(cdmBase instanceof Distribution){
            return handlePersistedDistribution((Distribution)cdmBase, state);
        }else if(cdmBase instanceof TextData){
            return handlePersistedTextData((TextData)cdmBase, state);
        }else if(cdmBase instanceof TaxonInteraction){
            return handlePersistedTaxonInteraction((TaxonInteraction)cdmBase, state);
        }else if(cdmBase instanceof HomotypicalGroup){
            return handlePersistedHomotypicalGroup((HomotypicalGroup)cdmBase, state);
        }else if(cdmBase instanceof SpecimenTypeDesignation){
            return handlePersistedSpecimenTypeDesignation((SpecimenTypeDesignation)cdmBase, state);
        }else if(cdmBase instanceof NameTypeDesignation){
            return handlePersistedNameTypeDesignation((NameTypeDesignation)cdmBase, state);
        }else if(cdmBase instanceof TextualTypeDesignation){
            return handlePersistedTextualTypeDesignation((TextualTypeDesignation)cdmBase, state);
        }else if(cdmBase instanceof TaxonDescription){
            return handlePersistedTaxonDescription((TaxonDescription)cdmBase, state);
        }else if(cdmBase instanceof NomenclaturalStatus){
            return handlePersistedNomenclaturalStatus((NomenclaturalStatus)cdmBase, state);
        }else if(cdmBase instanceof TaxonNameDescription){
            return handlePersistedTaxonNameDescription((TaxonNameDescription)cdmBase, state);
        }else if(cdmBase instanceof TaxonRelationship){
            return handlePersistedTaxonRelationship((TaxonRelationship)cdmBase, state);
        }else if(cdmBase instanceof HybridRelationship){
            return handlePersistedHybridRelationship((HybridRelationship)cdmBase, state);
        }else if(cdmBase instanceof NameRelationship){
            return handlePersistedNameRelationship((NameRelationship)cdmBase, state);
        }else if(cdmBase instanceof TaxonNodeAgentRelation){
            return handlePersistedTaxonNodeAgentRelation((TaxonNodeAgentRelation)cdmBase, state);
        }else if(cdmBase instanceof User){
            return handlePersistedUser((User)cdmBase, state);
        }else if(cdmBase instanceof Extension){
            return handlePersistedExtension((Extension)cdmBase, state);
        }else if(cdmBase instanceof Media){
            return handlePersistedMedia((Media)cdmBase, state);
        }else if(cdmBase instanceof MediaRepresentation){
            return handlePersistedMediaRepresentation((MediaRepresentation)cdmBase, state);
        }else if(cdmBase instanceof MediaRepresentationPart){
            return handlePersistedMediaRepresentationPart((MediaRepresentationPart)cdmBase, state);
        }else if(cdmBase instanceof Marker){
            return handlePersistedMarker((Marker)cdmBase, state);
        }else if(cdmBase instanceof Annotation){
            return handlePersistedAnnotation((Annotation)cdmBase, state);
        }else if(cdmBase instanceof LanguageString){
            return handlePersistedLanguageString((LanguageString)cdmBase, state);
        }else if(cdmBase instanceof TermVocabulary){
            return handlePersistedVocabulary((TermVocabulary<?>)cdmBase, state);
        }else if(cdmBase instanceof TermTree){
            return handlePersistedTermTree((TermTree<?>)cdmBase, state);
        }else if(cdmBase instanceof NamedArea){
            return handlePersistedNamedArea((NamedArea)cdmBase, state);
        }else if(cdmBase instanceof NamedAreaLevel){
            return handlePersistedNamedAreaLevel((NamedAreaLevel)cdmBase, state);
        }else if(cdmBase instanceof NamedAreaType){
            return handlePersistedNamedAreaType((NamedAreaType)cdmBase, state);
        }else if(cdmBase instanceof TermNode){
            return handlePersistedTermNode((TermNode)cdmBase, state);
        }else if(cdmBase instanceof Representation){
            return handlePersistedRepresentation((Representation)cdmBase, state);
        }else if(cdmBase instanceof InstitutionalMembership){
            return handlePersistedInstitutionalMembership((InstitutionalMembership)cdmBase, state);
        }else if(cdmBase instanceof Institution){
            return handlePersistedInstitution((Institution)cdmBase, state);
        }else if(cdmBase instanceof IntextReference){
            return handlePersistedIntextReference((IntextReference)cdmBase, state);
        }else if(cdmBase instanceof ExtensionType){
            return handlePersistedExtensionType((ExtensionType)cdmBase, state);
        }else if(cdmBase instanceof NomenclaturalStatusType){
            return handlePersistedNomenclaturalStatusType((NomenclaturalStatusType)cdmBase, state);
        }else if(cdmBase instanceof MarkerType){
            return handlePersistedMarkerType((MarkerType)cdmBase, state);
        }else if(cdmBase instanceof AnnotationType){
            return handlePersistedAnnotationType((AnnotationType)cdmBase, state);
        }else if(cdmBase instanceof IdentifierType){
            return handlePersistedIdentifierType((IdentifierType)cdmBase, state);
        }else if(cdmBase instanceof Language){
            return handlePersistedLanguage((Language)cdmBase, state);
        }else if(cdmBase instanceof Rank){
            return handlePersistedRank((Rank)cdmBase, state);
        }else if(cdmBase instanceof Rights){
            return handlePersistedRights((Rights)cdmBase, state);
        }else if(cdmBase instanceof DefinedTerm){
            return handlePersistedDefinedTerm((DefinedTerm)cdmBase, state);
        }else if(cdmBase instanceof Character){
            return handlePersistedCharacter((Character)cdmBase, state);
        }else if(cdmBase instanceof Feature){
            return handlePersistedFeature((Feature)cdmBase, state);
        }else if(cdmBase instanceof State){
            return handlePersistedState((State)cdmBase, state);
        }else if(cdmBase instanceof DefinedTermBase){
            return handlePersistedTerm((DefinedTermBase<?>)cdmBase, state);
        }else if(cdmBase instanceof ExternalLink){
            return handlePersistedExternalLink((ExternalLink)cdmBase, state);
        }else if(cdmBase instanceof Identifier){
            return handlePersistedIdentifier((Identifier)cdmBase, state);
        }else if(cdmBase instanceof Credit){
            return handlePersistedCredit((Credit)cdmBase, state);
        }else {
            throw new RuntimeException("Type not yet supported: " + cdmBase.getClass().getCanonicalName());
        }
    }


    protected TaxonNode handlePersistedTaxonNode(TaxonNode node, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        TaxonNode result = handlePersisted((AnnotatableEntity)node, state);
        if (result ==null){
            return result;
        }
        //complete
        handleCollection(result, TaxonNode.class, "agentRelations", TaxonNodeAgentRelation.class, state);
        result.setTaxon(detach(result.getTaxon(), state));
        result.setSource(detach(node.getSource(), state));
        result.setSynonymToBeUsed(detach(result.getSynonymToBeUsed(), state));
        handleMap(result, TaxonNode.class, "statusNote", Language.class, LanguageString.class, state);
        //classification, parent, children
        this.setInvisible(node, "classification", detach(node.getClassification(), state));
        handleParentTaxonNode(result, state);
        setNewCollection(node, TaxonNode.class, "childNodes", TaxonNode.class);
        return result;
    }

    private void handleParentTaxonNode(TaxonNode childNode, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        boolean notFromSource = state.getConfig().isAddAncestors() ? false : true;
        TaxonNode parent = detach(childNode.getParent(), notFromSource, state);
        //TODO
        String microReference = null;
        Reference reference = null;
        if (parent == null && childNode.getClassification().getRootNode().equals(childNode)){
            //do nothing
        }else if (parent == null ){
            childNode.getClassification().addChildNode(childNode, reference, microReference) ;
        }else{
            parent.addChildNode(childNode, reference, microReference);
        }
    }

    protected Taxon handlePersistedTaxon(Taxon taxon, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Taxon result = handlePersisted((TaxonBase<?>)taxon, state);
        //complete
        handleCollection(result, Taxon.class, "synonyms", Synonym.class, state);
        //do not cascade to taxon nodes
//        handleCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        setNewCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        handleCollection(result, Taxon.class, "relationsFromThisTaxon", TaxonRelationship.class, state);
        handleCollection(result, Taxon.class, "relationsToThisTaxon", TaxonRelationship.class, state);
        //descriptions
        if (this.doDescriptions(state)){
            handleTaxonDescriptions(result, state);
        }else{
            setNewCollection(result, Taxon.class, "descriptions", TaxonDescription.class);
        }
        return result;
    }

    protected Set<TaxonDescription> handleTaxonDescriptions(Taxon taxon, Cdm2CdmImportState state)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        taxon = CdmBase.deproxy(taxon);
        Distribution endemismDistribution = getEndemism(taxon, state);

        Function<TaxonDescription,Boolean> filterFunction = null;
        if (state.getConfig().isIgnoreComputedDescriptions()) {
            filterFunction = td->td.getTypes().stream().anyMatch(t->t.isKindOf(DescriptionType.COMPUTED));
        }
        handleCollection(taxon, Taxon.class, "descriptions", TaxonDescription.class, filterFunction, state);
        filterEmptyDescriptions(taxon, state);
        handleEndemism(taxon, endemismDistribution);

        return taxon.getDescriptions();
    }

    private Distribution getEndemism(Taxon taxon, Cdm2CdmImportState state) {
        Distribution endemismDistribution = null;
        if (state.getConfig().getEndemismHandler() != null){
            UUID uuidEndemicArea = state.getConfig().getUuidEndemicRelevantArea();
            NamedArea endemicArea = this.getNamedArea(state, uuidEndemicArea);
            PresenceAbsenceTerm endemic = this.getPresenceTerm(state, PresenceAbsenceTerm.uuidEndemic);
            PresenceAbsenceTerm notEndemic = this.getPresenceTerm(state, PresenceAbsenceTerm.uuidNotEndemic);
            PresenceAbsenceTerm unknownEndemic = this.getPresenceTerm(state, PresenceAbsenceTerm.uuidUnknownEndemism);
            DefinedTermBase<?>[] params = new DefinedTermBase[5];
            params[0] = endemicArea;
            params[1] = endemic;
            params[2] = notEndemic;
            params[3] = unknownEndemic;
            params[4] = this.getFeature(state, Feature.DISTRIBUTION().getUuid());

            DoubleResult<Taxon,DefinedTermBase<?>[]> input = new DoubleResult<>(taxon, params);
            endemismDistribution = state.getConfig().getEndemismHandler().apply(input);
            DescriptionElementSource source = endemismDistribution.addPrimaryTaxonomicSource(getSourceReference(state));
            source.setAccessed(TimePeriod.NewInstance(Calendar.getInstance()));
        }
        return endemismDistribution;
    }

    //#10324
    private void handleEndemism(Taxon taxon, Distribution endemismDistribution) {
        if (endemismDistribution != null) {
            Taxon newTaxon = taxon.getDescriptions().isEmpty()? null: taxon.getDescriptions().iterator().next().getTaxon();
            if (newTaxon == null) {
                newTaxon = CdmBase.deproxy(getTaxonService().find(taxon.getUuid()), Taxon.class);
                if (newTaxon == null) {
                    logger.error("Taxon for endemism does not exist yet at all: " + taxon.getTitleCache());
                }else if (!newTaxon.getName().getRank().isHigherOrEqualTo(RankClass.Genus)) {
                    logger.warn("Taxon has no description yet: " + newTaxon.getName().getTitleCache());
                }
            }else {
                TaxonDescription td = TaxonDescription.NewInstance(newTaxon);
                td.addElement(endemismDistribution);
                td.setTitleCache("Endemism computed from Euro+Med", true);
                taxon.getDescriptions().add(td);
            }
        }
    }

    protected boolean doDescriptions(@SuppressWarnings("unused") Cdm2CdmImportState state) {
        return false;
    }

    protected Synonym handlePersistedSynonym(Synonym synonym, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Synonym result = handlePersisted((TaxonBase)synonym, state);
        //complete
        setInvisible(result, "acceptedTaxon", detach(result.getAcceptedTaxon(), state));
        return result;
    }

    protected TaxonRelationship handlePersistedTaxonRelationship(TaxonRelationship taxRel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonRelationship result = handlePersisted((RelationshipBase)taxRel, state);
        //complete
        result.setFromTaxon(detach(result.getFromTaxon(), state));
        result.setToTaxon(detach(result.getToTaxon(), state));
        result.setType(detach(result.getType(), state));
        return result;
    }

    protected NameRelationship handlePersistedNameRelationship(NameRelationship rel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NameRelationship result = handlePersisted((RelationshipBase)rel, state);
        //complete
        setInvisible(result, "relatedFrom", detach(result.getFromName(), state));
        setInvisible(result, "relatedTo", detach(result.getToName(), state));
//        result.setFromName(detache(result.getFromName(), state));
//        result.setToName(detache(result.getToName(), state));
        result.setType(detach(result.getType(), state));
        return result;
    }

    protected HybridRelationship handlePersistedHybridRelationship(HybridRelationship rel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        HybridRelationship result = handlePersisted((RelationshipBase)rel, state);
        //complete
        setInvisible(result, "relatedFrom", detach(result.getParentName(), state));
        setInvisible(result, "relatedTo", detach(result.getHybridName(), state));
//        result.setFromName(detache(result.getFromName()));
//        result.setToName(detache(result.getToName()));
        result.setType(detach(result.getType(), state));
        return result;
    }

    protected NomenclaturalStatus handlePersistedNomenclaturalStatus(NomenclaturalStatus status, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalStatus result = handlePersisted((SingleSourcedEntityBase)status, state);
        //complete
        result.setType(detach(result.getType(), state));
        setInvisible(result, "name", detach(result.getName(), state));
        return result;
    }

    protected TypeDesignationBase handlePersisted(TypeDesignationBase designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TypeDesignationBase<?> result = handlePersisted((SourcedEntityBase)designation, state);
        //complete
        handleCollection(result, TypeDesignationBase.class, "registrations", Registration.class, state);
        handleCollection(result, TypeDesignationBase.class, "typifiedNames", TaxonName.class, state);
        return result;
    }

    protected <T extends TypeDesignationBase> T handlePersistedNameOrSpecimenTypeDesignation(T designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = (T)handlePersisted(designation, state);
        //complete
        result.setDesignationSource(detach(result.getDesignationSource(), state));
        result.setTypeStatus(detach(result.getTypeStatus(), state));
        return result;
    }

    protected NameTypeDesignation handlePersistedNameTypeDesignation(NameTypeDesignation designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NameTypeDesignation result = handlePersistedNameOrSpecimenTypeDesignation(designation, state);
        //complete
        result.setTypeName(detach(result.getTypeName(), state));
        return result;
    }

    protected SpecimenTypeDesignation handlePersistedSpecimenTypeDesignation(SpecimenTypeDesignation designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        SpecimenTypeDesignation result = handlePersistedNameOrSpecimenTypeDesignation(designation, state);
        //complete
        result.setTypeSpecimen(detach(result.getTypeSpecimen(), state));
        return result;
    }

    protected TextualTypeDesignation handlePersistedTextualTypeDesignation(TextualTypeDesignation designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TextualTypeDesignation result = (TextualTypeDesignation)handlePersisted(designation, state);
        //still to test
        handleMap(result, TextualTypeDesignation.class, "text", Language.class, LanguageString.class, state);
        return result;
    }

    protected InstitutionalMembership handlePersistedInstitutionalMembership(InstitutionalMembership institutionalMembership, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        InstitutionalMembership result = handlePersisted((VersionableEntity)institutionalMembership, state);
        //complete
//        result.setPerson(detache(result.getPerson()));
        setInvisible(result, "person", detach(result.getPerson(), state));
        result.setInstitute(detach(result.getInstitute(), state));
        return result;
    }

    protected Institution handlePersistedInstitution(Institution institution, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Institution result = handlePersisted((AgentBase)institution, state);
        //complete
        result.setIsPartOf(detach(result.getIsPartOf(), state));
        handleCollection(result, Institution.class, "types", DefinedTerm.class, state);
        return result;
    }

    protected TaxonNodeAgentRelation handlePersistedTaxonNodeAgentRelation(TaxonNodeAgentRelation nodeAgentRel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonNodeAgentRelation result = handlePersisted((AnnotatableEntity)nodeAgentRel, state);
        //complete
        result.setAgent(detach(result.getAgent(), state));
        result.setType(detach(result.getType(), state));
        setInvisible(result, "taxonNode", detach(result.getTaxonNode(), state));
        return result;
    }


    protected TaxonName handlePersistedTaxonName(TaxonName taxonName, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        @SuppressWarnings("rawtypes")
        TaxonName result = handlePersisted((IdentifiableEntity)taxonName, state);
        //complete
        result.setRank(detach(result.getRank(), state));
        result.setCombinationAuthorship(detach(result.getCombinationAuthorship(), state));
        result.setExCombinationAuthorship(detach(result.getExCombinationAuthorship(), state));
        result.setBasionymAuthorship(detach(result.getBasionymAuthorship(), state));
        result.setExBasionymAuthorship(detach(result.getExBasionymAuthorship(), state));
        result.setInBasionymAuthorship(detach(result.getInBasionymAuthorship(), state));
        result.setInCombinationAuthorship(detach(result.getInCombinationAuthorship(), state));

//        result.setNomenclaturalReference(detach(result.getNomenclaturalReference(), state));
        result.setNomenclaturalSource(detach(result.getNomenclaturalSource(), state));
        result.setHomotypicalGroup(detach(result.getHomotypicalGroup(), state));
        handleCollection(result, TaxonName.class, "descriptions", TaxonNameDescription.class, state);
        handleCollection(result, TaxonName.class, "hybridChildRelations", HybridRelationship.class, state);
        handleCollection(result, TaxonName.class, "hybridParentRelations", HybridRelationship.class, state);
        handleCollection(result, TaxonName.class, "relationsFromThisName", NameRelationship.class, state);
        handleCollection(result, TaxonName.class, "relationsToThisName", NameRelationship.class, state);
        handleCollection(result, TaxonName.class, "status", NomenclaturalStatus.class, state);

        handleCollection(result, TaxonName.class, "registrations", Registration.class, state);
        handleCollection(result, TaxonName.class, "typeDesignations", TypeDesignationBase.class, state);

        //do not propagate taxa from names
        @SuppressWarnings("rawtypes")
        Function<TaxonBase,Boolean> keepEmpty = tn->{return true;};
        handleCollection(result, TaxonName.class, "taxonBases", TaxonBase.class, keepEmpty, state);

        return result;
    }

    protected HomotypicalGroup handlePersistedHomotypicalGroup(HomotypicalGroup group, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        HomotypicalGroup result = handlePersisted((AnnotatableEntity)group, state);
        //complete
        handleCollection(result, HomotypicalGroup.class, "typifiedNames", TaxonName.class, state);
        return result;
    }

    protected Annotation handlePersistedAnnotation(Annotation annotation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Annotation result = handlePersisted((AnnotatableEntity)annotation, state);
        //complete
        result.setAnnotationType(detach(annotation.getAnnotationType(), state));
        result.setCommentator(detach(result.getCommentator(), state));
        handleCollection(result, Annotation.class, "intextReferences", IntextReference.class, state);
        return result;
    }

    protected Extension handlePersistedExtension(Extension extension, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Extension result = handlePersisted((VersionableEntity)extension, state);
        //complete
        result.setType(detach(extension.getType(), state));
        return result;
    }

    protected Marker handlePersistedMarker(Marker marker, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Marker result = handlePersisted((VersionableEntity)marker, state);
        //complete
        result.setMarkerType(detach(marker.getMarkerType(), state));
        return result;
    }

    protected Media handlePersistedMedia(Media media, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Media result = handlePersisted((IdentifiableEntity<?>)media, state);

        result.setArtist(detach(result.getArtist(), state));
        handleMap(result, Media.class, "description", Language.class, LanguageString.class, state);
        handleMap(result, Media.class, "title", Language.class, LanguageString.class, state);
        result.setLink(detach(result.getLink(), state));
        handleCollection(result, Media.class, "representations", MediaRepresentation.class, state);
        //complete  (mediaCreated is cloned)
        return result;
    }

    protected MediaRepresentation handlePersistedMediaRepresentation(MediaRepresentation mediaRepresentation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        MediaRepresentation result = handlePersisted((VersionableEntity)mediaRepresentation, state);

        handleCollection(result, MediaRepresentation.class, "mediaRepresentationParts", MediaRepresentationPart.class, state);
        setInvisible(result, "media", detach(result.getMedia(), state));
        //complete
        return result;
    }

    protected MediaRepresentationPart handlePersistedMediaRepresentationPart(MediaRepresentationPart part, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        MediaRepresentationPart result = handlePersisted((VersionableEntity)part, state);
        //rep, mediaMetaData
        handleCollection(result, MediaRepresentationPart.class, "mediaMetaData", MediaMetaData.class, state);
        setInvisible(result, MediaRepresentationPart.class, "mediaRepresentation", detach(result.getMediaRepresentation(), state));
        //complete
        return result;
    }

    protected Team handlePersistedTeam(Team team, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Team result = handlePersisted((TeamOrPersonBase)team, state);
        //complete
        handleCollection(result, Team.class, "teamMembers", Person.class, state);
        return result;
    }

    protected Contact handlePersistedContact(Contact contact, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Contact result = contact; // getTarget(contact);
        if (result ==null){
            return result;
        }
        if (!contact.getAddresses().isEmpty() || !contact.getEmailAddresses().isEmpty()
               || !contact.getFaxNumbers().isEmpty() ||!contact.getPhoneNumbers().isEmpty()
               ||!contact.getUrls().isEmpty()){
            logger.warn("Addresses not yet implemented");
        }
        setInvisible(result, "addresses", new HashSet<>());
//        handleCollection(result, Contact.class, "", Address.class);
        setInvisible(result, "faxNumbers", new ArrayList<>());
        setInvisible(result, "phoneNumbers", new ArrayList<>());
        setInvisible(result, "emailAddresses", new ArrayList<>());
        setInvisible(result, "urls", new ArrayList<>());
        return result;
    }

    protected Person handlePersistedPerson(Person person, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Person result = handlePersisted((TeamOrPersonBase)person, state);
        //complete
        handleCollection(result, Person.class, "institutionalMemberships", InstitutionalMembership.class, state);
        return result;
    }

    protected NamedArea handlePersistedNamedArea(NamedArea area, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedArea result = handlePersisted((DefinedTermBase)area, state);
        //complete
        handleCollection(result, NamedArea.class, "countries", Country.class, state);
        result.setLevel(detach(result.getLevel(), state));
        result.setType(detach(result.getType(), state));
        result.setShape(detach(result.getShape(), state));
        return result;
    }

    protected NamedAreaLevel handlePersistedNamedAreaLevel(NamedAreaLevel areaLevel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedAreaLevel result = handlePersisted((DefinedTermBase)areaLevel, state);
        //complete
        return result;
    }

    protected NamedAreaType handlePersistedNamedAreaType(NamedAreaType areaType, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedAreaType result = handlePersisted((DefinedTermBase)areaType, state);
        //complete
        return result;
    }

    protected IdentifierType handlePersistedIdentifierType(IdentifierType identifierType, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IdentifierType result = handlePersisted((DefinedTermBase)identifierType, state);
        //complete
        return result;
    }

    protected AnnotationType handlePersistedAnnotationType(AnnotationType annotationType, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        AnnotationType result = handlePersisted((AvailableForIdentifiableBase<?>)annotationType, state);
        //complete
        return result;
    }

    protected Language handlePersistedLanguage(Language language, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Language result = handlePersisted((DefinedTermBase)language, state);
        //complete
        return result;
    }

    protected Rank handlePersistedRank(Rank language, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Rank result = handlePersisted((DefinedTermBase)language, state);
        //complete
        return result;
    }

    protected Classification handlePersistedClassification(Classification classification, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Classification result = handlePersisted((IdentifiableEntity)classification, state);
        //complete
        result.setName(detach(classification.getName(), state));
        result.setSource(detach(classification.getSource(), state));
        result.setRootNode(detach(classification.getRootNode(), state));
        handleCollection(result, Classification.class, "geoScopes", NamedArea.class, state);
        handleMap(result, Classification.class, "description", Language.class, LanguageString.class, state);
        return result;
    }

    protected Reference handlePersistedReference(Reference reference, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Reference result = handlePersisted((IdentifiableMediaEntity)reference, state);
        result.setAuthorship(detach(result.getAuthorship(), state));
        result.setInstitution(detach(result.getInstitution(), state));
        result.setSchool(detach(result.getSchool(), state));
        result.setInReference(detach(result.getInReference(), state));
        return result;
    }

    protected SpecimenOrObservationBase<?> handlePersistedSpecimenOrObservationBase(SpecimenOrObservationBase specimen, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        //TODO implement for classes
        SpecimenOrObservationBase<?> result = handlePersisted((IdentifiableEntity)specimen, state);
        //complete
        result.setSex(detach(result.getSex(), state));
        result.setLifeStage(detach(result.getLifeStage(), state));
        result.setKindOfUnit(detach(result.getKindOfUnit(), state));
        handleCollection(result, SpecimenOrObservationBase.class, "determinations", DeterminationEvent.class, state);
        handleCollection(result, SpecimenOrObservationBase.class, "descriptions", SpecimenDescription.class, state);
        handleCollection(result, SpecimenOrObservationBase.class, "derivationEvents", DerivationEvent.class, state);
        handleMap(result, SpecimenOrObservationBase.class, "definition", Language.class, LanguageString.class, state);
        return result;
    }

    protected IdentifiableSource handlePersistedIdentifiableSource(IdentifiableSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IdentifiableSource result = handlePersisted((OriginalSourceBase)source, state);
        //complete
        return result;
    }

    protected <T extends NamedSourceBase> T handlePersisted(NamedSourceBase source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((OriginalSourceBase)source, state);
        //complete
        result.setNameUsedInSource(detach(result.getNameUsedInSource(), state));
        return result;
    }

    protected NamedSource handlePersistedNamedSource(NamedSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedSource result = handlePersisted((NamedSourceBase)source, state);
        //complete
        return result;
    }

    protected NomenclaturalSource handlePersistedNomenclaturalSource(NomenclaturalSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalSource result = handlePersisted((NamedSourceBase)source, state);
        //complete
        result.setSourcedName(detach(result.getSourcedName(), state));
        return result;
    }

    protected SecundumSource handlePersistedSecundumSource(SecundumSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        SecundumSource result = handlePersisted((NamedSourceBase)source, state);
        //complete
        result.setSourcedTaxon(detach(result.getSourcedTaxon(), state));
        return result;
    }

    protected DescriptionElementSource handlePersistedDescriptionElementSource(DescriptionElementSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DescriptionElementSource result = handlePersisted((NamedSourceBase)source, state);
        //complete
        DescriptionElementBase deb = detach(result.getSourcedElement(), state);
        deb.addSource(result);
        return result;
    }

    protected <T extends CommonTaxonName> T handlePersistedCommonTaxonName(CommonTaxonName element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setLanguage(detach(result.getLanguage(), state));
        result.setArea(detach(result.getArea(), state));
        return result;
    }

    protected <T extends TextData> T handlePersistedTextData(TextData element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setFormat(detach(result.getFormat(), state));
        handleMap(result, TextData.class, "multilanguageText", Language.class, LanguageString.class, state);
        return result;
    }

    protected <T extends Distribution> T handlePersistedDistribution(Distribution element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setArea(detach(result.getArea(), state));
        result.setStatus(detach(result.getStatus(), state));
        return result;
    }

    protected <T extends TaxonInteraction> T handlePersistedTaxonInteraction(TaxonInteraction element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        handleMap(result, TaxonInteraction.class, "description", Language.class, LanguageString.class, state);
        result.setTaxon2(detach(result.getTaxon2(), state));
        return result;
    }

    protected ExternalLink handlePersistedExternalLink(ExternalLink externalLink, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        ExternalLink result = handlePersisted((VersionableEntity)externalLink, state);
        //complete
        handleMap(result, ExternalLink.class, "description", Language.class, LanguageString.class, state);
        return result;
    }

    protected Identifier handlePersistedIdentifier(Identifier identifier, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Identifier result = handlePersisted((AnnotatableEntity)identifier, state);
        //complete
        result.setType(detach(result.getType(), state));
        return result;
    }

    protected Character handlePersistedCharacter(Character term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Character result = (Character)handlePersistedFeature(term, state);
        result.setStructure(detach(term.getStructure(), state));
        result.setProperty(detach(term.getProperty(), state));
        result.setRatioToStructure(detach(term.getRatioToStructure(), state));
        result.setStructureModifier(detach(term.getStructureModifier(), state));
        result.setPropertyModifier(detach(term.getPropertyModifier(), state));

        return result;
    }

    protected Feature handlePersistedFeature(Feature term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Feature result = handlePersisted((AvailableForTermBase<?>)term, state);
        //complete
        handleCollection(result, Feature.class, "inverseRepresentations", Representation.class, state);
        handleCollection(result, Feature.class, "recommendedMeasurementUnits", MeasurementUnit.class, state);
        handleCollection(result, Feature.class, "recommendedModifierEnumeration", TermVocabulary.class, state);
        handleCollection(result, Feature.class, "recommendedStatisticalMeasures", StatisticalMeasure.class, state);
        handleCollection(result, Feature.class, "supportedCategoricalEnumerations", TermVocabulary.class, state);
        return result;
    }

    protected State handlePersistedState(State term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        State result = handlePersisted((DefinedTermBase<?>)term, state);
        //complete
        return result;
    }

    protected ExtensionType handlePersistedExtensionType(ExtensionType term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        ExtensionType result = handlePersisted((DefinedTermBase)term, state);
        //complete
        return result;
    }

    protected MarkerType handlePersistedMarkerType(MarkerType term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        MarkerType result = handlePersisted((DefinedTermBase)term, state);
        //complete
        return result;
    }

    protected NomenclaturalStatusType handlePersistedNomenclaturalStatusType(NomenclaturalStatusType term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalStatusType result = handlePersisted((DefinedTermBase)term, state);
        //complete
        return result;
    }

    protected DefinedTerm handlePersistedDefinedTerm(DefinedTerm term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DefinedTerm result = handlePersisted((DefinedTermBase)term, state);
        //complete
        return result;
    }

    //placeholder for not implemented methods for subclasses
    protected DefinedTermBase<?> handlePersistedTerm(DefinedTermBase term, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DefinedTermBase<?> result = handlePersisted(term, state);
        logger.warn("Class not yet handled: " + term.getClass().getSimpleName());
        return result;
    }

    protected TermVocabulary<?> handlePersistedVocabulary(TermVocabulary voc, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TermVocabulary<?> result = (TermVocabulary<?>)handlePersisted((TermCollection)voc, state);
        if (!state.getConfig().isPartialVocabulariesForGraphs() || !state.isGraph()) {
            handleCollection(result, TermVocabulary.class, "terms", DefinedTermBase.class, state);
        }
        return result;
    }

    protected TermTree<?> handlePersistedTermTree(TermTree tree, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        //TODO TermGraphBase is still missing
        TermTree<?> result = (TermTree<?>)handlePersisted((TermCollection)tree, state);
        //complete
        result.getRoot().setUuid(tree.getRoot().getUuid());
        return result;
    }

    protected TermNode<?> handlePersistedTermNode(TermNode node, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TermNode<?> result = (TermNode<?>)handlePersisted((TermRelationBase)node, state);
        //complete
        setInvisible(result, "parent", detach(result.getParent(), state));
        handleCollection(result, TermNode.class, "inapplicableIf", FeatureState.class, state);
        handleCollection(result, TermNode.class, "onlyApplicableIf", FeatureState.class, state);
        handleCollection(result, TermNode.class, "children", TermNode.class, state);

        return result;
    }

    protected Representation handlePersistedRepresentation(Representation representation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Representation result = (Representation)handlePersisted((LanguageStringBase)representation, state);
        return result;
    }

    protected <T extends TermBase> T handlePersisted(TermBase termBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)termBase, state);
        //complete
        handleCollection(result, TermBase.class, "representations", Representation.class, state);
        handleExternallyManaged(result, state);
        return result;
    }

    protected <T extends TermCollection> T handlePersisted(TermCollection termCollection, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((TermBase)termCollection, state);
        //complete
        handleCollection(result, TermCollection.class, "termRelations", TermRelationBase.class, state);
        return result;
    }

    protected <T extends TermRelationBase> T handlePersisted(TermRelationBase termRelationBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)termRelationBase, state);
        result.setTerm(detach(result.getTerm(), state));
        setInvisible(result, TermRelationBase.class, "graph", detach(result.getGraph(), state));
        return result;
    }

    protected User handlePersistedUser(User user, Cdm2CdmImportState state) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        User result = (User)handlePersistedCdmBase(user, state);
        if (result.getUsername().equals("admin")){
            //TODO why only admin, is this not a problem for all duplicated usernames? Was this a preliminary decision?
            result = getUserService().listByUsername("admin", MatchMode.EXACT, null, null, null, null, null).iterator().next();
            state.putPermanent(user.getUuid(), result);
            cache(result, state); //necessary?
            state.addToSave(result);
            state.removeToSave(user);
        }
        if (!result.isPersisted()){
            result.setAuthorities(new HashSet<>());
            result.setGrantedAuthorities(new HashSet<>());
            setInvisible(result, "groups", new HashSet<>());
        }
        result.setPerson(detach(user.getPerson(), state));
        return result;
    }

    protected LanguageString handlePersistedLanguageString(LanguageString languageString, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        LanguageString result = handlePersisted((LanguageStringBase)languageString, state);
        //complete
        handleCollection(result, LanguageString.class, "intextReferences", IntextReference.class, state);
        return result;
    }

    protected Credit handlePersistedCredit(Credit credit, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Credit result = handlePersisted((LanguageStringBase)credit, state);
        //complete
        result.setAgent(detach(credit.getAgent(), state));
        return result;
    }


    protected Rights handlePersistedRights(Rights rights, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Rights result = handlePersisted((LanguageStringBase)rights, state);
        result.setAgent(detach(rights.getAgent(), state));
        result.setType(detach(rights.getType(), state));
        //complete
        return result;
    }

    protected IntextReference handlePersistedIntextReference(IntextReference intextReference, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IntextReference result = handlePersisted((VersionableEntity)intextReference, state);
        result.setReferencedEntity(detach(result.getReferencedEntity(), false, state));
        Method targetMethod = IntextReference.class.getDeclaredMethod("setTarget", IIntextReferenceTarget.class);
        targetMethod.setAccessible(true);
        targetMethod.invoke(result, detach(result.getTarget(), false, state));
        return result;
    }

    protected <T extends TaxonDescription> T handlePersistedTaxonDescription(TaxonDescription taxDescription, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase<?>)taxDescription, state);
        //complete
        setInvisible(taxDescription, "taxon", detach(taxDescription.getTaxon(), state));
        handleCollection(taxDescription, TaxonDescription.class, "geoScopes", NamedArea.class, state);
        handleCollection(taxDescription, TaxonDescription.class, "scopes", DefinedTerm.class, state);
        return result;
    }

    protected <T extends TaxonNameDescription> T handlePersistedTaxonNameDescription(TaxonNameDescription nameDescription, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase)nameDescription, state);
        //complete
        setInvisible(nameDescription, "taxonName", detach(nameDescription.getTaxonName(), state));
        return result;
    }

// ***************************** BASE CLASSES ********************************************/

    protected <T extends CdmBase> T handlePersistedCdmBase(CdmBase cdmBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        @SuppressWarnings("unchecked")
        T result = (T)getTarget(cdmBase, state);
        //complete
        result.setCreatedBy(makeCreatedUpdatedBy(cdmBase.getCreatedBy(), state, false));
        result.setCreated(makeCreatedUpdatedWhen(cdmBase.getCreated(), state, false));
        return result;
    }

    protected <T extends VersionableEntity> T handlePersisted(VersionableEntity entity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        @SuppressWarnings("unchecked")
        T result = (T)handlePersistedCdmBase((CdmBase)entity, state);
        //complete
        result.setUpdatedBy(makeCreatedUpdatedBy(entity.getUpdatedBy(), state, true));
        result.setUpdated(makeCreatedUpdatedWhen(entity.getUpdated(), state, false));

        return result;
    }

    protected <T extends AnnotatableEntity> T handlePersisted(AnnotatableEntity entity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((VersionableEntity)entity, state);
        //complete
        handleCollection(result, AnnotatableEntity.class, "annotations", Annotation.class, state);
        handleCollection(result, AnnotatableEntity.class, "markers", Marker.class, state);
        return result;
    }

    private <SOURCE extends OriginalSourceBase> Function<SOURCE,Boolean> getImportSourceFilter(){
        return (s)->s.getType() == OriginalSourceType.Import;
    }

    protected <T extends SourcedEntityBase<?>> T handlePersisted(
            @SuppressWarnings("rawtypes") SourcedEntityBase sourcedEntity,
            Cdm2CdmImportState state)
            throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        int originalId = sourcedEntity.getId();
        T result = handlePersisted((AnnotatableEntity)sourcedEntity, state);
        //complete
        //sources
        Function<OriginalSourceBase,Boolean> filterFunction = state.getConfig().isRemoveImportSources()? getImportSourceFilter() : null;

        handleCollection(result, SourcedEntityBase.class, "sources", OriginalSourceBase.class, filterFunction, state);
        if (!result.isPersisted()){
            //add current import source
            if (state.getConfig().isAddSources()){
                Reference sourceRef = getSourceReference(state);
                OriginalSourceBase newSource = result.addImportSource(String.valueOf(originalId), sourcedEntity.getClass().getSimpleName(),
                        sourceRef, null);
                getCommonService().save(newSource);
                addExistingObject(newSource, state);
            }
        }
        return result;
    }

    private void handleExternallyManaged(TermBase result, Cdm2CdmImportState state) {
        if (state.getConfig().isExternallyManaged()) {
            ExternallyManaged externallyManaged = new ExternallyManaged();
            externallyManaged.setAuthorityType(AuthorityType.EXTERN);
            externallyManaged.setExternalId(result.getUuid().toString());
            String subdomain = result.isInstanceOf(DefinedTermBase.class )? "term/":
                    result.isInstanceOf(TermVocabulary.class) ? "voc/" :
                        "list/";
            externallyManaged.setExternalLink(URI.create("https://terms.cybertaxonomy.org/"+ subdomain + result.getUuid() ));  //TODO
            externallyManaged.setImportMethod(ExternallyManagedImport.CDM_TERMS);
            externallyManaged.setLastRetrieved(DateTime.now());
            result.setExternallyManaged(externallyManaged);
        }
    }

    protected <T extends IdentifiableEntity> T handlePersisted(
            @SuppressWarnings("rawtypes") IdentifiableEntity identifiableEntity,
            Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        T result = handlePersisted((SourcedEntityBase<?>)identifiableEntity, state);
        //complete
        handleCollection(result, IdentifiableEntity.class, "credits", Credit.class, state);
        handleCollection(result, IdentifiableEntity.class, "extensions", Extension.class, state);
        handleCollection(result, IdentifiableEntity.class, "identifiers", Identifier.class, state);
        handleCollection(result, IdentifiableEntity.class, "rights", Rights.class, state);
        handleCollection(result, IdentifiableEntity.class, "links", ExternalLink.class, state);

        return result;
    }

    protected <T extends DefinedTermBase> T handlePersisted(
            @SuppressWarnings("rawtypes") DefinedTermBase definedTermBase,
            Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        T result = handlePersisted((TermBase)definedTermBase, state);
        //complete
        handleCollection(result, DefinedTermBase.class, "media", Media.class, state);
        handleCollection(result, DefinedTermBase.class, "generalizationOf", DefinedTermBase.class, state);
        handleCollection(result, DefinedTermBase.class, "includes", DefinedTermBase.class, state);
        result.setKindOf(detach(result.getKindOf(), state));
        result.setPartOf(detach(result.getPartOf(), state));
        setInvisible(result, DefinedTermBase.class, "vocabulary", detach(result.getVocabulary(), state));

        return result;
    }

    protected <T extends AvailableForTermBase> T handlePersisted(
            @SuppressWarnings("rawtypes") AvailableForTermBase availableForTermBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DefinedTermBase)availableForTermBase, state);
        //complete
        return result;
    }

    protected <T extends AvailableForIdentifiableBase<?>> T handlePersisted(AvailableForIdentifiableBase availableForTermBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AvailableForTermBase<?>)availableForTermBase, state);
        //complete
        return result;
    }

    protected <T extends OriginalSourceBase> T handlePersisted(OriginalSourceBase source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)source, state);
        //complete
        result.setCitation(detach(result.getCitation(), state));
        handleCollection(result, OriginalSourceBase.class, "links", ExternalLink.class, state);
        return result;
    }

    protected <T extends LanguageStringBase> T handlePersisted(LanguageStringBase lsBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)lsBase, state);
        //complete
        result.setLanguage(detach(lsBase.getLanguage(), state));
        return result;
    }

    protected <T extends TeamOrPersonBase> T handlePersisted(TeamOrPersonBase teamOrPerson, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AgentBase)teamOrPerson, state);
        //complete
        return result;
    }

    protected <T extends AgentBase> T handlePersisted(AgentBase agent, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableMediaEntity)agent, state);
        result.setContact(detach(result.getContact(), state));
        //complete
        return result;
    }

    protected <T extends TaxonBase> T handlePersisted(TaxonBase taxonBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)taxonBase, state);
        //complete
        result.setName(detach(taxonBase.getName(), state));
        result.setSecSource(detach(taxonBase.getSecSource(), state));
        return result;
    }

    protected <T extends IdentifiableMediaEntity> T handlePersisted(IdentifiableMediaEntity mediaEntity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)mediaEntity, state);
        //complete
        handleCollection(result, IdentifiableMediaEntity.class, "media", Media.class, state);
        return result;
    }

    protected <T extends SingleSourcedEntityBase> T handlePersisted(SingleSourcedEntityBase referencedEntity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)referencedEntity, state);
        //complete
        result.setSource(detach(result.getSource(), state));
        return result;
    }

    protected <T extends DescriptionBase> T handlePersisted(DescriptionBase descriptionBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity<?>)descriptionBase, state);
        //complete
        //elements
        Function<DescriptionElementBase,Boolean> filterFunction = null;
        if (state.getConfig().hasCommonNameLanguageFilter() || state.getConfig().hasDistributionFilterFromAreaFilter()) {
            //TODO move to state as computed set
            List<LogicFilter<NamedArea>> areaFilter = state.getConfig().getTaxonNodeFilter().getAreaFilter();
            Set<UUID> areaUuids = areaFilter == null ? new HashSet<>() : areaFilter.stream().map(af->af.getUuid()).collect(Collectors.toSet());
            filterFunction = deb->this.toBeFilteredFact(deb, areaUuids, state);
        }
        handleCollection(result, DescriptionBase.class, "descriptionElements", DescriptionElementBase.class, filterFunction, state);
        //others
        handleCollection(result, DescriptionBase.class, "descriptiveDataSets", DescriptiveDataSet.class, state);
        handleCollection(result, DescriptionBase.class, "descriptionSources", Reference.class, state);
        result.setDescribedSpecimenOrObservation(detach(descriptionBase.getDescribedSpecimenOrObservation(), state));
        return result;
    }

    @SuppressWarnings("unchecked")
    private void filterEmptyDescriptions(@SuppressWarnings("rawtypes") IDescribable describable, Cdm2CdmImportState state) {

        Set<DescriptionBase<?>> toRemove = new HashSet<>();
        //select candidates to remove
        for (DescriptionBase<?> db : ((IDescribable<?>)describable).getDescriptions()) {
            if (db.getElements().isEmpty()) {
                toRemove.add(db);
            }
        }
        //remove
        for (DescriptionBase<?> db : toRemove) {
            describable.removeDescription(db);
        }
    }

    private boolean toBeFilteredFact(DescriptionElementBase deb, Set<UUID> distrAreaUuids,
            Cdm2CdmImportState state) {

        //distributions
        if (state.getConfig().isDistributionFilterFromAreaFilter()
                && deb.isInstanceOf(Distribution.class)) {
            //TODO do also by feature
            Distribution distribution = CdmBase.deproxy(deb, Distribution.class);
            if (distribution.getArea() == null || !distrAreaUuids.contains(distribution.getArea().getUuid())){
                return true;
            }
        }

        //common names
        if (!CdmUtils.isNullSafeEmpty(state.getConfig().getCommonNameLanguageFilter())
                && deb.isInstanceOf(CommonTaxonName.class)){
            CommonTaxonName ctn = CdmBase.deproxy(deb, CommonTaxonName.class);
            if (ctn.getLanguage()== null || !state.getConfig().getCommonNameLanguageFilter().contains(ctn.getLanguage().getUuid()) ){
                return true;
            }
        }
        return false;
    }

    protected <T extends DescriptionElementBase> T handlePersisted(DescriptionElementBase element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)element, state);
        //complete
        result.setFeature(detach(result.getFeature(), state));
        setInvisible(result, DescriptionElementBase.class, "inDescription", detach(result.getInDescription(), state));
        Function<DescriptionElementSource,Boolean> filterFunction = state.getConfig().isRemoveImportSources()? getImportSourceFilter() : null;
        handleCollection(result, DescriptionElementBase.class, "sources", DescriptionElementSource.class, filterFunction, state);
        handleCollection(result, DescriptionElementBase.class, "media", Media.class, state);
        handleCollection(result, DescriptionElementBase.class, "modifiers", DefinedTerm.class, state);
        handleMap(result, DescriptionElementBase.class, "modifyingText", Language.class, LanguageString.class, state);

        return result;
    }

    protected <T extends RelationshipBase> T handlePersisted(RelationshipBase relBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((SingleSourcedEntityBase)relBase, state);
        return result;
    }

//************************** COLLECTIONS / MAPS ****************************************/

    protected <HOLDER extends CdmBase, ITEM extends CdmBase> void handleCollection(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter, Class<ITEM> itemClass,
            Cdm2CdmImportState state)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Function<ITEM, Boolean> filterFunction = null;
        handleCollection(holder, declaringClass, parameter, itemClass, filterFunction, state);
    }

    protected <HOLDER extends CdmBase, ITEM extends CdmBase> void handleCollection(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter, Class<ITEM> itemClass,
            Function<ITEM,Boolean> filterFunction, Cdm2CdmImportState state)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Collection<ITEM> oldCollection = setNewCollection(holder, declaringClass, parameter, itemClass);
        Collection<ITEM> newCollection = getTargetCollection(oldCollection, filterFunction, state);
        Field field = declaringClass.getDeclaredField(parameter);
        field.setAccessible(true);
        field.set(holder, newCollection);
    }

    protected <HOLDER extends CdmBase, KEY extends CdmBase, ITEM extends CdmBase>
            void handleMap(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter,
            Class<KEY> keyClass, Class<ITEM> itemClass, Cdm2CdmImportState state)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        //TODO we do not need to set the new map 2x
        Map<KEY,ITEM> oldMap = setNewMap(holder, declaringClass, parameter, keyClass, itemClass);
        Map<KEY,ITEM> newMap = getTargetMap(oldMap, state);
        Field field = declaringClass.getDeclaredField(parameter);
        field.setAccessible(true);
        field.set(holder, newMap);
    }

    protected <T extends CdmBase> Collection<T> setNewCollection(CdmBase obj, Class<?> holderClass,
            String parameter, Class<T> entityClass) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        Field field = holderClass.getDeclaredField(parameter);
        field.setAccessible(true);
        Collection<T> oldValue = (Collection<T>)field.get(obj);
        Collection<T> newValue = null;
        if (Set.class.isAssignableFrom(field.getType())){
            newValue = new HashSet<>();
        }else if (List.class.isAssignableFrom(field.getType())){
            newValue = new ArrayList<>();
        }else{
            throw new RuntimeException("Unsupported collection type: " + field.getType().getCanonicalName());
        }
        field.set(obj, newValue);
        return oldValue;
    }

    protected <KEY extends CdmBase, ITEM extends CdmBase> Map<KEY,ITEM> setNewMap(CdmBase obj, Class<?> holderClass,
            String parameter, Class<KEY> keyClass, Class<ITEM> itemClass) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = holderClass.getDeclaredField(parameter);
        field.setAccessible(true);
        Map<KEY,ITEM> oldValue = (Map<KEY,ITEM>)field.get(obj);
        Map<KEY,ITEM> newValue = null;
        if (Map.class.isAssignableFrom(field.getType())){
            newValue = new HashMap<>();
        }else{
            throw new RuntimeException("Unsupported map type: " + field.getType().getCanonicalName());
        }
        field.set(obj, newValue);
        return oldValue;
    }


    private <T extends Collection<S>, S extends CdmBase> Collection<S> getTargetCollection(
            T sourceCollection, Function<S,Boolean> filterFunction, Cdm2CdmImportState state
            ) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        Collection<S> result;
        if (Set.class.isAssignableFrom(sourceCollection.getClass())){
            result = new HashSet<>();
        }else {
            result = new ArrayList<>();
        }
        for (S entity : sourceCollection){
            if (filterFunction != null && filterFunction.apply(entity)) {
                continue;
            }else {
                S target = detach(entity, state);
                result.add(target);
            }
        }
        return result;
    }

    private <K extends CdmBase, V extends CdmBase> Map<K,V> getTargetMap(Map<K,V> sourceMap, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Map<K,V> result = new HashMap<>();
        for (K key : sourceMap.keySet()){
            K targetKey = detach(key, state);
            V targetValue = detach(sourceMap.get(key), state);
            result.put(targetKey, targetValue);
        }
        return result;
    }

// ****************************** USER HANDLING

    private User makeCreatedUpdatedBy(User createdByOriginal, Cdm2CdmImportState state, boolean isUpdatedBy) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        UserImportMode mode = isUpdatedBy? state.getConfig().getUpdatedByMode() : state.getConfig().getCreatedByMode();

        switch (mode) {
        case NONE:
            return null;
        case ORIGINAL:
            return detach(createdByOriginal, state);
        default:
            logger.warn("Mode not yet supported: " + mode);
            return null;
        }
    }

    private DateTime makeCreatedUpdatedWhen(DateTime createdOriginal, Cdm2CdmImportState state, boolean isUpdatedBy) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        CreatedUpdatedMode mode = isUpdatedBy? state.getConfig().getUpdatedMode() : state.getConfig().getCreatedMode();

        switch (mode) {
        case NONE:
            return null;
        case ORIGINAL:
            return createdOriginal;
        default:
            logger.warn("Mode not yet supported: " + mode);
            return null;
        }
    }


// ****************************** INVISIBLE **************************************/

    protected void setInvisible(Object holder, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setInvisible(holder, holder.getClass(), fieldName, value);
    }
    protected void setInvisible(Object holder, Class<?> holderClazz, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = holderClazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(holder, value);
    }

// ************************* GET TARGET ******************************************/


    //TODO this should be cached for partition
    private <T extends CdmBase> T getTarget(T source, Cdm2CdmImportState state) {
        if (source == null){
            return null;
        }
        T result = getCached(source, state);
//        if (result == null){
//            Class<T> clazz = (Class<T>)source.getClass();
//            result = getCommonService().find(clazz, source.getUuid());
//        }
        if (result == null){
            //Alternative: clone?
            result = CdmBase.deproxy(source);
            result.setId(0);
            cache(result, state);
            state.addToSave(result);
        }
        return result;
    }

// ******************* CACHE *******************************************************/

    protected void cache(CdmBase cdmBase, Cdm2CdmImportState state) {
       if (cdmBase instanceof User || cdmBase instanceof DefinedTermBase){
           state.putPermanent(cdmBase.getUuid(), cdmBase);
       }else{
           state.putToSessionCache(cdmBase);
       }
       addExistingObject(cdmBase, state);
    }

    private void addExistingObject(CdmBase cdmBase, Cdm2CdmImportState state) {
        cdmBase = CdmBase.deproxy(cdmBase);
        Set<UUID> set = state.getExistingObjects(cdmBase.getClass());
        if (set == null){
            set = loadExistingUuids(cdmBase.getClass(), state);
//            set = new HashSet<>();
//            existingObjects.put(cdmBase.getClass(), set);
        }
        set.add(cdmBase.getUuid());
    }

    protected boolean isInCache(CdmBase cdmBase, Cdm2CdmImportState state) {
        return getCached(cdmBase, state) != null;
    }

    protected <T extends CdmBase> T getCached(T cdmBase, Cdm2CdmImportState state) {
        T result = (T)state.getFromSessionCache(cdmBase.getUuid());
        if (result == null){
            result = (T)state.getPermanent(cdmBase.getUuid());
        }
        return result;
    }

    protected void clearCache(Cdm2CdmImportState state) {
        state.clearSessionCache();
    }

    private Reference getSourceReference(Cdm2CdmImportState state) {
        UUID uuid = state.getConfig().getSourceRefUuid();
        if (uuid == null && state.getConfig().getSourceReference() != null){
            uuid = state.getConfig().getSourceReference().getUuid();
            state.getConfig().setSourceRefUuid(uuid);
        }
        Reference result = (Reference)state.getFromSessionCache(uuid);
        if (result == null){
            result = (Reference)state.getPermanent(uuid);
        }
        if (result == null){
            result = getReferenceService().find(uuid);
            if (result != null) {
                state.putToSessionCache(result);
            }
        }

        if (result == null){
            result = state.getConfig().getSourceReference();
            if (result == null){
                result = ReferenceFactory.newDatabase();
                //TODO
                result.setTitle("Cdm2Cdm Import");
            }
            getReferenceService().save(result);
            state.putToSessionCache(result);
        }
        return result;
    }


    protected ITaxonNodeOutStreamPartitioner getTaxonNodePartitioner(Cdm2CdmImportState state, IProgressMonitor monitor,
            Cdm2CdmImportConfigurator config) {
        ITaxonNodeOutStreamPartitioner partitioner = config.getPartitioner();
        if (partitioner == null){
            if(!config.isConcurrent()){
                partitioner = TaxonNodeOutStreamPartitioner.NewInstance(sourceRepo(state), state,
                        state.getConfig().getTaxonNodeFilter(), 100,
                        monitor, 1, TaxonNodeOutStreamPartitioner.fullPropertyPaths);
                ((TaxonNodeOutStreamPartitioner)partitioner).setLastCommitManually(true);
            }else{
                partitioner = TaxonNodeOutStreamPartitionerConcurrent
                        .NewInstance(state.getConfig().getSource(), state.getConfig().getTaxonNodeFilter(),
                                1000, monitor, 1, TaxonNodeOutStreamPartitioner.fullPropertyPaths);
            }
        }
        return partitioner;
    }

}
