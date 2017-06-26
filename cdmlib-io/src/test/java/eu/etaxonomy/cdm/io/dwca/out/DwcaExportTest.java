/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.TARGET;
import eu.etaxonomy.cdm.io.dwca.in.DwcaImportIntegrationTest;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 25.06.2017
 *
 */
public class DwcaExportTest  extends CdmTransactionalIntegrationTest{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DwcaImportIntegrationTest.class);

    @SpringBeanByName
    private CdmApplicationAwareDefaultExport<DwcaTaxExportConfigurator> defaultExport;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @Test
    public void test(){
        try {
            createTestDataSet();
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
        File destinationFolder = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(null, destinationFolder, null);
        config.setTarget(TARGET.EXPORT_DATA);
        ExportResult result = defaultExport.invoke(config);
        System.out.println(result.createReport());
        ExportDataWrapper<?> data = result.getExportData();
        Map<String, byte[]> dat = (Map<String, byte[]>) data.getExportData();
        for (String key : dat.keySet()){
            byte[] byt = dat.get(key);
            System.out.println(key + ": " + new String(byt) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        Reference sec1 = ReferenceFactory.newGeneric();

        Classification classification = Classification.NewInstance("DwcaExportTest Classificaiton");

        NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
        TaxonName name1 = parser.parseReferencedName("Genus species subsp. subspec Mill., The book of botany 3: 22. 1804",
                NomenclaturalCode.ICNAFP, Rank.SUBSPECIES());
        Taxon taxon = Taxon.NewInstance(name1, sec1);

        TaxonNode node1 = classification.addChildTaxon(taxon, sec1, "22");

        classificationService.save(classification);
        taxonNodeService.save(node1);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);

        Distribution distribution = Distribution.NewInstance(NamedArea.AFRICA(), PresenceAbsenceTerm.PRESENT());
        description.addElement(distribution);
        commitAndStartNewTransaction(null);

//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "Classification",
//            }, "testAttachDnaSampleToDerivedUnit");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

}
