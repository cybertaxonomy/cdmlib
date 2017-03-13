/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.validation.batch;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.attribute.standard.Media;
import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author ayco_holleman
 * @date 28 jan. 2015
 *
 */
class BatchValidationUtil {

    public static void main(String[] args) {
        System.out.println(ITaxonService.class.getGenericInterfaces()[0]);
    }

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BatchValidationUtil.class);

    // Ideally retrieved dynamically through reflection, but got stuck on
    // getXXXService methods in ICdmRepository returning proxies
    // (com.sun.proxy.$Proxy), which is a dead end when attempting to infer
    // parameter arguments (e.g. the AgentBase in IAgentService<AgentBase>).
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends ICdmBase, S extends T> List<EntityValidationUnit<T, S>> getAvailableServices(
            ICdmRepository appConfig) {
        List<EntityValidationUnit<T, S>> services = new ArrayList<EntityValidationUnit<T, S>>();
        services.add(new EntityValidationUnit(AgentBase.class, appConfig.getAgentService()));
        services.add(new EntityValidationUnit(Amplification.class, appConfig.getAmplificationService()));
        services.add(new EntityValidationUnit(Classification.class, appConfig.getClassificationService()));
        services.add(new EntityValidationUnit(Collection.class, appConfig.getCollectionService()));
//        services.add(new EntityValidationUnit(OriginalSourceBase.class, appConfig.getCommonService()));
        services.add(new EntityValidationUnit(DescriptionBase.class, appConfig.getDescriptionService()));
        services.add(new EntityValidationUnit(FeatureNode.class, appConfig.getFeatureNodeService()));
        services.add(new EntityValidationUnit(FeatureTree.class, appConfig.getFeatureTreeService()));
        services.add(new EntityValidationUnit(Group.class, appConfig.getGroupService()));
        // Causes some AOP-related error when calling list() method on it
        //services.add(new EntityValidationUnit(DefinedTermBase.class, appConfig.getLocationService()));
        services.add(new EntityValidationUnit(Media.class, appConfig.getMediaService()));
        services.add(new EntityValidationUnit(TaxonNameBase.class, appConfig.getNameService()));
        services.add(new EntityValidationUnit(SpecimenOrObservationBase.class, appConfig.getOccurrenceService()));
        services.add(new EntityValidationUnit(PolytomousKeyNode.class, appConfig.getPolytomousKeyNodeService()));
        services.add(new EntityValidationUnit(PolytomousKey.class, appConfig.getPolytomousKeyService()));
        services.add(new EntityValidationUnit(Primer.class, appConfig.getPrimerService()));
        services.add(new EntityValidationUnit(Reference.class, appConfig.getReferenceService()));
        services.add(new EntityValidationUnit(Sequence.class, appConfig.getSequenceService()));
        services.add(new EntityValidationUnit(TaxonNode.class, appConfig.getTaxonNodeService()));
        services.add(new EntityValidationUnit(TaxonBase.class, appConfig.getTaxonService()));
        services.add(new EntityValidationUnit(DefinedTermBase.class, appConfig.getTermService()));
        services.add(new EntityValidationUnit(User.class, appConfig.getUserService()));
        services.add(new EntityValidationUnit(TermVocabulary.class, appConfig.getVocabularyService()));
        services.add(new EntityValidationUnit(WorkingSet.class, appConfig.getWorkingSetService()));
        return services;
    }

    public static <T extends ICdmBase, S extends T> List<Class<CdmBase>> getClassesToValidate(){
        List<Class<CdmBase>> classesToValidate = new ArrayList<Class<CdmBase>>();
        classesToValidate.addAll((List)Arrays.asList(new Class[]{
                Reference.class,
                NameRelationship.class,
                TaxonNameBase.class,
                TypeDesignationBase.class,
                TaxonBase.class,
                Synonym.class,
                TaxonNode.class,
                GatheringEvent.class}));
        return classesToValidate;
    }


    // Created to infer (1st) parameter type of parametrized type,
    // but won't work because the service argument appears to be a
    // proxy object (com.sun.proxy.$Proxy).
    public static Class<?> getServicedEntity(IService<?> service) {
        Class<?> serviceClass = service.getClass();
        System.out.println(serviceClass.getName());
        ParameterizedType pt = (ParameterizedType) serviceClass.getGenericInterfaces()[0];
        Type type = pt.getActualTypeArguments()[0];
        return (Class<?>) type;
    }

    public static boolean isConstrainedEntityClass(Validator validator, Class<? extends ICdmBase> entityClass) {
        return validator.getConstraintsForClass(entityClass).hasConstraints();
    }


}
