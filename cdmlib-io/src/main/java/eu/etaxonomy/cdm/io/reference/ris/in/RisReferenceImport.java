/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 11.05.2017
 *
 */
@Component
public class RisReferenceImport
    extends CdmImportBase<RisReferenceImportConfigurator, RisReferenceImportState>{

    private static final long serialVersionUID = 7022034669942979722L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RisReferenceImport.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(RisReferenceImportState state) {
        RisReferenceImportConfigurator config = state.getConfig();
        try {
//            new FileReader(file)
            InputStreamReader inputReader = config.getSource();
            RisRecordReader risReader = new RisRecordReader(state, inputReader);

            Set<Reference> referencesToSave = new HashSet<>();


            Map<RisReferenceTag, String> next = risReader.readRecord();
            while (next != RisRecordReader.EOF){
                Reference ref = makeReference(next);
                referencesToSave.add(ref);
                next = risReader.readRecord();
            }

            getReferenceService().saveOrUpdate(referencesToSave);

        } catch (Exception e) {
            String message = "Unexpected exception during RIS Reference Import";
            state.getResult().addException(e, message);
        }
    }

    /**
     * @param next
     * @return
     */
    private Reference makeReference(Map<RisReferenceTag, String> record) {
        ReferenceType type = makeReferenceType(record);
        Reference result = ReferenceFactory.newReference(type);
        ReferenceType inRefType = type == ReferenceType.Article ? ReferenceType.Journal : ReferenceType.Generic;
        Reference inRef = ReferenceFactory.newReference(inRefType);

        for (RisReferenceTag tag : record.keySet()){
            String value = record.get(tag);

            if (isNotBlank(value)){
                switch (tag) {
                    case T1:
                        result.setTitle(value);
                        break;
                    case AU:
                        Person author = Person.NewTitledInstance(value);
                        result.setAuthorship(author);
                        break;
                    case Y1:
                        TimePeriod y1 = TimePeriodParser.parseString(value);
                        result.setDatePublished(y1);
                        break;
                    case PY:
                        //TODO
                        TimePeriod py = TimePeriodParser.parseString(value);
                        result.setDatePublished(py);
                        break;
                    case DA:
                        //TODO
                        TimePeriod da = TimePeriodParser.parseString(value);
                        result.setDatePublished(da);
                        break;
                    case N1:
                        Annotation annotation = Annotation.NewInstance(value, AnnotationType.EDITORIAL(), Language.DEFAULT());
                        result.addAnnotation(annotation);
                        break;
                    case DO:
                        DOI doi = DOI.fromString(value);
                        result.setDoi(doi);
                        break;
                    case T2:
                        inRef.setTitle(value);
                        break;
                    case JF:
                        inRef.setTitle(value);
                        break;
                    case JO:
                        inRef.setTitle(value);
                        break;
                    case SP:
                        String startPage = value;
                        result.setPages(startPage);
                        break;
                    case EP:
                        String endPage = value;
                        result.setPages(endPage);
                        break;
                    case VL:
                        String volume = value;
                        result.setVolume(volume);
                        break;
                    case IS:
                        String issueNumber = value;
                        result.setVolume(issueNumber);
                        break;
                    case PB:
                        String publisher = value;
                        result.setPublisher(publisher);
                        break;
                    case N2:
                        String n2Str = value;
                        result.setReferenceAbstract(n2Str);
                        break;
                    case AB:
                        String abstractStr = value;
                        result.setReferenceAbstract(abstractStr);
                        break;
                    case SN:
                        String issn = value;
                        result.setIssn(issn);
                        break;
                    default:
                        //TODO
                        break;
                }
            }else {
                //TODO isBlank
            }

        }
        return result;
    }

    /**
     * @param record
     * @return
     */
    private ReferenceType makeReferenceType(Map<RisReferenceTag, String> record) {
        RisReferenceTag tyTag = RisReferenceTag.TY;
        String typeStr = record.get(tyTag);
        RisRecordType type = RisRecordType.valueOf(typeStr);
        ReferenceType cdmType = type.getCdmReferenceType();
        record.remove(tyTag);
        return cdmType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(RisReferenceImportState state) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(RisReferenceImportState state) {
        return false;
    }
}
