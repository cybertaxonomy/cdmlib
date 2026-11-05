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

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TestRule;
import org.springframework.beans.factory.annotation.Autowired;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author b.clark
 * @since 20.01.2009
 */
public class SDDImportBenchmark extends CdmIoTransactionalSpringIntegrationTest {

    @Autowired
    private SDDImport sddImport;

    @Autowired
    private INameService nameService;

    @Rule
    //was of Type MethodRule before but this was not implemented in junitbenchmarks 0.5.0 anymore
	public TestRule benchmarkRun = new BenchmarkRule();

    private SDDImportConfigurator configurator;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/Cichorieae-DA.sdd.xml");
        assertNotNull(url);
        configurator = SDDImportConfigurator.NewInstance(URI.fromUrl(url), null);
    }

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void testInit() {
        assertNotNull(sddImport, "sddImport should not be null");
        assertNotNull(nameService, "nameService should not be null");
    }

    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 0)
    @Test
    public void testDoInvoke() {
        sddImport.doInvoke(new SDDImportState(configurator));
        this.setComplete();
        this.endTransaction();
    }

    @Override    public void createTestDataSet() throws FileNotFoundException {}
}