/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.sdd.in;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * Lightweight timing harness for SDD import (replaces abandoned junit-benchmarks / JUnit 4 {@code @Rule}).
 *
 * @author b.clark
 * @since 20.01.2009
 */
public class SDDImportBenchmark extends CdmIoTransactionalSpringIntegrationTest {

    @Autowired
    private SDDImport sddImport;

    @Autowired
    private INameService nameService;

    private SDDImportConfigurator configurator;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/Cichorieae-DA.sdd.xml");
        assertNotNull(url);
        configurator = SDDImportConfigurator.NewInstance(URI.fromUrl(url), null);
    }

    @Test
    public void testInit() {
        assertNotNull(sddImport, "sddImport should not be null");
        assertNotNull(nameService, "nameService should not be null");
    }

    /**
     * Manual timing only: imports the large Cichorieae-DA SDD twice and can take many minutes.
     */
    @Disabled("Manual benchmark against Cichorieae-DA.sdd.xml; too slow for the default suite")
    @Test
    public void testDoInvoke() {
        final int rounds = 2;
        for (int i = 1; i <= rounds; i++) {
            long start = System.nanoTime();
            sddImport.doInvoke(new SDDImportState(configurator));
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            logger.info("testDoInvoke round " + i + "/" + rounds + ": " + elapsedMs + " ms");
        }
        this.setComplete();
        this.endTransaction();
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
