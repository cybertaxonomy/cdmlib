
package eu.etaxonomy.cdm.api.service;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class MediaServiceImplTest extends CdmIntegrationTest{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MediaServiceImplTest.class);

	@SpringBeanByType
	private IMediaService service;
	
	@Before
	public void setUp() throws Exception {
	}
	
	
}
