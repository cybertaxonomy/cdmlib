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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.ICdmImportSource;
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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
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
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.TaxonName;
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
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
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
    private static final Logger logger = Logger.getLogger(Cdm2CdmImportBase.class);

    //quick and dirty
    private Cdm2CdmImportState stateX;

    protected ICdmRepository sourceRepo(Cdm2CdmImportState state){
        ICdmRepository repo = state.getSourceRepository();
        if (repo == null){
            ICdmImportSource source = state.getConfig().getSource();
            if (source instanceof ICdmRepository){
                repo = (ICdmRepository)source;
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

    protected  Contact detache(Contact contact, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        contact = CdmBase.deproxy(contact);
        if (contact == null){
            return contact;
        }else{
            return handlePersistedContact(contact, state);
        }
    }

    protected  IIntextReferencable detache(IIntextReferencable cdmBase, boolean onlyForDefinedSignature, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferencable)detache((CdmBase)cdmBase, state);
    }
    protected  IIntextReferenceTarget detache(IIntextReferenceTarget cdmBase, boolean onlyForDefinedSignature, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferenceTarget)detache((CdmBase)cdmBase, state);
    }

    protected <T extends CdmBase> T detache(T cdmBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return detache(cdmBase, false, state);
    }

    protected <T extends CdmBase> T detache(T cdmBase, boolean notFromSource, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
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
                T existingObj = getCommonService().find(clazz, cdmBase.getUuid());
                if (existingObj != null){
                    cache(existingObj, state);
                    return existingObj;
                }else{
                    logger.warn("Object should exist already but does not exist in target. This should not happen: " + cdmBase.getClass().getSimpleName() + "/" + cdmBase.getUuid());
                }
            }
        }
        if ( !cdmBase.isPersited()){
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
        }else if(cdmBase instanceof CommonTaxonName){
            return handlePersistedCommonTaxonName((CommonTaxonName)cdmBase, state);
        }else if(cdmBase instanceof Distribution){
            return handlePersistedDistribution((Distribution)cdmBase, state);
        }else if(cdmBase instanceof TextData){
            return handlePersistedTextData((TextData)cdmBase, state);
        }else if(cdmBase instanceof HomotypicalGroup){
            return handlePersistedHomotypicalGroup((HomotypicalGroup)cdmBase, state);
        }else if(cdmBase instanceof TypeDesignationBase){
            return handlePersistedTypeDesignationBase((TypeDesignationBase)cdmBase, state);
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
        }else if(cdmBase instanceof Rights){
            return handlePersistedRights((Rights)cdmBase, state);
        }else if(cdmBase instanceof DefinedTerm){
            return handlePersistedDefinedTerm((DefinedTerm)cdmBase, state);
        }else if(cdmBase instanceof DefinedTermBase){
            return handlePersistedTerm((DefinedTermBase<?>)cdmBase, state);
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
        result.setTaxon(detache(result.getTaxon(), state));
        result.setCitation(detache(node.getReference(), state));
        result.setSynonymToBeUsed(detache(result.getSynonymToBeUsed(), state));
        handleMap(result, TaxonNode.class, "excludedNote", Language.class, LanguageString.class, state);
        //classification, parent, children
        this.setInvisible(node, "classification", detache(node.getClassification(), state));
        handleParentTaxonNode(result, state);
        setNewCollection(node, TaxonNode.class, "childNodes", TaxonNode.class);
        return result;
    }

    private void handleParentTaxonNode(TaxonNode childNode, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonNode parent = detache(childNode.getParent(), true, state);
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
        Taxon result = handlePersisted((TaxonBase)taxon, state);
        //complete
        handleCollection(result, Taxon.class, "synonyms", Synonym.class, state);
//        handleCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        setNewCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        handleCollection(result, Taxon.class, "relationsFromThisTaxon", TaxonRelationship.class, state);
        handleCollection(result, Taxon.class, "relationsToThisTaxon", TaxonRelationship.class, state);
        if (this.doDescriptions(state)){
            handleCollection(result, Taxon.class, "descriptions", TaxonDescription.class, state);
        }else{
            setNewCollection(result, Taxon.class, "descriptions", TaxonDescription.class);
        }
        return result;
    }

    protected boolean doDescriptions(Cdm2CdmImportState state) {
        return false;
    }

    protected Synonym handlePersistedSynonym(Synonym synonym, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Synonym result = handlePersisted((TaxonBase)synonym, state);
        //complete
        setInvisible(result, "acceptedTaxon", detache(result.getAcceptedTaxon(), state));
        result.setType(detache(result.getType(), state));
        return result;
    }

    protected TaxonRelationship handlePersistedTaxonRelationship(TaxonRelationship taxRel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonRelationship result = handlePersisted((RelationshipBase)taxRel, state);
        //complete
        result.setFromTaxon(detache(result.getFromTaxon(), state));
        result.setToTaxon(detache(result.getToTaxon(), state));
        result.setType(detache(result.getType(), state));
        return result;
    }

    protected NameRelationship handlePersistedNameRelationship(NameRelationship rel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NameRelationship result = handlePersisted((RelationshipBase)rel, state);
        //complete
        setInvisible(result, "relatedFrom", detache(result.getFromName(), state));
        setInvisible(result, "relatedTo", detache(result.getToName(), state));
//        result.setFromName(detache(result.getFromName(), state));
//        result.setToName(detache(result.getToName(), state));
        result.setType(detache(result.getType(), state));
        return result;
    }

    protected HybridRelationship handlePersistedHybridRelationship(HybridRelationship rel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        HybridRelationship result = handlePersisted((RelationshipBase)rel, state);
        //complete
        setInvisible(result, "relatedFrom", detache(result.getParentName(), state));
        setInvisible(result, "relatedTo", detache(result.getHybridName(), state));
//        result.setFromName(detache(result.getFromName()));
//        result.setToName(detache(result.getToName()));
        result.setType(detache(result.getType(), state));
        return result;
    }

    protected NomenclaturalStatus handlePersistedNomenclaturalStatus(NomenclaturalStatus status, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalStatus result = handlePersisted((SingleSourcedEntityBase)status, state);
        //complete
        result.setType(detache(result.getType(), state));
        return result;
    }

    protected TypeDesignationBase handlePersistedTypeDesignationBase(TypeDesignationBase<?> designation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TypeDesignationBase result = handlePersisted((SourcedEntityBase)designation, state);
        //complete
        result.setCitation(detache(result.getCitation(), state));
        handleCollection(result, TypeDesignationBase.class, "registrations", Registration.class, state);
        handleCollection(result, TypeDesignationBase.class, "typifiedNames", TaxonName.class, state);
        result.setTypeStatus(detache(result.getTypeStatus(), state));
        return result;
    }

    protected InstitutionalMembership handlePersistedInstitutionalMembership(InstitutionalMembership institutionalMembership, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        InstitutionalMembership result = handlePersisted((VersionableEntity)institutionalMembership, state);
        //complete
//        result.setPerson(detache(result.getPerson()));
        setInvisible(result, "person", detache(result.getPerson(), state));
        result.setInstitute(detache(result.getInstitute(), state));
        return result;
    }

    protected Institution handlePersistedInstitution(Institution institution, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Institution result = handlePersisted((AgentBase)institution, state);
        //complete
        result.setIsPartOf(detache(result.getIsPartOf(), state));
        handleCollection(result, Institution.class, "types", DefinedTerm.class, state);
        return result;
    }

    protected TaxonNodeAgentRelation handlePersistedTaxonNodeAgentRelation(TaxonNodeAgentRelation nodeAgentRel, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonNodeAgentRelation result = handlePersisted((AnnotatableEntity)nodeAgentRel, state);
        //complete
        result.setAgent(detache(result.getAgent(), state));
        result.setType(detache(result.getType(), state));
        setInvisible(result, "taxonNode", detache(result.getTaxonNode(), state));
        return result;
    }


    protected TaxonName handlePersistedTaxonName(TaxonName taxonName, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        @SuppressWarnings("rawtypes")
        TaxonName result = handlePersisted((IdentifiableEntity)taxonName, state);
        //complete
        result.setRank(detache(result.getRank(), state));
        result.setCombinationAuthorship(detache(result.getCombinationAuthorship(), state));
        result.setExCombinationAuthorship(detache(result.getExCombinationAuthorship(), state));
        result.setBasionymAuthorship(detache(result.getBasionymAuthorship(), state));
        result.setExBasionymAuthorship(detache(result.getExBasionymAuthorship(), state));
        result.setInBasionymAuthorship(detache(result.getInBasionymAuthorship(), state));
        result.setInCombinationAuthorship(detache(result.getInCombinationAuthorship(), state));

        result.setNomenclaturalReference(detache(result.getNomenclaturalReference(), state));
        result.setNomenclaturalSource(detache(result.getNomenclaturalSource(), state));
        result.setHomotypicalGroup(detache(result.getHomotypicalGroup(), state));
        handleCollection(result, TaxonName.class, "descriptions", TaxonNameDescription.class, state);
        handleCollection(result, TaxonName.class, "hybridChildRelations", HybridRelationship.class, state);
        handleCollection(result, TaxonName.class, "hybridParentRelations", HybridRelationship.class, state);
        handleCollection(result, TaxonName.class, "relationsFromThisName", NameRelationship.class, state);
        handleCollection(result, TaxonName.class, "relationsToThisName", NameRelationship.class, state);
        handleCollection(result, TaxonName.class, "status", NomenclaturalStatus.class, state);

        handleCollection(result, TaxonName.class, "registrations", Registration.class, state);
        handleCollection(result, TaxonName.class, "typeDesignations", TypeDesignationBase.class, state);

        handleCollection(result, TaxonName.class, "taxonBases", TaxonBase.class, state);

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
        result.setAnnotationType(detache(annotation.getAnnotationType(), state));
        result.setCommentator(detache(result.getCommentator(), state));
        handleCollection(result, Annotation.class, "intextReferences", IntextReference.class, state);
        return result;
    }

    protected Extension handlePersistedExtension(Extension extension, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Extension result = handlePersisted((VersionableEntity)extension, state);
        //complete
        result.setType(detache(extension.getType(), state));
        return result;
    }

    protected Marker handlePersistedMarker(Marker marker, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Marker result = handlePersisted((VersionableEntity)marker, state);
        //complete
        result.setMarkerType(detache(marker.getMarkerType(), state));
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
        NamedArea result = handlePersisted((OrderedTermBase)area, state);
        //complete
        handleCollection(result, NamedArea.class, "countries", Country.class, state);
        result.setLevel(detache(result.getLevel(), state));
        result.setType(detache(result.getType(), state));
        result.setShape(detache(result.getShape(), state));
        return result;
    }

    protected Classification handlePersistedClassification(Classification classification, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Classification result = handlePersisted((IdentifiableEntity)classification, state);
        //complete
        result.setName(detache(classification.getName(), state));
        result.setReference(detache(classification.getReference(), state));
        result.setRootNode(detache(classification.getRootNode(), state));
        handleCollection(result, Classification.class, "geoScopes", NamedArea.class, state);
        handleMap(result, Classification.class, "description", Language.class, LanguageString.class, state);
        return result;
    }

    protected Reference handlePersistedReference(Reference reference, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Reference result = handlePersisted((IdentifiableMediaEntity)reference, state);
        result.setAuthorship(detache(result.getAuthorship(), state));
        result.setInstitution(detache(result.getInstitution(), state));
        result.setSchool(detache(result.getSchool(), state));
        result.setInReference(detache(result.getInReference(), state));
        return result;
    }

    protected SpecimenOrObservationBase<?> handlePersistedSpecimenOrObservationBase(SpecimenOrObservationBase specimen, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        //TODO implement for classes
        SpecimenOrObservationBase<?> result = handlePersisted((IdentifiableEntity)specimen, state);
        //complete
        result.setSex(detache(result.getSex(), state));
        result.setLifeStage(detache(result.getLifeStage(), state));
        result.setKindOfUnit(detache(result.getKindOfUnit(), state));
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
        result.setNameUsedInSource(detache(result.getNameUsedInSource(), state));
        return result;
    }

    protected NamedSource handlePersistedNamedSource(NamedSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedSource result = handlePersisted((NamedSourceBase)source, state);
        //complete
        return result;
    }

    protected SecundumSource handlePersistedSecundumSource(DescriptionElementSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        SecundumSource result = handlePersisted((NamedSourceBase)source, state);
        //TODO correct?
        result.setSourcedTaxon(detache(result.getSourcedTaxon(), state));
        return result;
    }

    protected DescriptionElementSource handlePersistedDescriptionElementSource(DescriptionElementSource source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DescriptionElementSource result = handlePersisted((NamedSourceBase)source, state);
        //TODO correct?
        detache(result.getSourcedElement(), state).addSource(result);
        return result;
    }

    protected <T extends CommonTaxonName> T  handlePersistedCommonTaxonName(CommonTaxonName element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setLanguage(detache(result.getLanguage(), state));
        result.setArea(detache(result.getArea(), state));
        return result;
    }

    protected <T extends TextData> T  handlePersistedTextData(TextData element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setFormat(detache(result.getFormat(), state));
        handleMap(result, TextData.class, "multilanguageText", Language.class, LanguageString.class, state);
        return result;
    }

    protected <T extends Distribution> T  handlePersistedDistribution(Distribution element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element, state);
        //complete
        result.setArea(detache(result.getArea(), state));
        result.setStatus(detache(result.getStatus(), state));
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
        NomenclaturalStatusType result = handlePersisted((OrderedTermBase)term, state);
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
        handleCollection(result, TermVocabulary.class, "terms", DefinedTermBase.class, state);
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
        setInvisible(result, "parent", detache(result.getParent(), state));
        handleCollection(result, TermNode.class, "inapplicableIf", FeatureState.class, state);
        handleCollection(result, TermNode.class, "onlyApplicableIf", FeatureState.class, state);
        handleCollection(result, TermNode.class, "children", TermNode.class, state);

        return result;
    }

    protected Representation handlePersistedRepresentation(Representation representation, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Representation result = (Representation)handlePersisted((LanguageStringBase)representation, state);
        return result;
    }

    protected <T extends TermBase> T  handlePersisted(TermBase termBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)termBase, state);
        //complete
        handleCollection(result, TermBase.class, "representations", Representation.class, state);
        return result;
    }

    protected <T extends TermCollection> T  handlePersisted(TermCollection termCollection, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((TermBase)termCollection, state);
        //complete
        handleCollection(result, TermCollection.class, "termRelations", TermRelationBase.class, state);
        return result;
    }

    protected <T extends TermRelationBase> T  handlePersisted(TermRelationBase termRelationBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((VersionableEntity)termRelationBase, state);
        result.setTerm(detache(result.getTerm(), state));
        setInvisible(result, TermRelationBase.class, "graph", detache(result.getGraph(), state));
        return result;
    }

    protected User handlePersistedUser(User user, Cdm2CdmImportState state) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        User result = (User)handlePersistedCdmBase(user, state);
        if (result.getUsername().equals("admin")){
            result = getUserService().listByUsername("admin", MatchMode.EXACT, null, null, null, null, null).iterator().next();
            state.putPermanent(user.getUuid(), result);
            cache(result, state); //necessary?
            state.addToSave(result);
            state.removeToSave(user);
        }
        if (!result.isPersited()){
            result.setAuthorities(new HashSet<>());
            result.setGrantedAuthorities(new HashSet<>());
            setInvisible(result, "groups", new HashSet<>());
        }
        result.setPerson(detache(user.getPerson(), state));
        return result;
    }


    protected LanguageString handlePersistedLanguageString(LanguageString languageString, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        LanguageString result = handlePersisted((LanguageStringBase)languageString, state);
        //complete
        handleCollection(result, LanguageString.class, "intextReferences", IntextReference.class, state);
        return result;
    }

    protected Rights handlePersistedRights(Rights rights, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Rights result = handlePersisted((LanguageStringBase)rights, state);
        result.setAgent(detache(rights.getAgent(), state));
        result.setType(detache(rights.getType(), state));
        //complete
        return result;
    }

    protected IntextReference handlePersistedIntextReference(IntextReference intextReference, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IntextReference result = handlePersisted((VersionableEntity)intextReference, state);
        result.setReferencedEntity(detache(result.getReferencedEntity(), false, state));
        Method targetMethod = IntextReference.class.getDeclaredMethod("setTarget", IIntextReferenceTarget.class);
        targetMethod.setAccessible(true);
        targetMethod.invoke(result, detache(result.getTarget(), false, state));
        return result;
    }

    protected <T extends TaxonDescription> T  handlePersistedTaxonDescription(TaxonDescription taxDescription, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase)taxDescription, state);
        //complete
        setInvisible(taxDescription, "taxon", detache(taxDescription.getTaxon(), state));
        handleCollection(taxDescription, TaxonDescription.class, "geoScopes", NamedArea.class, state);
        handleCollection(taxDescription, TaxonDescription.class, "scopes", DefinedTerm.class, state);
        return result;
    }

    protected <T extends TaxonDescription> T  handlePersistedTaxonNameDescription(TaxonNameDescription nameDescription, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase)nameDescription, state);
        //complete
        setInvisible(nameDescription, "taxonName", detache(nameDescription.getTaxonName(), state));
        return result;
    }


// ***************************** BASE CLASSES ********************************************/

    protected <T extends CdmBase> T handlePersistedCdmBase(CdmBase cdmBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = (T)getTarget(cdmBase, state);
        //complete
        cdmBase.setCreatedBy(detache(cdmBase.getCreatedBy(), state));
        return result;
    }

    protected <T extends VersionableEntity> T handlePersisted(VersionableEntity entity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = (T)handlePersistedCdmBase((CdmBase)entity, state);
        //complete
        entity.setUpdatedBy(detache(entity.getUpdatedBy(), state));
        return result;
    }

    protected <T extends AnnotatableEntity> T handlePersisted(AnnotatableEntity entity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((VersionableEntity)entity, state);
        //complete
        handleCollection(result, AnnotatableEntity.class, "annotations", Annotation.class, state);
        handleCollection(result, AnnotatableEntity.class, "markers", Marker.class, state);
        return result;
    }

    protected <T extends SourcedEntityBase<?>> T  handlePersisted(SourcedEntityBase sourcedEntity,
            Cdm2CdmImportState state)
            throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        int originalId = sourcedEntity.getId();
        T result = handlePersisted((AnnotatableEntity)sourcedEntity, state);
        //complete
        handleCollection(result, SourcedEntityBase.class, "sources", OriginalSourceBase.class, state);
        if (!result.isPersited()){
            if(state.getConfig().isRemoveImportSources()){
                filterImportSources(result.getSources());
            }
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

    /**
     * @param sources
     */
    private void filterImportSources(Set<? extends OriginalSourceBase> sources) {
        Set<OriginalSourceBase> toDelete = new HashSet<>();
        for (OriginalSourceBase osb: sources){
            if (osb.getType() == OriginalSourceType.Import){
                toDelete.add(osb);
            }
        }
        for (OriginalSourceBase osb: toDelete){
            sources.remove(osb);
        }
    }

    protected <T extends IdentifiableEntity> T  handlePersisted(IdentifiableEntity identifiableEntity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((SourcedEntityBase)identifiableEntity, state);
        //complete
        handleCollection(result, IdentifiableEntity.class, "credits", Credit.class, state);
        handleCollection(result, IdentifiableEntity.class, "extensions", Extension.class, state);
        handleCollection(result, IdentifiableEntity.class, "identifiers", Identifier.class, state);
        handleCollection(result, IdentifiableEntity.class, "rights", Rights.class, state);
        handleCollection(result, IdentifiableEntity.class, "links", ExternalLink.class, state);

        return result;
    }

    protected <T extends DefinedTermBase> T  handlePersisted(DefinedTermBase definedTermBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((TermBase)definedTermBase, state);
        //complete
        handleCollection(result, DefinedTermBase.class, "media", Media.class, state);
        handleCollection(result, DefinedTermBase.class, "generalizationOf", DefinedTermBase.class, state);
        handleCollection(result, DefinedTermBase.class, "includes", DefinedTermBase.class, state);
        result.setKindOf(detache(result.getKindOf(), state));
        result.setPartOf(detache(result.getPartOf(), state));
        setInvisible(result, DefinedTermBase.class, "vocabulary", detache(result.getVocabulary(), state));

        return result;
    }

    protected <T extends OriginalSourceBase> T  handlePersisted(OriginalSourceBase source, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)source, state);
        //complete
        result.setCitation(detache(result.getCitation(), state));
        handleCollection(result, OriginalSourceBase.class, "links", ExternalLink.class, state);
        return result;
    }

    protected <T extends LanguageStringBase> T  handlePersisted(LanguageStringBase lsBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)lsBase, state);
        //complete
        result.setLanguage(detache(lsBase.getLanguage(), state));
        return result;
    }

    protected <T extends TeamOrPersonBase> T  handlePersisted(TeamOrPersonBase teamOrPerson, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AgentBase)teamOrPerson, state);
        //complete
        return result;
    }

    protected <T extends AgentBase> T  handlePersisted(AgentBase agent, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableMediaEntity)agent, state);
        result.setContact(detache(result.getContact(), state));
        //complete
        return result;
    }

    protected <T extends TaxonBase> T  handlePersisted(TaxonBase taxonBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)taxonBase, state);
        //complete
        result.setName(detache(taxonBase.getName(), state));
        result.setSec(detache(taxonBase.getSec(), state));
        return result;
    }

    protected <T extends IdentifiableMediaEntity> T  handlePersisted(IdentifiableMediaEntity mediaEntity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)mediaEntity, state);
        //complete
        handleCollection(result, IdentifiableMediaEntity.class, "media", Media.class, state);
        return result;
    }

    protected <T extends SingleSourcedEntityBase> T  handlePersisted(SingleSourcedEntityBase referencedEntity, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)referencedEntity, state);
        //complete
        result.setCitation(detache(result.getCitation(), state));
        return result;
    }

    protected <T extends DescriptionBase> T  handlePersisted(DescriptionBase descriptionBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)descriptionBase, state);
        //complete
        handleCollection(result, DescriptionBase.class, "descriptionElements", DescriptionElementBase.class, state);
        handleCollection(result, DescriptionBase.class, "descriptiveDataSets", DescriptiveDataSet.class, state);
        handleCollection(result, DescriptionBase.class, "descriptionSources", Reference.class, state);
        result.setDescribedSpecimenOrObservation(detache(descriptionBase.getDescribedSpecimenOrObservation(), state));
        return result;
    }

    protected <T extends DescriptionElementBase> T  handlePersisted(DescriptionElementBase element, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)element, state);
        //complete
        result.setFeature(detache(result.getFeature(), state));
        setInvisible(result, DescriptionElementBase.class, "inDescription", detache(result.getInDescription(), state));
        handleCollection(result, DescriptionElementBase.class, "sources", DescriptionElementSource.class, state);
        handleCollection(result, DescriptionElementBase.class, "media", Media.class, state);
        handleCollection(result, DescriptionElementBase.class, "modifiers", DefinedTerm.class, state);
        handleMap(result, DescriptionElementBase.class, "modifyingText", Language.class, LanguageString.class, state);

        return result;
    }

    protected <T extends RelationshipBase> T  handlePersisted(RelationshipBase relBase, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((SingleSourcedEntityBase)relBase, state);
        return result;
    }


//************************** COLLECTIONS / MAPS ****************************************/

    protected <HOLDER extends CdmBase, ITEM extends CdmBase> void handleCollection(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter, Class<ITEM> itemClass,
            Cdm2CdmImportState state)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Collection<ITEM> oldCollection = setNewCollection(holder, declaringClass, parameter, itemClass);
        Collection<ITEM> newCollection = getTargetCollection(itemClass, oldCollection, state);
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
            Class<S> clazz, T sourceCollection, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Collection<S> result =  new ArrayList<>();
        if (Set.class.isAssignableFrom(sourceCollection.getClass())){
            result = new HashSet<>();
        }
        for (S entity : sourceCollection){
            S target = detache(entity, state);
            result.add(target);
        }
        return result;
    }

    private <K extends CdmBase, V extends CdmBase> Map<K,V> getTargetMap(Map<K,V> sourceMap, Cdm2CdmImportState state) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Map<K,V> result = new HashMap<>();
        for (K key : sourceMap.keySet()){
            K targetKey = detache(key, state);
            V targetValue = detache(sourceMap.get(key), state);
            result.put(targetKey, targetValue);
        }
        return result;
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
