/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @author a.kohlbecker
 *
 * @since 24.06.2008
 */
@Service
@Transactional(readOnly = true)
public class DescriptionElementServiceImpl
        extends AnnotatableServiceBase<DescriptionElementBase,IDescriptionElementDao>
        implements IDescriptionElementService {

    private static final Logger logger = Logger.getLogger(DescriptionElementServiceImpl.class);

//    protected IDescriptionElementDao descriptionElementDao;
//    protected ITermTreeDao featureTreeDao;
//    protected IDescriptiveDataSetDao descriptiveDataSetDao;
//    protected ITermNodeDao termNodeDao;
//    protected ITermVocabularyDao vocabularyDao;
//    protected IDefinedTermDao definedTermDao;
//    protected IStatisticalMeasurementValueDao statisticalMeasurementValueDao;
//    protected ITaxonDao taxonDao;
//    protected ITaxonNameDao nameDao;
//    protected IOccurrenceDao occurrenceDao;
//    protected ITaxonNodeDao taxonNodeDao;
//    protected IDescriptiveDataSetDao dataSetDao;
//    protected ITermService termService;

    //TODO change to Interface
    private NaturalLanguageGenerator naturalLanguageGenerator;


    @Override
    @Autowired
    protected void setDao(IDescriptionElementDao dao) {
        this.dao = dao;
    }
//
//    @Autowired
//    protected void setFeatureTreeDao(ITermTreeDao featureTreeDao) {
//        this.featureTreeDao = featureTreeDao;
//    }
//
//    @Autowired
//    protected void setDescriptiveDataSetDao(IDescriptiveDataSetDao descriptiveDataSetDao) {
//        this.descriptiveDataSetDao = descriptiveDataSetDao;
//    }
//
//    @Autowired
//    protected void setTermNodeDao(ITermNodeDao featureNodeDao) {
//        this.termNodeDao = featureNodeDao;
//    }
//
//    @Autowired
//    protected void setVocabularyDao(ITermVocabularyDao vocabularyDao) {
//        this.vocabularyDao = vocabularyDao;
//    }
//
//    @Autowired
//    protected void setDefinedTermDao(IDefinedTermDao definedTermDao) {
//        this.definedTermDao = definedTermDao;
//    }
//
//    @Autowired
//    protected void setTermService(ITermService definedTermService) {
//        this.termService = definedTermService;
//    }
//
//    @Autowired
//    protected void statisticalMeasurementValueDao(IStatisticalMeasurementValueDao statisticalMeasurementValueDao) {
//        this.statisticalMeasurementValueDao = statisticalMeasurementValueDao;
//    }
//
//    @Autowired
//    protected void setDescriptionElementDao(IDescriptionElementDao descriptionElementDao) {
//        this.descriptionElementDao = descriptionElementDao;
//    }

    @Autowired
    protected void setNaturalLanguageGenerator(NaturalLanguageGenerator naturalLanguageGenerator) {
        this.naturalLanguageGenerator = naturalLanguageGenerator;
    }

//    @Autowired
//    protected void setTaxonDao(ITaxonDao taxonDao) {
//        this.taxonDao = taxonDao;
//    }
//
//    @Autowired
//    protected void setTaxonNodeDao(ITaxonNodeDao taxonNodeDao) {
//        this.taxonNodeDao = taxonNodeDao;
//    }
//
//    @Autowired
//    protected void setDataSetDao(IDescriptiveDataSetDao dataSetDao) {
//        this.dataSetDao = dataSetDao;
//    }

    public DescriptionElementServiceImpl() {
        if (logger.isDebugEnabled()){logger.debug("Load DescriptionElementService Bean");}
    }


    @Override
    public Pager<Annotation> getDescriptionElementAnnotations(DescriptionElementBase annotatedObj, MarkerType status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths){
        long numberOfResults = dao.countAnnotations(annotatedObj, status);

        List<Annotation> results = new ArrayList<>();
        if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
            results = dao.getAnnotations(annotatedObj, status, pageSize, pageNumber, orderHints, propertyPaths);
        }

        return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
    }

}
