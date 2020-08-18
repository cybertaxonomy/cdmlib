/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.iiif;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.MediaPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController.EntityMediaContext;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.controller.util.IMediaToolbox;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;
/**
 * Serves media lists as iiif manifest files ( for the IIIF Presentation API see https://iiif.io/api/presentation/2.1/#resource-structure).
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/8867
 *
 * @author a.kohlbecker
 * @since Feb 28, 2020
 */
@RestController
@CrossOrigin(origins = "*")
@Api("iiif")
@RequestMapping(value = {"/iiif"}, produces = "application/json; charset=utf-8")
public class ManifestController {

    private static final String HTTP_IIIF_CYBERTAXONOMY_ORG = "http://iiif.cybertaxonomy.org/";

    public static final Logger logger = Logger.getLogger(ManifestController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
        // binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
        // binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
        // binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        // binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<>(termService));
    }

    @Autowired
    private TaxonPortalController taxonPortalController;

    @Autowired
    private ITermService termService;

    @Autowired
    private IMediaToolbox mediaTools;

    @RequestMapping(
            value = {"taxon/{uuid}/manifest"},
            method = RequestMethod.GET)
    public String doTaxonMedia(
                @PathVariable("uuid") UUID uuid,
                @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
                @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
                @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
                @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
                @RequestParam(value = "includeTaxonDescriptions", required = false) Boolean  includeTaxonDescriptions,
                @RequestParam(value = "includeOccurrences", required = false, defaultValue = "false") Boolean  includeOccurrences,
                @RequestParam(value = "includeTaxonNameDescriptions", required = false, defaultValue = "false") Boolean  includeTaxonNameDescriptions,
                @RequestParam(value = "includeTaxonomicChildren", required = false, defaultValue = "false") Boolean  includeTaxonomicChildren,
                HttpServletRequest request, HttpServletResponse response) throws IOException {

            logger.info("doGetMedia() " + AbstractController.requestPathAndQuery(request));

            EntityInitStrategy taxonInitStrategy = includeTaxonomicChildren? TaxonPortalController.TAXON_WITH_CHILDNODES_INIT_STRATEGY : TaxonPortalController.TAXON_INIT_STRATEGY;
            EntityMediaContext<Taxon> entityMediaContext = taxonPortalController.loadMediaForTaxonAndRelated(uuid,
                    relationshipUuids, relationshipInversUuids,
                    includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                    response, taxonInitStrategy.getPropertyPaths(),
                    MediaPortalController.MEDIA_INIT_STRATEGY.getPropertyPaths());

            if(includeTaxonomicChildren){
                Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);
                entityMediaContext.setMedia(
                        taxonPortalController.addTaxonomicChildrenMedia(includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions, entityMediaContext.getEntity(), includeRelationships, entityMediaContext.getMedia())
                                );
            }
            ManifestComposer manifestFactory = new ManifestComposer(HTTP_IIIF_CYBERTAXONOMY_ORG, mediaTools);
            manifestFactory.setDoJoinAttributions(true);
            manifestFactory.setUseThumbnailDimensionsForCanvas(true);
            return serializeManifest(manifestFactory.manifestFor(entityMediaContext, "taxon", uuid.toString()));
    }

    private String serializeManifest(Manifest manifest) throws JsonProcessingException{
        IiifObjectMapper iiifMapper = new IiifObjectMapper();
        return iiifMapper.writeValueAsString(manifest);
    }


}
