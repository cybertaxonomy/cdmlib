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
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
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
import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
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
import eu.etaxonomy.cdm.model.description.State;
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
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
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
    private Cdm2CdmImportState state;

    //TODO move to state
    private Map<UUID, CdmBase> sessionCache = new HashMap<>();
    private Map<Class,Set<UUID>> existingObjects = new HashMap<>();
    protected Set<CdmBase> toSave = new HashSet<>();

    protected ICdmRepository sourceRepo(Cdm2CdmImportState state){
        ICdmRepository repo = state.getSourceRepository();
        if (repo == null){
            System.out.println("start source repo");
            boolean omitTermLoading = true;
            repo = CdmApplicationController.NewInstance(state.getConfig().getSource(),
                    DbSchemaValidation.VALIDATE, omitTermLoading);
            state.setSourceRepository(repo);
            System.out.println("end source repo");
        }
        return repo;
    }

    protected  Contact detache(Contact contact) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        contact = CdmBase.deproxy(contact);
        if (contact == null){
            return contact;
        }else{
            return handlePersistedContact(contact);
        }
    }

    protected  IIntextReferencable detache(IIntextReferencable cdmBase, boolean onlyForDefinedSignature) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferencable)detache((CdmBase)cdmBase);
    }
    protected  IIntextReferenceTarget detache(IIntextReferenceTarget cdmBase, boolean onlyForDefinedSignature) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return (IIntextReferenceTarget)detache((CdmBase)cdmBase);
    }

    protected <T extends CdmBase> T detache(T cdmBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        return detache(cdmBase, false);
    }

    protected <T extends CdmBase> T detache(T cdmBase, boolean notFromSource) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        cdmBase = CdmBase.deproxy(cdmBase);
        if (cdmBase == null ){
            return cdmBase;
        }else if(isInCache(cdmBase)){
            return getCached(cdmBase);
        }else {
            if (existingObjects.get(cdmBase.getClass()) == null){
                loadExistingUuids(cdmBase.getClass());
            }
            boolean exists = existingObjects.get(cdmBase.getClass()).contains(cdmBase.getUuid());
            if (exists){
                Class<T> clazz = (Class<T>)cdmBase.getClass();
                T existingObj = getCommonService().find(clazz, cdmBase.getUuid());
                if (existingObj != null){
                    cache(existingObj);
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
            return notFromSource? null : (T)handlePersisted(cdmBase);
        }
    }

    private Set<UUID> loadExistingUuids(Class<? extends CdmBase> clazz) {
        List<UUID> list = getCommonService().listUuid(clazz);
        Set<UUID> result = new HashSet<>(list);
        existingObjects.put(clazz, result);
        return result;
    }

    protected <A extends CdmBase> CdmBase handlePersisted(A cdmBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        if(cdmBase instanceof TaxonNode){
            return handlePersistedTaxonNode((TaxonNode)cdmBase);
        }else if(cdmBase instanceof Taxon){
            return handlePersistedTaxon((Taxon)cdmBase);
        }else if(cdmBase instanceof Synonym){
            return handlePersistedSynonym((Synonym)cdmBase);
        }else if(cdmBase instanceof TaxonName){
            return handlePersistedTaxonName((TaxonName)cdmBase);
        }else if(cdmBase instanceof Team){
            return handlePersistedTeam((Team)cdmBase);
        }else if(cdmBase instanceof Person){
            return handlePersistedPerson((Person)cdmBase);
        }else if(cdmBase instanceof Classification){
            return handlePersistedClassification((Classification)cdmBase);
        }else if(cdmBase instanceof Reference){
            return handlePersistedReference((Reference)cdmBase);
        }else if(cdmBase instanceof SpecimenOrObservationBase){
            return handlePersistedSpecimenOrObservationBase((SpecimenOrObservationBase)cdmBase);
        }else if(cdmBase instanceof IdentifiableSource){
            return handlePersistedIdentifiableSource((IdentifiableSource)cdmBase);
        }else if(cdmBase instanceof DescriptionElementSource){
            return handlePersistedDescriptionElementSource((DescriptionElementSource)cdmBase);
        }else if(cdmBase instanceof CommonTaxonName){
            return handlePersistedCommonTaxonName((CommonTaxonName)cdmBase);
        }else if(cdmBase instanceof Distribution){
            return handlePersistedDistribution((Distribution)cdmBase);
        }else if(cdmBase instanceof TextData){
            return handlePersistedTextData((TextData)cdmBase);
        }else if(cdmBase instanceof HomotypicalGroup){
            return handlePersistedHomotypicalGroup((HomotypicalGroup)cdmBase);
        }else if(cdmBase instanceof TypeDesignationBase){
            return handlePersistedTypeDesignationBase((TypeDesignationBase)cdmBase);
        }else if(cdmBase instanceof TaxonDescription){
            return handlePersistedTaxonDescription((TaxonDescription)cdmBase);
        }else if(cdmBase instanceof NomenclaturalStatus){
            return handlePersistedNomenclaturalStatus((NomenclaturalStatus)cdmBase);
        }else if(cdmBase instanceof TaxonNameDescription){
            return handlePersistedTaxonNameDescription((TaxonNameDescription)cdmBase);
        }else if(cdmBase instanceof TaxonRelationship){
            return handlePersistedTaxonRelationship((TaxonRelationship)cdmBase);
        }else if(cdmBase instanceof HybridRelationship){
            return handlePersistedHybridRelationship((HybridRelationship)cdmBase);
        }else if(cdmBase instanceof NameRelationship){
            return handlePersistedNameRelationship((NameRelationship)cdmBase);
        }else if(cdmBase instanceof TaxonNodeAgentRelation){
            return handlePersistedTaxonNodeAgentRelation((TaxonNodeAgentRelation)cdmBase);
        }else if(cdmBase instanceof User){
            return handlePersistedUser((User)cdmBase);
        }else if(cdmBase instanceof Extension){
            return handlePersistedExtension((Extension)cdmBase);
        }else if(cdmBase instanceof Marker){
            return handlePersistedMarker((Marker)cdmBase);
        }else if(cdmBase instanceof Annotation){
            return handlePersistedAnnotation((Annotation)cdmBase);
        }else if(cdmBase instanceof LanguageString){
            return handlePersistedLanguageString((LanguageString)cdmBase);
        }else if(cdmBase instanceof TermVocabulary){
            return handlePersistedVocabulary((TermVocabulary<?>)cdmBase);
        }else if(cdmBase instanceof NamedArea){
            return handlePersistedNamedArea((NamedArea)cdmBase);
        }else if(cdmBase instanceof TermNode){
            return handlePersistedTermNode((TermNode)cdmBase);
        }else if(cdmBase instanceof Representation){
            return handlePersistedRepresentation((Representation)cdmBase);
        }else if(cdmBase instanceof InstitutionalMembership){
            return handlePersistedInstitutionalMembership((InstitutionalMembership)cdmBase);
        }else if(cdmBase instanceof Institution){
            return handlePersistedInstitution((Institution)cdmBase);
        }else if(cdmBase instanceof IntextReference){
            return handlePersistedIntextReference((IntextReference)cdmBase);
        }else if(cdmBase instanceof ExtensionType){
            return handlePersistedExtensionType((ExtensionType)cdmBase);
        }else if(cdmBase instanceof NomenclaturalStatusType){
            return handlePersistedNomenclaturalStatusType((NomenclaturalStatusType)cdmBase);
        }else if(cdmBase instanceof MarkerType){
            return handlePersistedMarkerType((MarkerType)cdmBase);
        }else if(cdmBase instanceof Rights){
            return handlePersistedRights((Rights)cdmBase);
        }else if(cdmBase instanceof DefinedTerm){
            return handlePersistedDefinedTerm((DefinedTerm)cdmBase);
        }else if(cdmBase instanceof DefinedTermBase){
            return handlePersistedTerm((DefinedTermBase<?>)cdmBase);
        }else {
            throw new RuntimeException("Type not yet supported: " + cdmBase.getClass().getCanonicalName());
        }
    }


    protected TaxonNode handlePersistedTaxonNode(TaxonNode node) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {

        TaxonNode result = handlePersisted((AnnotatableEntity)node);
        if (result ==null){
            return result;
        }
        //complete
        handleCollection(result, TaxonNode.class, "agentRelations", TaxonNodeAgentRelation.class);
        result.setTaxon(detache(result.getTaxon()));
        result.setReference(detache(node.getReference()));
        result.setSynonymToBeUsed(detache(result.getSynonymToBeUsed()));
        handleMap(result, TaxonNode.class, "excludedNote", Language.class, LanguageString.class);
        //classification, parent, children
        this.setInvisible(node, "classification", detache(node.getClassification()));
        handleParentTaxonNode(result);
        setNewCollection(node, TaxonNode.class, "childNodes", TaxonNode.class);
        return result;
    }

    private void handleParentTaxonNode(TaxonNode childNode) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonNode parent = detache(childNode.getParent(), true);
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

    protected Taxon handlePersistedTaxon(Taxon taxon) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Taxon result = handlePersisted((TaxonBase)taxon);
        //complete
        handleCollection(result, Taxon.class, "synonyms", Synonym.class);
//        handleCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        setNewCollection(result, Taxon.class, "taxonNodes", TaxonNode.class);
        handleCollection(result, Taxon.class, "relationsFromThisTaxon", TaxonRelationship.class);
        handleCollection(result, Taxon.class, "relationsToThisTaxon", TaxonRelationship.class);
        if (this.doDescriptions(state)){
            handleCollection(result, Taxon.class, "descriptions", TaxonDescription.class);
        }else{
            setNewCollection(result, Taxon.class, "descriptions", TaxonDescription.class);
        }
        return result;
    }

    protected boolean doDescriptions(Cdm2CdmImportState state) {
        return false;
    }

    protected Synonym handlePersistedSynonym(Synonym synonym) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Synonym result = handlePersisted((TaxonBase)synonym);
        //complete
        setInvisible(result, "acceptedTaxon", detache(result.getAcceptedTaxon()));
        result.setType(detache(result.getType()));
        return result;
    }

    protected TaxonRelationship handlePersistedTaxonRelationship(TaxonRelationship taxRel) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonRelationship result = handlePersisted((RelationshipBase)taxRel);
        //complete
        result.setFromTaxon(detache(result.getFromTaxon()));
        result.setToTaxon(detache(result.getToTaxon()));
        result.setType(detache(result.getType()));
        return result;
    }

    protected NameRelationship handlePersistedNameRelationship(NameRelationship rel) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NameRelationship result = handlePersisted((RelationshipBase)rel);
        //complete
        setInvisible(result, "relatedFrom", detache(result.getFromName()));
        setInvisible(result, "relatedTo", detache(result.getToName()));
//        result.setFromName(detache(result.getFromName()));
//        result.setToName(detache(result.getToName()));
        result.setType(detache(result.getType()));
        return result;
    }

    protected HybridRelationship handlePersistedHybridRelationship(HybridRelationship rel) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        HybridRelationship result = handlePersisted((RelationshipBase)rel);
        //complete
        setInvisible(result, "relatedFrom", detache(result.getParentName()));
        setInvisible(result, "relatedTo", detache(result.getHybridName()));
//        result.setFromName(detache(result.getFromName()));
//        result.setToName(detache(result.getToName()));
        result.setType(detache(result.getType()));
        return result;
    }

    protected NomenclaturalStatus handlePersistedNomenclaturalStatus(NomenclaturalStatus status) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalStatus result = handlePersisted((ReferencedEntityBase)status);
        //complete
        result.setType(detache(result.getType()));
        return result;
    }

    protected TypeDesignationBase handlePersistedTypeDesignationBase(TypeDesignationBase<?> designation) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TypeDesignationBase result = handlePersisted((SourcedEntityBase)designation);
        //complete
        result.setCitation(detache(result.getCitation()));
        handleCollection(result, TypeDesignationBase.class, "registrations", Registration.class);
        handleCollection(result, TypeDesignationBase.class, "typifiedNames", TaxonName.class);
        result.setTypeStatus(detache(result.getTypeStatus()));
        return result;
    }

    protected InstitutionalMembership handlePersistedInstitutionalMembership(InstitutionalMembership institutionalMembership) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        InstitutionalMembership result = handlePersisted((VersionableEntity)institutionalMembership);
        //complete
//        result.setPerson(detache(result.getPerson()));
        setInvisible(result, "person", detache(result.getPerson()));
        result.setInstitute(detache(result.getInstitute()));
        return result;
    }

    protected Institution handlePersistedInstitution(Institution institution) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Institution result = handlePersisted((AgentBase)institution);
        //complete
        result.setIsPartOf(detache(result.getIsPartOf()));
        handleCollection(result, Institution.class, "types", DefinedTerm.class);
        return result;
    }

    protected TaxonNodeAgentRelation handlePersistedTaxonNodeAgentRelation(TaxonNodeAgentRelation nodeAgentRel) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TaxonNodeAgentRelation result = handlePersisted((AnnotatableEntity)nodeAgentRel);
        //complete
        result.setAgent(detache(result.getAgent()));
        result.setType(detache(result.getType()));
        setInvisible(result, "taxonNode", detache(result.getTaxonNode()));
        return result;
    }


    protected TaxonName handlePersistedTaxonName(TaxonName taxonName) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        @SuppressWarnings("rawtypes")
        TaxonName result = handlePersisted((IdentifiableEntity)taxonName);
        //complete
        result.setRank(detache(result.getRank()));
        result.setCombinationAuthorship(detache(result.getCombinationAuthorship()));
        result.setExCombinationAuthorship(detache(result.getExCombinationAuthorship()));
        result.setBasionymAuthorship(detache(result.getBasionymAuthorship()));
        result.setExBasionymAuthorship(detache(result.getExBasionymAuthorship()));
        result.setInBasionymAuthorship(detache(result.getInBasionymAuthorship()));
        result.setInCombinationAuthorship(detache(result.getInCombinationAuthorship()));

        result.setNomenclaturalReference(detache(result.getNomenclaturalReference()));
        result.setNomenclaturalSource(detache(result.getNomenclaturalSource()));
        result.setHomotypicalGroup(detache(result.getHomotypicalGroup()));
        handleCollection(result, TaxonName.class, "descriptions", TaxonNameDescription.class);
        handleCollection(result, TaxonName.class, "hybridChildRelations", HybridRelationship.class);
        handleCollection(result, TaxonName.class, "hybridParentRelations", HybridRelationship.class);
        handleCollection(result, TaxonName.class, "relationsFromThisName", NameRelationship.class);
        handleCollection(result, TaxonName.class, "relationsToThisName", NameRelationship.class);
        handleCollection(result, TaxonName.class, "status", NomenclaturalStatus.class);

        handleCollection(result, TaxonName.class, "registrations", Registration.class);
        handleCollection(result, TaxonName.class, "typeDesignations", TypeDesignationBase.class);

        handleCollection(result, TaxonName.class, "taxonBases", TaxonBase.class);

        return result;
    }

    protected HomotypicalGroup handlePersistedHomotypicalGroup(HomotypicalGroup group) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        HomotypicalGroup result = handlePersisted((AnnotatableEntity)group);
        //complete
        handleCollection(result, HomotypicalGroup.class, "typifiedNames", TaxonName.class);
        return result;
    }

    protected Annotation handlePersistedAnnotation(Annotation annotation) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Annotation result = handlePersisted((AnnotatableEntity)annotation);
        //complete
        result.setAnnotationType(detache(annotation.getAnnotationType()));
        result.setCommentator(detache(result.getCommentator()));
        handleCollection(result, Annotation.class, "intextReferences", IntextReference.class);
        return result;
    }

    protected Extension handlePersistedExtension(Extension extension) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Extension result = handlePersisted((VersionableEntity)extension);
        //complete
        result.setType(detache(extension.getType()));
        return result;
    }

    protected Marker handlePersistedMarker(Marker marker) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Marker result = handlePersisted((VersionableEntity)marker);
        //complete
        result.setMarkerType(detache(marker.getMarkerType()));
        return result;
    }

    protected Team handlePersistedTeam(Team team) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Team result = handlePersisted((TeamOrPersonBase)team);
        //complete
        handleCollection(result, Team.class, "teamMembers", Person.class);
        return result;
    }

    protected Contact handlePersistedContact(Contact contact) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
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

    protected Person handlePersistedPerson(Person person) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Person result = handlePersisted((TeamOrPersonBase)person);
        //complete
        handleCollection(result, Person.class, "institutionalMemberships", InstitutionalMembership.class);
        return result;
    }

    protected NamedArea handlePersistedNamedArea(NamedArea area) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NamedArea result = handlePersisted((OrderedTermBase)area);
        //complete
        handleCollection(result, NamedArea.class, "countries", Country.class);
        result.setLevel(detache(result.getLevel()));
        result.setType(detache(result.getType()));
        result.setShape(detache(result.getShape()));
        return result;
    }

    protected Classification handlePersistedClassification(Classification classification) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Classification result = handlePersisted((IdentifiableEntity)classification);
        //complete
        result.setName(detache(classification.getName()));
        result.setReference(detache(classification.getReference()));
        result.setRootNode(detache(classification.getRootNode()));
        handleCollection(result, Classification.class, "geoScopes", NamedArea.class);
        handleMap(result, Classification.class, "description", Language.class, LanguageString.class);
        return result;
    }

    protected Reference handlePersistedReference(Reference reference) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Reference result = handlePersisted((IdentifiableMediaEntity)reference);
        result.setAuthorship(detache(result.getAuthorship()));
        result.setInstitution(detache(result.getInstitution()));
        result.setSchool(detache(result.getSchool()));
        result.setInReference(detache(result.getInReference()));
        return result;
    }

    protected SpecimenOrObservationBase<?> handlePersistedSpecimenOrObservationBase(SpecimenOrObservationBase specimen) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        //TODO implement for classes
        SpecimenOrObservationBase<?> result = handlePersisted((IdentifiableEntity)specimen);
        //complete
        result.setSex(detache(result.getSex()));
        result.setLifeStage(detache(result.getLifeStage()));
        result.setKindOfUnit(detache(result.getKindOfUnit()));
        handleCollection(result, SpecimenOrObservationBase.class, "determinations", DeterminationEvent.class);
        handleCollection(result, SpecimenOrObservationBase.class, "descriptions", SpecimenDescription.class);
        handleCollection(result, SpecimenOrObservationBase.class, "derivationEvents", DerivationEvent.class);
        handleMap(result, SpecimenOrObservationBase.class, "definition", Language.class, LanguageString.class);
        return result;
    }

    protected IdentifiableSource handlePersistedIdentifiableSource(IdentifiableSource source) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IdentifiableSource result = handlePersisted((OriginalSourceBase)source);
        //complete
        return result;
    }

    protected DescriptionElementSource handlePersistedDescriptionElementSource(DescriptionElementSource source) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DescriptionElementSource result = handlePersisted((OriginalSourceBase)source);
        //complete
        result.setNameUsedInSource(detache(result.getNameUsedInSource()));
        return result;
    }

    protected <T extends CommonTaxonName> T  handlePersistedCommonTaxonName(CommonTaxonName element) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element);
        //complete
        result.setLanguage(detache(result.getLanguage()));
        result.setArea(detache(result.getArea()));
        return result;
    }

    protected <T extends TextData> T  handlePersistedTextData(TextData element) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element);
        //complete
        result.setFormat(detache(result.getFormat()));
        handleMap(result, TextData.class, "multilanguageText", Language.class, LanguageString.class);
        return result;
    }

    protected <T extends Distribution> T  handlePersistedDistribution(Distribution element) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionElementBase)element);
        //complete
        result.setArea(detache(result.getArea()));
        result.setStatus(detache(result.getStatus()));
        return result;
    }

    protected ExtensionType handlePersistedExtensionType(ExtensionType term) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        ExtensionType result = handlePersisted((DefinedTermBase)term);
        //complete
        return result;
    }

    protected MarkerType handlePersistedMarkerType(MarkerType term) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        MarkerType result = handlePersisted((DefinedTermBase)term);
        //complete
        return result;
    }

    protected NomenclaturalStatusType handlePersistedNomenclaturalStatusType(NomenclaturalStatusType term) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        NomenclaturalStatusType result = handlePersisted((OrderedTermBase)term);
        //complete
        return result;
    }

    protected DefinedTerm handlePersistedDefinedTerm(DefinedTerm term) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DefinedTerm result = handlePersisted((DefinedTermBase)term);
        //complete
        return result;
    }

    //placeholder for not implemented methods for subclasses
    protected DefinedTermBase<?> handlePersistedTerm(DefinedTermBase term) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        DefinedTermBase<?> result = handlePersisted(term);
        logger.warn("Class not yet handled: " + term.getClass().getSimpleName());
        return result;
    }

    protected TermVocabulary<?> handlePersistedVocabulary(TermVocabulary voc) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TermVocabulary<?> result = (TermVocabulary<?>)handlePersisted((TermCollection)voc);
        handleCollection(result, TermVocabulary.class, "terms", DefinedTermBase.class);
        return result;
    }

    protected TermNode<?> handlePersistedTermNode(TermNode node) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        TermNode<?> result = (TermNode<?>)handlePersisted((TermRelationBase)node);
        //complete
        setInvisible(result, "parent", detache(result.getParent()));
        handleCollection(result, TermNode.class, "inapplicableIf", FeatureState.class);
        handleCollection(result, TermNode.class, "onlyApplicableIf", FeatureState.class);
        handleCollection(result, TermNode.class, "inapplicableIf_old", State.class);
        handleCollection(result, TermNode.class, "onlyApplicableIf_old", State.class);
        handleCollection(result, TermNode.class, "children", TermNode.class);

        return result;
    }

    protected Representation handlePersistedRepresentation(Representation representation) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Representation result = (Representation)handlePersisted((LanguageStringBase)representation);
        return result;
    }

    protected <T extends TermBase> T  handlePersisted(TermBase termBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)termBase);
        //complete
        handleCollection(result, TermBase.class, "representations", Representation.class);
        return result;
    }

    protected <T extends TermCollection> T  handlePersisted(TermCollection termCollection) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((TermBase)termCollection);
        //complete
        handleCollection(result, TermCollection.class, "termRelations", TermRelationBase.class);
        return result;
    }

    protected <T extends TermRelationBase> T  handlePersisted(TermRelationBase termRelationBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((VersionableEntity)termRelationBase);
        result.setTerm(detache(result.getTerm()));
        setInvisible(result, TermRelationBase.class, "graph", detache(result.getGraph()));
        return result;
    }

    protected User handlePersistedUser(User user) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        User result = (User)handlePersistedCdmBase(user);
        if (result.getUsername().equals("admin")){
            result = getUserService().listByUsername("admin", MatchMode.EXACT, null, null, null, null, null).iterator().next();
            getState().putPermanent(user.getUuid(), result);
            cache(result); //necessary?
            toSave.add(result);
            toSave.remove(user);
        }
        if (!result.isPersited()){
            result.setAuthorities(new HashSet<>());
            result.setGrantedAuthorities(new HashSet<>());
            setInvisible(result, "groups", new HashSet<>());
        }
        result.setPerson(detache(user.getPerson()));
        return result;
    }


    protected LanguageString handlePersistedLanguageString(LanguageString languageString) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        LanguageString result = handlePersisted((LanguageStringBase)languageString);
        //complete
        handleCollection(result, LanguageString.class, "intextReferences", IntextReference.class);
        return result;
    }

    protected Rights handlePersistedRights(Rights rights) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Rights result = handlePersisted((LanguageStringBase)rights);
        result.setAgent(detache(rights.getAgent()));
        result.setType(detache(rights.getType()));
        //complete
        return result;
    }

    protected IntextReference handlePersistedIntextReference(IntextReference intextReference) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        IntextReference result = handlePersisted((VersionableEntity)intextReference);
        result.setReferencedEntity(detache(result.getReferencedEntity(), false));
        Method targetMethod = IntextReference.class.getDeclaredMethod("setTarget", IIntextReferenceTarget.class);
        targetMethod.setAccessible(true);
        targetMethod.invoke(result, detache(result.getTarget(), false));
        return result;
    }

    protected <T extends TaxonDescription> T  handlePersistedTaxonDescription(TaxonDescription taxDescription) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase)taxDescription);
        //complete
        setInvisible(taxDescription, "taxon", detache(taxDescription.getTaxon()));
        handleCollection(taxDescription, TaxonDescription.class, "geoScopes", NamedArea.class);
        handleCollection(taxDescription, TaxonDescription.class, "scopes", DefinedTerm.class);
        return result;
    }

    protected <T extends TaxonDescription> T  handlePersistedTaxonNameDescription(TaxonNameDescription nameDescription) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((DescriptionBase)nameDescription);
        //complete
        setInvisible(nameDescription, "taxonName", detache(nameDescription.getTaxonName()));
        return result;
    }


// ***************************** BASE CLASSES ********************************************/

    protected <T extends CdmBase> T handlePersistedCdmBase(CdmBase cdmBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = (T)getTarget(cdmBase);
        //complete
        cdmBase.setCreatedBy(detache(cdmBase.getCreatedBy()));
        return result;
    }

    protected <T extends VersionableEntity> T handlePersisted(VersionableEntity entity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = (T)handlePersistedCdmBase((CdmBase)entity);
        //complete
        entity.setUpdatedBy(detache(entity.getUpdatedBy()));
        return result;
    }

    protected <T extends AnnotatableEntity> T handlePersisted(AnnotatableEntity entity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((VersionableEntity)entity);
        //complete
        handleCollection(result, AnnotatableEntity.class, "annotations", Annotation.class);
        handleCollection(result, AnnotatableEntity.class, "markers", Marker.class);
        return result;
    }

    protected <T extends SourcedEntityBase> T  handlePersisted(SourcedEntityBase sourcedEntity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        int originalId = sourcedEntity.getId();
        T result = handlePersisted((AnnotatableEntity)sourcedEntity);
        //complete
        handleCollection(result, SourcedEntityBase.class, "sources", OriginalSourceBase.class);
        if (!result.isPersited()){
            if(getState().getConfig().isRemoveImportSources()){
                filterImportSources(result.getSources());
            }
            if (getState().getConfig().isAddSources()){
                Reference sourceRef = getSourceReference(getState());
                OriginalSourceBase<?> newSource = result.addImportSource(String.valueOf(originalId), sourcedEntity.getClass().getSimpleName(),
                        sourceRef, null);
                getCommonService().save(newSource);
                addExistingObject(newSource);
            }
        }
        return result;
    }

    /**
     * @param sources
     */
    private void filterImportSources(Set<OriginalSourceBase<?>> sources) {
        Set<OriginalSourceBase<?>> toDelete = new HashSet<>();
        for (OriginalSourceBase<?> osb: sources){
            if (osb.getType() == OriginalSourceType.Import){
                toDelete.add(osb);
            }
        }
        for (OriginalSourceBase<?> osb: toDelete){
            sources.remove(osb);
        }
    }

    protected <T extends IdentifiableEntity> T  handlePersisted(IdentifiableEntity identifiableEntity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((SourcedEntityBase)identifiableEntity);
        //complete
        handleCollection(result, IdentifiableEntity.class, "credits", Credit.class);
        handleCollection(result, IdentifiableEntity.class, "extensions", Extension.class);
        handleCollection(result, IdentifiableEntity.class, "identifiers", Identifier.class);
        handleCollection(result, IdentifiableEntity.class, "rights", Rights.class);
        return result;
    }

    protected <T extends DefinedTermBase> T  handlePersisted(DefinedTermBase definedTermBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((TermBase)definedTermBase);
        //complete
        handleCollection(result, DefinedTermBase.class, "media", Media.class);
        handleCollection(result, DefinedTermBase.class, "generalizationOf", DefinedTermBase.class);
        handleCollection(result, DefinedTermBase.class, "includes", DefinedTermBase.class);
        result.setKindOf(detache(result.getKindOf()));
        result.setPartOf(detache(result.getPartOf()));
        setInvisible(result, DefinedTermBase.class, "vocabulary", detache(result.getVocabulary()));

        return result;
    }

    protected <T extends OriginalSourceBase> T  handlePersisted(OriginalSourceBase source) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((ReferencedEntityBase)source);
        //complete
        handleCollection(result, OriginalSourceBase.class, "links", ExternalLink.class);
        return result;
    }

    protected <T extends LanguageStringBase> T  handlePersisted(LanguageStringBase lsBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)lsBase);
        //complete
        result.setLanguage(detache(lsBase.getLanguage()));
        return result;
    }

    protected <T extends TeamOrPersonBase> T  handlePersisted(TeamOrPersonBase teamOrPerson) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AgentBase)teamOrPerson);
        //complete
        return result;
    }

    protected <T extends AgentBase> T  handlePersisted(AgentBase agent) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableMediaEntity)agent);
        result.setContact(detache(result.getContact()));
        //complete
        return result;
    }

    protected <T extends TaxonBase> T  handlePersisted(TaxonBase taxonBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)taxonBase);
        //complete
        result.setName(detache(taxonBase.getName()));
        result.setSec(detache(taxonBase.getSec()));
        return result;
    }

    protected <T extends IdentifiableMediaEntity> T  handlePersisted(IdentifiableMediaEntity mediaEntity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)mediaEntity);
        //complete
        handleCollection(result, IdentifiableMediaEntity.class, "media", Media.class);
        return result;
    }

    protected <T extends ReferencedEntityBase> T  handlePersisted(ReferencedEntityBase referencedEntity) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)referencedEntity);
        //complete
        result.setCitation(detache(result.getCitation()));
        return result;
    }

    protected <T extends DescriptionBase> T  handlePersisted(DescriptionBase descriptionBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((IdentifiableEntity)descriptionBase);
        //complete
        handleCollection(result, DescriptionBase.class, "descriptionElements", DescriptionElementBase.class);
        handleCollection(result, DescriptionBase.class, "descriptiveDataSets", DescriptiveDataSet.class);
        handleCollection(result, DescriptionBase.class, "descriptionSources", Reference.class);
        result.setDescribedSpecimenOrObservation(detache(descriptionBase.getDescribedSpecimenOrObservation()));
        return result;
    }

    protected <T extends DescriptionElementBase> T  handlePersisted(DescriptionElementBase element) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((AnnotatableEntity)element);
        //complete
        result.setFeature(detache(result.getFeature()));
        setInvisible(result, DescriptionElementBase.class, "inDescription", detache(result.getInDescription()));
        handleCollection(result, DescriptionElementBase.class, "sources", DescriptionElementSource.class);
        handleCollection(result, DescriptionElementBase.class, "media", Media.class);
        handleCollection(result, DescriptionElementBase.class, "modifiers", DefinedTerm.class);
        handleMap(result, DescriptionElementBase.class, "modifyingText", Language.class, LanguageString.class);

        return result;
    }

    protected <T extends RelationshipBase> T  handlePersisted(RelationshipBase relBase) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        T result = handlePersisted((ReferencedEntityBase)relBase);
        return result;
    }


//************************** COLLECTIONS / MAPS ****************************************/

    protected <HOLDER extends CdmBase, ITEM extends CdmBase> void handleCollection(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter, Class<ITEM> itemClass)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Collection<ITEM> oldCollection = setNewCollection(holder, declaringClass, parameter, itemClass);
        Collection<ITEM> newCollection = getTargetCollection(itemClass, oldCollection);
        Field field = declaringClass.getDeclaredField(parameter);
        field.setAccessible(true);
        field.set(holder, newCollection);
    }

    protected <HOLDER extends CdmBase, KEY extends CdmBase, ITEM extends CdmBase>
            void handleMap(
            HOLDER holder, Class<? super HOLDER> declaringClass, String parameter,
            Class<KEY> keyClass, Class<ITEM> itemClass)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        //TODO we do not need to set the new map 2x
        Map<KEY,ITEM> oldMap = setNewMap(holder, declaringClass, parameter, keyClass, itemClass);
        Map<KEY,ITEM> newMap = getTargetMap(oldMap);
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


    private <T extends Collection<S>, S extends CdmBase> Collection<S> getTargetCollection(Class<S> clazz, T sourceCollection) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Collection<S> result =  new ArrayList<>();
        if (Set.class.isAssignableFrom(sourceCollection.getClass())){
            result = new HashSet<>();
        }
        for (S entity : sourceCollection){
            S target = detache(entity);
            result.add(target);
        }
        return result;
    }

    private <K extends CdmBase, V extends CdmBase> Map<K,V> getTargetMap(Map<K,V> sourceMap) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException, IllegalArgumentException, NoSuchMethodException {
        Map<K,V> result = new HashMap<>();
        for (K key : sourceMap.keySet()){
            K targetKey = detache(key);
            V targetValue = detache(sourceMap.get(key));
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
    private <T extends CdmBase> T getTarget(T source) {
        if (source == null){
            return null;
        }
        T result = getCached(source);
//        if (result == null){
//            Class<T> clazz = (Class<T>)source.getClass();
//            result = getCommonService().find(clazz, source.getUuid());
//        }
        if (result == null){
            //Alternative: clone?
            result = CdmBase.deproxy(source);
            result.setId(0);
            cache(result);
            toSave.add(result);
        }
        return result;
    }

// ******************* CACHE *******************************************************/


    protected void cache(CdmBase cdmBase) {
       if (cdmBase instanceof User || cdmBase instanceof DefinedTermBase){
           getState().putPermanent(cdmBase.getUuid(), cdmBase);
       }else{
           sessionCache.put(cdmBase.getUuid(), cdmBase);
       }
       addExistingObject(cdmBase);

    }

    private void addExistingObject(CdmBase cdmBase) {
        cdmBase = CdmBase.deproxy(cdmBase);
        Set<UUID> set = existingObjects.get(cdmBase.getClass());
        if (set == null){
            set = loadExistingUuids(cdmBase.getClass());
//            set = new HashSet<>();
//            existingObjects.put(cdmBase.getClass(), set);
        }
        set.add(cdmBase.getUuid());
    }

    protected boolean isInCache(CdmBase cdmBase) {
        return getCached(cdmBase) != null;
    }

    protected <T extends CdmBase> T getCached(T cdmBase) {
        T result = (T)sessionCache.get(cdmBase.getUuid());
        if (result == null){
            result = (T)getState().getPermanent(cdmBase.getUuid());
        }
        return result;
    }

    protected void clearCache() {
        sessionCache.clear();
    }

    private Reference getSourceReference(Cdm2CdmImportState state) {
        UUID uuid = state.getConfig().getSourceRefUuid();
        if (uuid == null){
            uuid = state.getConfig().getSourceReference().getUuid();
            state.getConfig().setSourceRefUuid(uuid);
        }
        Reference result = (Reference)sessionCache.get(uuid);
        if (result == null){
            result = (Reference)getState().getPermanent(uuid);
        }
        if (result == null){
            result = getReferenceService().find(uuid);
        }
        if (result == null){
            result = state.getConfig().getSourceReference();
            getReferenceService().save(result);
            sessionCache.put(result.getUuid(), result);
        }
        return result;
    }

    public Cdm2CdmImportState getState() {
        return state;
    }

    public void setState(Cdm2CdmImportState state) {
        this.state = state;
    }

}
