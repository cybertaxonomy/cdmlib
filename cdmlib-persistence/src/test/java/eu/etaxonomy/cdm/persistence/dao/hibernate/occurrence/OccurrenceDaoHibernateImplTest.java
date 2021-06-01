package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.h2.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class OccurrenceDaoHibernateImplTest  extends CdmIntegrationTest {

	@SpringBeanByType
	private IOccurrenceDao dao;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//**************** TESTS ************************************************

	@Test
	public void testRebuildIndex() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testOccurrenceDaoHibernateImpl() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountDerivationEvents() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountDeterminations() {
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
	public void testGetDerivationEvents() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetDeterminations() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountClassOfQextendsSpecimenOrObservationBaseTaxonBase() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testListClassOfQextendsSpecimenOrObservationBaseTaxonBaseIntegerIntegerListOfOrderHintListOfString() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetDerivedUnitUuidAndTitleCache() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetFieldUnitUuidAndTitleCache() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testListByAnyAssociation() {
		logger.warn("Not yet implemented");
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

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}