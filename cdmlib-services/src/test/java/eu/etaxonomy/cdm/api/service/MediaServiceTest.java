// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.MediaDeletionConfigurator;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author k.luther
 * @date 10.03.2016
 *
 */
public class MediaServiceTest extends CdmTransactionalIntegrationTest{

    @SpringBeanByType
    private IMediaService service;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    private final UUID mediaUuid = UUID.fromString("2d357cac-5aba-477e-a8f6-2988f63e8b5b");

    private final UUID mediaUuid2 = UUID.fromString("5c2313fc-9be2-4595-ba23-7e0dd5b46694");


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestDataSet()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
    @Test
    @DataSet
    public final void testDeleteMedia(){
       Media media = service.load(mediaUuid);
       Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
       TaxonDescription description = new TaxonDescription();
       description.setImageGallery(true);
       DescriptionElementBase descriptionElement = TextData.NewInstance(Feature.IMAGE());
       descriptionElement.addMedia(media);

       description.addElement(descriptionElement);
       taxon.addDescription(description);
       taxon = (Taxon)taxonService.save(taxon);
       taxon = (Taxon)taxonService.find(taxon.getUuid());
       description = taxon.getDescriptions().iterator().next();
       UUID descriptionUuid = description.getUuid();
       Assert.assertNotNull(media);

       DeleteResult result = service.delete(mediaUuid, new MediaDeletionConfigurator());
       if (result.isOk()){
           Assert.fail();
       }


       MediaDeletionConfigurator config = new MediaDeletionConfigurator();
       config.setDeleteFrom(taxon);
       config.setDeleteFromDescription(true);


       media = service.find(mediaUuid);
       Assert.assertNotNull(media);
       result = service.delete(mediaUuid, config);
       if (!result.isOk()){
           Assert.fail();
       }
       media = service.find(mediaUuid);
       description = (TaxonDescription)descriptionService.find(descriptionUuid);

       Assert.assertNull(media);
     //  Assert.assertNull(description);
       Assert.assertTrue(description.getElements().isEmpty());
       media = service.find(mediaUuid2);
       Assert.assertNotNull(media);

    }

}
