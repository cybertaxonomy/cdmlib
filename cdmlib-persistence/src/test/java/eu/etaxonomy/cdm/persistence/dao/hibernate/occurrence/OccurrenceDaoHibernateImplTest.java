/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class OccurrenceDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
	private IOccurrenceDao dao;

//**************** TESTS ************************************************

	@Test
	public void testRebuildIndex() {
		logger.warn("Not yet implemented");
	}

    @Test
    public void testCountMedia() {
        MediaSpecimen unit = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        SpecimenDescription desc = SpecimenDescription.NewInstance(unit);
        desc.setImageGallery(true);
        TextData textData = TextData.NewInstance(Feature.IMAGE());
        desc.addElement(textData);
        Media media1 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        Media media2 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        textData.addMedia(media1);
        textData.addMedia(media2);

        Media media3 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        unit.setMediaSpecimen(media3);
        dao.save(unit);

        Assert.assertEquals(3, dao.countMedia(unit));
        unit.setMediaSpecimen(media2);
        Assert.assertEquals(2, dao.countMedia(unit));
    }

    @Test
    public void testGetMedia() {
        MediaSpecimen unit = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
        SpecimenDescription desc = SpecimenDescription.NewInstance(unit);
        desc.setImageGallery(true);
        TextData textData = TextData.NewInstance(Feature.IMAGE());
        desc.addElement(textData);
        Media media1 = Media.NewInstance(URI.create("https://www.abc.de"), 5, "jpg", "jpg");
        Media media2 = Media.NewInstance(URI.create("https://www.defg.de"), 5, "jpg", "jpg");
        textData.addMedia(media1);
        textData.addMedia(media2);

        Media media3 = Media.NewInstance(URI.create("https://www.hij.de"), 5, "jpg", "jpg");
        unit.setMediaSpecimen(media3);
        dao.save(unit);

        List<Media> media = dao.getMedia(unit, null, null, null);
        Assert.assertEquals(3, media.size());
        //test that paging works (note: the sorting is not a requirement for the method, but with current implementation it works; if implementation is changed the test may need to be adapted
        List<Integer> ids = media.stream().map(m->m.getId()).collect(Collectors.toList());
        Collections.sort(ids);
        Assert.assertEquals(ids.get(0), (Integer)media.get(0).getId());
        Assert.assertEquals(ids.get(1), (Integer)media.get(1).getId());
        Assert.assertEquals(ids.get(2), (Integer)media.get(2).getId());
        media = dao.getMedia(unit, 2, 1, null);
        Assert.assertEquals(1, media.size());
        Assert.assertEquals(ids.get(2), (Integer)media.get(0).getId());

        //test deduplication
        unit.setMediaSpecimen(media2);
        Assert.assertEquals(2, dao.countMedia(unit));
    }

	@Test
	public void testSaveOriginalLabelData(){
		DerivedUnit unit = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
		String originalLabelInfo = StringUtils.pad("my original info", 10000, "x", false);
		Assert.assertEquals(Integer.valueOf(10000),  (Integer)originalLabelInfo.length());
		unit.setOriginalLabelInfo(originalLabelInfo);
		//test that lob is supported
		dao.save(unit);
		//assert no exception
	}

	@Test
	@DataSet
	public void testStatusWithoutTypeCanBeLoaded() {
	    //This is for testing if an occurrence status can be loaded if it has
	    //no type although OccurrenceStatus.type has a NotNull constraint.
	    //Result: it can be loaded and during update an constraint violation
	    //only takes place if the occurrence status record itself is changed.
	    //I tested with changing the specimen or adding an annotation to the
	    //occurrence status with no constraint violation observed.
	    UUID uuid = UUID.fromString("07e70de7-680b-48c1-9c94-50c1aa470a92");
	    DerivedUnit specimen = (DerivedUnit)dao.load(uuid);
	    Assert.assertNotNull(specimen);
	    OccurrenceStatus status = specimen.getStatus().iterator().next();
	    Assert.assertNotNull(status);
	    Assert.assertNull(status.getType());

	    //guarantee that a constraint still exists on OccurrenceStatus.type
	    status.setCitationMicroReference("222");
	    dao.saveOrUpdate(specimen);
	    try {
            commitAndStartNewTransaction(new String[]{"OccurrenceStatus","OccurrenceStatus_Annotation","Annotation","SpecimenOrObservationBase"});
            Assert.fail("Updating status without type should throw an exception");
        } catch (ConstraintViolationException e) {
            //constraint violation expected
            Assert.assertEquals(1, e.getConstraintViolations().size());
            Assert.assertEquals("Occurrence status must have a type defined", e.getConstraintViolations().iterator().next().getMessage());
        }
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}