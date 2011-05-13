package eu.etaxonomy.cdm.api.service.security;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

import eu.etaxonomy.cdm.api.application.CdmApplicationRemoteController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class TestCdmAppicationContextSecurity {

	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name",
			// the name
			"name.$",
			"name.rank.representations",
			"name.status.type.representations",

			// taxon descriptions
			"descriptions.elements.area.$",
			"descriptions.elements.multilanguageText",
			"descriptions.elements.media.representations.parts",
			"descriptions.elements.media.title",
			// supplemental
			"annotations"
	});

	private ICdmApplicationRemoteConfiguration appCtr;
	private static final Logger logger = Logger.getLogger(TestCdmAppicationContextSecurity.class);

	@Before
	public void setUp() {
		
		Resource applicationContextResource = new ClassPathResource("/eu/etaxonomy/cdm/remotingApplicationContext.xml");
		appCtr = CdmApplicationRemoteController.NewInstance(applicationContextResource, null);
		
        String user = "admin"; // Role:ROLE_ADMIN, Role:ROLE_USER
        String pwd = "0000";

        Authentication auth = new UsernamePasswordAuthenticationToken(user, pwd);
        SecurityContextImpl sc = new SecurityContextImpl();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);
        logger.info("setting up security context");
	}

	@Test 
	public void testUserService() {
		
		IUserService userService = appCtr.getUserService();
		UserDetails userDetails = userService.loadUserByUsername("admin");
		String username = userDetails.getUsername();
		String pwd = userDetails.getPassword();
		logger.info("userDetails for " + username + ": '" + pwd + "'");
	}

	@Test
	public void testAddAnnotationToTaxon(){
		
      ITaxonService taxonService = appCtr.getTaxonService();		
      UUID uuid = UUID.fromString("7267e704-e0e8-41ee-8112-5aa22c61dea9");
      TaxonBase taxon = taxonService.load(uuid, TAXON_INIT_STRATEGY);

      Annotation annotation = Annotation.NewDefaultLanguageInstance("Test");

      taxon.addAnnotation(annotation);

      taxonService.saveOrUpdate(taxon);
	}

	@Test
	public void testGetTaxon() {
		List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
				//"*",
				"synonymRelations.synonym.name",
				"relationsToThisTaxon",
				"relationsFromThisTaxon",
				// taxon relations 
				"relationsToThisName.fromTaxon.name",
				// the name
				"name.descriptions.elements",
				"name.typeDesignations", 
				"name.relationsToThisName",
				"name.relationsFromThisName",
				"name.homotypicalGroup.typifiedNames",
				"name.rank.representations",
				"name.status.type.representations",
				// name supplemental
				"name.annotations",
				"name.markers",
				"name.credits",
				"name.extensions",
				"name.rights",
				"name.sources",
				// taxon descriptions
				"descriptions.elements.area.$",
				"descriptions.elements.multilanguageText",
				"descriptions.elements.media.representations.parts",
				"descriptions.elements.media.title",
				// supplemental
				"annotations",
				"markers",
				"credits",
				"extensions",
				"rights",
				"sources"
				});
		
		ITaxonService taxonService = appCtr.getTaxonService();
		UUID uuid = UUID.fromString("9763e5f0-6cd4-4d96-b8a4-4420854f7727");
		TaxonBase taxon = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		logger.info("taxonName=" + taxon.getName());
		
		TaxonNameBase name = taxon.getName();
	}

	@Test 
	public void testEditTaxonName() {
		ITaxonService taxonService = appCtr.getTaxonService();		
		UUID uuid = UUID.fromString("7267e704-e0e8-41ee-8112-5aa22c61dea9");
		TaxonBase taxon = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		logger.info("taxonName=" + taxon.getName());
		
		TaxonNameBase name = taxon.getName();
		
		String nameString = "Malvaceaeyyyyy";
		name.setTitleCache(nameString);
		
		taxonService.saveOrUpdate(taxon);
		
		TaxonBase taxon2 = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		Assert.assertEquals(nameString, taxon2.getName().getTitleCache());
	}

	@Test 
	public void testEditTaxonNameConcurrent() {
		ITaxonService taxonService = appCtr.getTaxonService();		
		UUID uuid = UUID.fromString("7267e704-e0e8-41ee-8112-5aa22c61dea9");
		
		// Session 1 aktiv
		TaxonBase taxon1 = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		logger.info("taxonName=" + taxon1.getName());
		
		// Session 2 aktiv
		TaxonBase taxon2 = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		TaxonNameBase name2 = taxon2.getName();
		
		
		// Session 1 aktiv
		TaxonNameBase name1 = taxon1.getName();
		
		Assert.assertEquals(name1, name2);
		
		String nameString = "Malvaceae";
		name1.setTitleCache(nameString);
		taxonService.saveOrUpdate(taxon1);
		
		// Session 2 aktiv
		System.out.println("The names are the same: " + name1.getTitleCache().equals(name2.getTitleCache()));
		
		
	}

	@Test
	public void testClassificationData() {
		IClassificationService classificationService = appCtr.getClassificationService();
		String uuidString = "0c2b5d25-7b15-4401-8b51-dd4be0ee5cab"; // true: "0c2b5d25-7b15-4401-8b51-dd4be0ee5cab"
		UUID uuid = UUID.fromString(uuidString);
		boolean exists = classificationService.exists(uuid);
		logger.info("classificationService with uuid='" + uuid + "' exists=" + exists);
	}

//	@Test
//	public void testNewDatabaseConnection(){
//		boolean omitTermLoading = false;
//		Resource applicationContextResource = new ClassPathResource("/eu/etaxonomy/cdm/remotingApplicationContext.xml");
//		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance("localhost", "cdm_demo_cyprus", "root", "juergen1");
//		CdmApplicationController appCtr = CdmApplicationController.NewInstance(applicationContextResource, dataSource, DbSchemaValidation.VALIDATE, omitTermLoading);
//		appCtr.close();
//	}
	
	@Test
	public void testApplicationRemoteController(){
		Resource applicationContextResource = new ClassPathResource("/eu/etaxonomy/cdm/remotingApplicationContext.xml");
		CdmApplicationRemoteController appCtr = CdmApplicationRemoteController.NewInstance(applicationContextResource, null);
		appCtr.getClassificationService();
		
		appCtr.close();
	}

}
