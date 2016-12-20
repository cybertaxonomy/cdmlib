/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.suite;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.etaxonomy.cdm.io.excel.taxa.NormalExplicitImportTest;
import eu.etaxonomy.cdm.io.jaxb.CdmImporterTest;
import eu.etaxonomy.cdm.io.sdd.in.SDDImportTest;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportConfiguratorTest;
import eu.etaxonomy.cdm.io.specimen.excel.in.ExcelImportConfiguratorTest;
import eu.etaxonomy.cdm.io.specimen.excel.in.ExcelSpecimenImportExampleTest;


@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
        	NormalExplicitImportTest.class,
        	CdmImporterTest.class,
        	SDDImportTest.class,
            SpecimenImportConfiguratorTest.class,
            ExcelImportConfiguratorTest.class,
            ExcelSpecimenImportExampleTest.class
        }
    )
public class CdmTestSuite {
    @SuppressWarnings("unused")
    private static final  Logger logger = Logger.getLogger(CdmTestSuite.class);

    // the class remains completely empty,
    // being used only as a holder for the above annotations
}
