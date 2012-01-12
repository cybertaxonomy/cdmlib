/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.hibernate.HibernateUnitils;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 04.02.2009
 * @version 1.0
 */
public class TaxonServiceSearchTest extends CdmTransactionalIntegrationTest {
    private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);

    @SpringBeanByType
    private ITaxonService taxonService;
    @SpringBeanByType
    private IReferenceService referenceService;
    @SpringBeanByType
    private IDescriptionService descriptionService;
    @SpringBeanByType
    private INameService nameService;
    @SpringBeanByType
    private ICdmMassIndexer indexer;


    @Test
    public void testDbUnitUsageTest() throws Exception {
        assertNotNull("taxonService should exist", taxonService);
        assertNotNull("nameService should exist", nameService);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}.
     */
    @Test
    @DataSet
    public final void testFindTaxaAndNames() {

        printDataSet(System.err, new String[] {"TaxonBase"});

        ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
        configurator.setTitleSearchString("Abies*");
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(true);
        configurator.setDoSynonyms(true);
        configurator.setDoNamesWithoutTaxa(true);
        configurator.setDoTaxaByCommonNames(true);
        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);
        List<IdentifiableEntity> list = pager.getRecords();

        if (logger.isDebugEnabled()) {
            for (int i = 0; i < list.size(); i++) {
                String nameCache = "";
                if (list.get(i) instanceof NonViralName) {
                    nameCache = ((NonViralName<?>)list.get(i)).getNameCache();
                } else if (list.get(i) instanceof TaxonBase) {
                    TaxonNameBase taxonNameBase= ((TaxonBase)list.get(i)).getName();
                    nameCache = ((NonViralName)taxonNameBase).getNameCache();
                } else {}
                logger.debug(list.get(i).getClass() + "(" + i +")" +
                        ": Name Cache = " + nameCache + ", Title Cache = " + list.get(i).getTitleCache());
            }
        }

        logger.debug("number of taxa: "+list.size());
        assertTrue(list.size()==7);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * .
     */
    @Test
//    @DataSet
    public final void testSearchTaxaByName() {
        logger.warn("testSearchTaxaByName not yet implemented"); // TODO
    }

    @SuppressWarnings("rawtypes")
    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText() throws CorruptIndexException, IOException, ParseException {

        indexer.reindex();
        //
        endTransaction();
        startNewTransaction();

        Pager<SearchResult<TaxonBase>> pager;

        pager = taxonService.findByDescriptionElementFullText(CommonTaxonName.class, "Weiß*", null, null, null, null);
        Assert.assertEquals("Expecting one entity", Integer.valueOf(1),  pager.getCount());
        Assert.assertEquals("Abies alba sec. ", pager.getRecords().get(0).getEntity().getTitleCache());
        Assert.assertEquals("Abies alba sec. ", pager.getRecords().get(0).getDoc().get("inDescription.taxon.titleCache"));

        // modify the taxon
        TaxonBase taxon = pager.getRecords().get(0).getEntity();

        String newName = "Quercus robur";
        // TODO setting the taxon.titleCache indirectly via the taxonName doe not work for some reason ...
//        BotanicalName name = HibernateProxyHelper.deproxy(taxon.getName(), BotanicalName.class);
//        name.setProtectedNameCache(false);
//        name.setGenusOrUninomial("Quercus");
//        name.setSpecificEpithet("robur");
//        name.setNameCache(newNameCache, true);
//        name.setFullTitleCache(newNameCache, true);
//        name.setTitleCache(newNameCache, true);
        // TODO (continued) ... need to set it directly:
        taxon.setTitleCache(newName  + " sec. ", true);

        taxonService.saveOrUpdate(taxon);

        setComplete();
        endTransaction();
        startNewTransaction();

        printDataSet(System.err, new String[] {"TaxonBase", "TaxonNameBase"});

        taxon = taxonService.load(taxon.getUuid());
        Assert.assertEquals(newName + " sec. ", taxon.getTitleCache());

        // test if new titleCache is found in the index, doc and entity
        pager = taxonService.findByDescriptionElementFullText(DescriptionElementBase.class, "Weiß*", null, null, null, null);
        Assert.assertEquals(newName + " sec. ", pager.getRecords().get(0).getEntity().getTitleCache());
        Assert.assertEquals(newName + " sec. ", pager.getRecords().get(0).getDoc().get("inDescription.taxon.titleCache"));

    }


//     @Test
    @DataSet("BlankDataSet.xml")
    public final void createDataSet() {

        Reference sec = ReferenceFactory.newBook();
        referenceService.save(sec);

        BotanicalName n_abies = BotanicalName.NewInstance(Rank.GENUS());
        n_abies.setNameCache("Abies", true);
        Taxon t_abies = Taxon.NewInstance(n_abies, sec);
        taxonService.save(t_abies);

        BotanicalName n_abies_alba = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_alba.setNameCache("Abies alba", true);
        Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
        taxonService.save(t_abies_alba);

        BotanicalName n_abies_balsamea = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_balsamea.setNameCache("Abies balsamea", true);
        Taxon t_abies_balsamea = Taxon.NewInstance(n_abies_balsamea, sec);
        taxonService.save(t_abies_balsamea);

        BotanicalName n_abies_grandis = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_grandis.setNameCache("Abies grandis", true);
        Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
        taxonService.save(t_abies_grandis);

        BotanicalName n_abies_kawakamii = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_kawakamii.setNameCache("Abies kawakamii", true);
        Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
        taxonService.save(t_abies_kawakamii);

        BotanicalName n_abies_subalpina = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_subalpina.setNameCache("Abies subalpina", true);
        Synonym s_abies_subalpina = Synonym.NewInstance(n_abies_subalpina, sec);
        taxonService.save(s_abies_subalpina);

        BotanicalName n_abies_lasiocarpa = BotanicalName.NewInstance(Rank.SPECIES());
        n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
        Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa, sec);
        t_abies_lasiocarpa.addSynonym(s_abies_subalpina, SynonymRelationshipType.SYNONYM_OF());
        taxonService.save(t_abies_lasiocarpa);

        TaxonDescription d_abies_alba = TaxonDescription.NewInstance(t_abies_alba);
        d_abies_alba.addElement(CommonTaxonName.NewInstance("Weißtanne", Language.GERMAN()));
        d_abies_alba.addElement(CommonTaxonName.NewInstance("silver fir", Language.ENGLISH()));

        TaxonDescription d_abies_balsamea = TaxonDescription.NewInstance(t_abies_balsamea);
        d_abies_balsamea.addElement(TextData.NewInstance("Die Balsam-Tanne (Abies balsamea) ist eine Pflanzenart aus der Gattung der Tannen (Abies). Sie wächst im nordöstlichen Nordamerika, wo sie sowohl Tief- als auch Bergland besiedelt. Sie gilt als relativ anspruchslos gegenüber dem Standort und ist frosthart. In vielen Teilen des natürlichen Verbreitungsgebietes stellt sie die Klimaxbaumart dar.", Language.GERMAN(), null));
        d_abies_balsamea.addElement(TextData.NewInstance("Бальзам ньыв (лат. Abies balsamea) – быдмассэзлӧн пожум котырись ньыв увтырын торья вид. Ньывпуыс быдмӧ 14–20 метра вылына да овлӧ 10–60 см кыза диаметрын. Ньывпу пантасьӧ Ойвыв Америкаын.", Language.RUSSIAN(), null));

        descriptionService.save(d_abies_alba);

        setComplete();
        endTransaction();

        printDataSet(System.out, new String[] {"TAXONBASE", "TAXONNAMEBASE", "SYNONYMRELATIONSHIP", "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE", "AGENTBASE", "HOMOTYPICALGROUP"});
    }

}
