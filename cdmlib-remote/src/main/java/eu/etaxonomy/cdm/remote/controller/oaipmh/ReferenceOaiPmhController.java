/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.oaipmh;

import static eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix.DWC;
import io.swagger.annotations.Api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.remote.controller.IdDoesNotExistException;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;

@Controller
@Api("OAI-PMH References")
@RequestMapping(value = "/reference/oai", params = "verb")
public class ReferenceOaiPmhController extends AbstractOaiPmhController<Reference, IReferenceService> {

    @Override
    protected List<String> getPropertyPaths() {
        return Arrays.asList(new String []{
                "$",
                "inBook.authorship",
                "inJournal",
                "inProceedings",
        });
    }

    private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "titleCache",
            "name.titleCache",
            "name.nomenclaturalReference.titleCache",
            "$"
            });

    @Override
    protected void addSets(ModelAndView modelAndView) {
        Set<SetSpec> sets = new HashSet<SetSpec>();
        sets.add(SetSpec.REFERENCE);
        modelAndView.addObject("sets",sets);
    }

    @Override
    @Autowired
    public void setService(IReferenceService service) {
        this.service = service;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.AbstractOaiPmhController#finishModelAndView(eu.etaxonomy.cdm.model.common.LSID, eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix, org.springframework.web.servlet.ModelAndView)
     */
    @Override
    protected void finishModelAndView(LSID identifier,
            MetadataPrefix metadataPrefix, ModelAndView modelAndView)
            throws IdDoesNotExistException {

        if(metadataPrefix.equals(DWC)){
            modelAndView.addObject("entitylist", obtainCoveredTaxaList(identifier, metadataPrefix));
            modelAndView.setViewName("oai/getRecord.dwc");
        } else {
            super.finishModelAndView(identifier, metadataPrefix, modelAndView);
        }
    }

    /**
     * @param identifier
     * @param metadataPrefix
     * @return
     * @throws IdDoesNotExistException
     */
    private  List<TaxonBase> obtainCoveredTaxaList(
            LSID identifier, MetadataPrefix metadataPrefix)
            throws IdDoesNotExistException {

        AuditEventRecord<Reference> auditEventRecord = obtainCdmEntity(identifier);
        Reference reference = auditEventRecord.getAuditableObject();
        List<TaxonBase> list = service.listCoveredTaxa(reference, true, TAXON_INIT_STRATEGY);
        return list;
    }



}
