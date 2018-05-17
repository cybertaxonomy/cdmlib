/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 16.11.2015
 */
public class AdvancedBeanInitializerTest extends CdmTransactionalIntegrationTest {

    private static final Logger logger = Logger.getLogger(AdvancedBeanInitializerTest.class);

    private static final UUID personUuid = UUID.fromString("d0568bb1-4dc8-40dc-a405-d0b9e714a7a9");

    private static final UUID teamUuid = UUID.fromString("f2ab0cab-f8a4-4db0-9f2d-2f0a1b627597");

    private static final UUID referenceUuid = UUID.fromString("f48196c6-854a-416e-8f2a-67bd39e988dc");

    private static final UUID nameUuid = UUID.fromString("98cbb643-d521-4ca7-86f7-8180bea85d9f");

    private static final UUID taxonUuid = UUID.fromString("07171a4c-f9f0-4459-a7e4-9f75981f7027");

    @SpringBeanByType
    private IAgentDao agentDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

    @SpringBeanByType
    private ITaxonNameDao nameDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    @SpringBeanByType
    private AdvancedBeanInitializer initializer;

    @SpringBeanByType
    private AdvancedBeanInitializer defaultBeanInitializer;

    @SpringBeanByType
    private SessionFactory factory;

    private Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> deacivatedAutoIntitializers;

    /**
     * Checks that the AdvancedBeanInitializer is available and that the expected set of beanAutoInitializers is configured
     * in the persitence.xml.

     */
    @Before
    public void assertAutoinitializers(){

        assert defaultBeanInitializer != null;

        Class[] expectedAutoInitializers = new Class[]{
            eu.etaxonomy.cdm.persistence.dao.initializer.TitleAndNameCacheAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.AnnotationTypeAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.MarkerTypeAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.GatheringEventLocationAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.TermBaseAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.MediaAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.TypeDesignationAutoInitializer.class,
            eu.etaxonomy.cdm.persistence.dao.initializer.TeamAutoInitializer.class
            };

        Set<Class> checkSet = new HashSet<>(Arrays.asList(expectedAutoInitializers));

        for(AutoPropertyInitializer api : defaultBeanInitializer.getBeanAutoInitializers().values()){
            assert checkSet.remove(api.getClass()) == true;
        }
        assert checkSet.size() == 0;
    }

    @After
    public void restoreAutoinitializers() {
        if(deacivatedAutoIntitializers != null){
            defaultBeanInitializer.getBeanAutoInitializers().putAll(deacivatedAutoIntitializers);
            deacivatedAutoIntitializers = null;
        }
    }

    @DataSet
    @Test
    public void testContact() {
        Person person = (Person)agentDao.findByUuid(personUuid);

        final List<String> propPath = Arrays.asList(new String[]{
            "contact.urls",
            "contact.phoneNumbers",
            "contact.addresses",
            "contact.faxNumbers",
            "contact.emailAddresses",
        });
        initializer.initialize(person, propPath);
    }

    @DataSet
    @Test
    public void testFullNameGraphWithPreloadedReference() {
        // find the reference by iD (not load!)
        Reference ref = referenceDao.findById(5000);
        TaxonName name = nameDao.findById(5000);
        assertFalse("for this test to be significant the authorship must be uninitialized", Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
        initializer.initialize(name, Arrays.asList(new String[]{"nomenclaturalReference.authorship.$"}));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
    }


    @DataSet
    @Test
    public void testToOneWildcard() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        TaxonName name = nameDao.load(nameUuid, Arrays.asList("$"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference()));
        assertFalse(Hibernate.isInitialized(name.getAnnotations()));

        name = nameDao.load(nameUuid, Arrays.asList("nomenclaturalReference.$"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference()));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
        assertFalse(Hibernate.isInitialized(name.getNomenclaturalReference().getAnnotations()));
    }

    @DataSet
    @Test
    @Ignore // TODO fix #7375
    public void testComplexPath() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        TaxonName name = nameDao.load(nameUuid, Arrays.asList("nomenclaturalReference.$.*.contact.faxNumbers"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference())); // nomenclaturalReference
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship())); // $
        assertFalse("must not be initialized by 'nomenclaturalReference.$'", Hibernate.isInitialized(name.getNomenclaturalReference().getExtensions()));
        Team team = HibernateProxyHelper.deproxy(name.getNomenclaturalReference().getAuthorship(), Team.class);
        assertTrue(Hibernate.isInitialized(team.getTeamMembers())); // *
        Person person1 = HibernateProxyHelper.deproxy(team.getTeamMembers().get(0), Person.class);
        assertEquals(personUuid, person1.getUuid());
        assertTrue(Hibernate.isInitialized(person1.getContact())); // contact
        assertFalse("must not be initialized by 'nomenclaturalReference.$.*.contact'", Hibernate.isInitialized(person1.getAnnotations()));
        assertTrue(Hibernate.isInitialized(person1.getContact().getFaxNumbers())); // * // FIXME fails here #7375
    }

    @DataSet
    @Test
    public void testPersonContacts() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        Person person1 = (Person) agentDao.load(personUuid, Arrays.asList("contact.faxNumbers"));
        assertTrue(Hibernate.isInitialized(person1.getContact()));
        assertTrue(Hibernate.isInitialized(person1.getContact().getFaxNumbers()));
    }

    @DataSet
    @Test
    public void testToOneWildcardDepth1() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        TaxonName name = nameDao.load(nameUuid, Arrays.asList("nomenclaturalReference.$"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference()));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
        assertFalse(Hibernate.isInitialized(name.getNomenclaturalReference().getAnnotations()));

    }

    @DataSet
    @Test
    public void testToManyWildcard() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        TaxonName name = nameDao.load(nameUuid, Arrays.asList("*"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference()));
        assertTrue(Hibernate.isInitialized(name.getAnnotations()));

    }

    @DataSet
    @Test
    public void testToManyWildcardDepth1() {

        deacivatedAutoIntitializers = clearAutoinitializers();
        assureSessionClear();

        TaxonName name = nameDao.load(nameUuid, Arrays.asList("nomenclaturalReference.*"));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference()));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship()));
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAnnotations()));

    }

    @DataSet
    @Test
    public void testTitleAndNameCacheAutoInitializer() {

        assureSessionClear();

        Logger.getLogger(AdvancedBeanInitializer.class).setLevel(Level.TRACE);

        Taxon taxon = (Taxon)taxonDao.load(taxonUuid, Arrays.asList("$"));
        assertTrue(Hibernate.isInitialized(taxon.getName()));
        TaxonName name = taxon.getName();
        // the TitleAndNameCacheAutoInitializer must not intitialize the nomenclaturalReference
        // since the authorship is only taken from the combinationAutors field
        assertFalse(Hibernate.isInitialized(name.getNomenclaturalReference()));
    }

    @DataSet
    @Test
    public void testTeamAutoInitializer() {

        assureSessionClear();

        Logger.getLogger(AdvancedBeanInitializer.class).setLevel(Level.TRACE);

        deacivatedAutoIntitializers = clearAutoinitializers();
        // load bean with autoinitializers deactivated
        factory.getCurrentSession().setFlushMode(FlushMode.MANUAL); // TODO this is only needed due to #7377 and should be removed otherwise
        Taxon taxon = (Taxon)taxonDao.load(taxonUuid, Arrays.asList("name.nomenclaturalReference.authorship"));
        assertTrue(Hibernate.isInitialized(taxon.getName())); // name
        TaxonName name = taxon.getName();
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference())); // nomenclaturalReference
        assertTrue(Hibernate.isInitialized(name.getNomenclaturalReference().getAuthorship())); // authorship
        Team team = HibernateProxyHelper.deproxy(name.getNomenclaturalReference().getAuthorship(), Team.class);

        // FIXME : the below assertion fail due to #7377 if the session flushmode is AUTO, this is not critical but an inconsistency.
        //    In AdvancedBeanInitializer.bulkLoadLazyBeans(BeanInitNode node) the query.list()
        //    with "QueryImpl( SELECT c FROM TeamOrPersonBase as c  WHERE c.id IN (:idSet) )" triggers an autoFlush.
        //    In turn of the autoflush the team.titleCache is generated which causes the teamMembers to be initialized
        //
        // members should not initialized since they where not included in the property path
        assertFalse("members should not intitialized since they where not included in the property path", Hibernate.isInitialized(team.getTeamMembers()));

        // activate the teamAutoInitializer again
        AutoPropertyInitializer<CdmBase> teamAutoInitializer = deacivatedAutoIntitializers.get(TeamOrPersonBase.class);
        deacivatedAutoIntitializers.remove(teamAutoInitializer);
        defaultBeanInitializer.getBeanAutoInitializers().put(TeamOrPersonBase.class, teamAutoInitializer);

        taxon = (Taxon)taxonDao.load(taxonUuid, Arrays.asList("name.nomenclaturalReference.authorship"));

        team = HibernateProxyHelper.deproxy(name.getNomenclaturalReference().getAuthorship(), Team.class);
        assertTrue("members should have been intitialized by the ", Hibernate.isInitialized(team.getTeamMembers()));

    }

    // ============================== end of tests ========================= //

    /**
     * @return
     */
    protected Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> clearAutoinitializers() {
        Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> autoIntitializers = new HashMap<>(defaultBeanInitializer.getBeanAutoInitializers());
        defaultBeanInitializer.getBeanAutoInitializers().clear();
        return autoIntitializers;
    }


    /**
     *
     */
    protected void assureSessionClear() {
        try {
            factory.getCurrentSession().clear();
            logger.debug("session cleared");
        } catch (HibernateException e){
            logger.debug("no session");
            // IGNORE no session
        }
    }


    @Override
    // @Test
    public void createTestDataSet() throws FileNotFoundException {
        // 1. create person and a reference
        Person person1 = Person.NewTitledInstance("A. Adonis");
        Person person2 = Person.NewTitledInstance("B. Belalugosi");
        Team team = Team.NewInstance();
        team.setUuid(teamUuid);
        team.addTeamMember(person1);
        team.addTeamMember(person2);
        Set<Address> addresses = new HashSet<Address>();
        addresses.add(Address.NewInstance(Country.GERMANY(), "locality", "pobox", "postcode", "region", "street", Point.NewInstance(50.02,33.3, ReferenceSystem.GOOGLE_EARTH(), 3)));
        List<String> emailAddresses = new ArrayList<String>();
        emailAddresses.add("My.email@web.de");
        List<String> faxNumbers = new ArrayList<String>();
        faxNumbers.add("0049-30-1234545");
        List<String> phoneNumbers = new ArrayList<String>();
        phoneNumbers.add("0049-30-1234546");
        List<URI> urls = new ArrayList<URI>();
        urls.add(URI.create("http://www.test.de"));
        Contact contact = Contact.NewInstance(addresses, emailAddresses, faxNumbers, phoneNumbers, urls);

        person1.setContact(contact);
        person1.setUuid(personUuid);
        person1 = (Person)agentDao.save(person1);
        person2 = (Person)agentDao.save(person2);
        team = (Team)agentDao.save(team);

        Reference ref = ReferenceFactory.newBook();
        ref.setUuid(referenceUuid);
        ref.setAuthorship(team);
        ref.setTitle("The Book");
        referenceDao.save(ref);

        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setUuid(nameUuid);
        name.setNomenclaturalReference(ref);
        name.setTitleCache("Species testii", true);
        nameDao.save(name);

        Taxon taxon = Taxon.NewInstance(name, null);
        taxon.setUuid(taxonUuid);
        taxonDao.save(taxon);

        // 2. end the transaction so that all data is actually written to the db
        setComplete();
        endTransaction();

        // use the fileNameAppendix if you are creating a data set file which need to be named differently
        // from the standard name. For example if a single test method needs different data then the other
        // methods the test class you may want to set the fileNameAppendix when creating the data for this method.
        String fileNameAppendix = null;

        // 3.
        writeDbUnitDataSetFile(new String[] {
            "ADDRESS", "AGENTBASE","AgentBase_contact_emailaddresses",
            "AgentBase_contact_faxnumbers","AgentBase_contact_phonenumbers",
            "AgentBase_contact_urls","AgentBase_Address", "AgentBase_AgentBase",
            "REFERENCE", "TaxonName", "HomotypicalGroup", "TaxonBase",
            "",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix );

    }

}
