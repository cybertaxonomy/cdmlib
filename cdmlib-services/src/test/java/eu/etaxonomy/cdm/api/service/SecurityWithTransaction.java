
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;


@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({"/eu/etaxonomy/cdm/applicationContextSecurity.xml"})
@DataSet("SecurityTest.xml")
@Transactional
public class SecurityWithTransaction {
	private static final Logger logger = Logger
			.getLogger(SecurityWithTransaction.class);
	
	@SpringBeanByName
	private ITaxonService taxonService;
	
	@SpringBeanByName
	private IDescriptionService descriptionService;
	
	@SpringBeanByName
	private ITaxonNodeService taxonNodeService;
	
	@SpringBeanByName
	private IUserService userService;
	
	
	@TestDataSource
	protected DataSource dataSource;
	
	private Authentication authentication;
	
	@SpringBeanByName
	private AuthenticationManager authenticationManager;
	
	private UsernamePasswordAuthenticationToken token;
	
	
	@Before
	public void setUp(){
		token = new UsernamePasswordAuthenticationToken("ben", "sPePhAz6");
	}
	
	@Test
	public void testDeleteTaxon(){
		token = new UsernamePasswordAuthenticationToken("taxonomist", "test3");
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Taxon actualTaxon = (Taxon)taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		
		taxonService.delete(actualTaxon);
	}
	
	
	@Test
	public void testSaveOrUpdateDescription(){
		
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		/*Taxon taxon = (Taxon) taxonService.load(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		
		Iterator<TaxonDescription> iterator = descriptions.iterator();
		
		TaxonDescription description = iterator.next();*/
		TaxonDescription description = (TaxonDescription) descriptionService.find(UUID.fromString("eb17b80a-9be6-4642-a6a8-b19a318925e6"));
		
		TextData textData = new TextData();
		textData.setFeature(Feature.ECOLOGY());
		Media media = Media.NewInstance();
		textData.addMedia(media);
		
		
		
		//descriptionService.saveDescriptionElement(textData);
		description.addElement(textData);
		
		descriptionService.saveOrUpdate(description);
		
		Taxon taxon = (Taxon) taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		
		Iterator<TaxonDescription> iterator = descriptions.iterator();
		
		description = iterator.next();
		assertEquals(1, descriptions.size());
		assertEquals(2,description.getElements().size());
		
		
		
	}
	
	@Test
	public void testAllowOnlyAccessToPartOfTree(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		Taxon tribe = (Taxon)taxonService.find(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
		Taxon taxon = (Taxon)taxonService.find(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
		Iterator<TaxonNode> it = tribe.getTaxonNodes().iterator();
		TaxonNode node = it.next();
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		assertFalse(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		node = node.getChildNodes().iterator().next();
		System.err.println(node.getUuid()); 
		assertTrue(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		node = node.getChildNodes().iterator().next();
		assertTrue(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		TaxonDescription description = TaxonDescription.NewInstance(taxon);
		
		taxonNodeService.saveOrUpdate(node);
		assertFalse(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		
		
	}
	
	
}
