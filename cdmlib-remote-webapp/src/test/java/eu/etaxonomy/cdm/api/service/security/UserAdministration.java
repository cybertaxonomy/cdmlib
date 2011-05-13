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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

import eu.etaxonomy.cdm.api.application.CdmApplicationRemoteController;
import eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class UserAdministration {

	private ICdmApplicationRemoteConfiguration appCtr;
	private static final Logger logger = Logger.getLogger(UserAdministration.class);

	@Before
	public void setUp() {
		Resource applicationContextResource = new ClassPathResource("/eu/etaxonomy/cdm/remotingApplicationContext.xml");
		appCtr = CdmApplicationRemoteController.NewInstance(applicationContextResource, null);
		
        String user = "admin"; // ROLE_ADMIN, ROLE_USER
//        String user = "developer"; // ROLE_USER1, ROLE_USER2
        String pwd = "0000";

        Authentication auth = new UsernamePasswordAuthenticationToken(user, pwd);
        SecurityContextImpl sc = new SecurityContextImpl();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);
        logger.info("setting up security context");
	}

	@Test 
	public void testSaveOrUpdateSample() {
		ITaxonService taxonService = appCtr.getTaxonService();		
		UUID uuid = UUID.fromString("7267e704-e0e8-41ee-8112-5aa22c61dea9");
		TaxonBase taxon = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		logger.info("taxonName=" + taxon.getName());
		
		TaxonNameBase name = taxon.getName();
		
		String nameString = "Malvaceae";
		name.setTitleCache(nameString);
		
		taxonService.saveOrUpdate(taxon);
		
		TaxonBase taxon2 = taxonService.load(uuid, TAXON_INIT_STRATEGY);
		logger.info("taxonName=" + taxon2.getName());

		Assert.assertEquals(nameString, taxon2.getName().getTitleCache());
	}

	@Test 
	public void testCreateNewGroupWithPresentGrantedAuthorities() {	
		IGroupService groupService = appCtr.getGroupService();
		String newGroupName = "GROUP_TEST";
		List<GrantedAuthority> authoritiesList = groupService.findGroupAuthorities("GROUP_ADMIN");
		groupService.createGroup(newGroupName, authoritiesList);
	}

//	@Test 
//	public void testCreateUser() {	
//		IUserService userService = appCtr.getUserService();
//		String username = "developer";
//		String pwd = "0000";
//		UserDetails user = User.NewInstance(username, pwd);
//		userService.createUser(user);
//	}

//	@Test 
//	public void testCreateGroupUserWithNewAuthorities() {	
//		IGroupService groupService = appCtr.getGroupService();
//		String groupName = "GROUP_USER";
//		GrantedAuthorityImpl grantedAuthorityAdmin = GrantedAuthorityImpl.NewInstance();
//		GrantedAuthorityImpl grantedAuthorityUser = (GrantedAuthorityImpl.NewInstance());
//		grantedAuthorityAdmin.setAuthority("ROLE_USER1");
//		grantedAuthorityUser.setAuthority("ROLE_USER2");
//		List<GrantedAuthority> grantedAuthoritiesList = new ArrayList<GrantedAuthority>();
//		grantedAuthoritiesList.add(grantedAuthorityAdmin);
//		grantedAuthoritiesList.add(grantedAuthorityUser);
//		groupService.createGroup(groupName, grantedAuthoritiesList);
//	}

//	@Test 
//	public void testCreateGroupAdminWithNewAuthorities() {	
//		IGroupService groupService = appCtr.getGroupService();
//		String groupName = "GROUP_ADMIN";
//		GrantedAuthorityImpl grantedAuthorityAdmin = GrantedAuthorityImpl.NewInstance();
//		GrantedAuthorityImpl grantedAuthorityUser = (GrantedAuthorityImpl.NewInstance());
//		grantedAuthorityAdmin.setAuthority("ROLE_ADMIN");
//		grantedAuthorityUser.setAuthority("ROLE_USER");
//		List<GrantedAuthority> grantedAuthoritiesList = new ArrayList<GrantedAuthority>();
//		grantedAuthoritiesList.add(grantedAuthorityAdmin);
//		grantedAuthoritiesList.add(grantedAuthorityUser);
//		groupService.createGroup(groupName, grantedAuthoritiesList);
//	}

	@Test 
	public void testAddUserToUserGroup() {	
		IGroupService groupService = appCtr.getGroupService();
		String groupName = "GROUP_TEST";
		String userName = "developer";
		groupService.addUserToGroup(userName, groupName);
	}

	@Test 
	public void testRemoveUserFromUserGroup() {	
		IGroupService groupService = appCtr.getGroupService();
		String groupName = "GROUP_TEST";
		String userName = "developer";
		groupService.removeUserFromGroup(userName, groupName);
	}

//	@Test 
//	public void testAddUserToAdminGroup() {	
//		IGroupService groupService = appCtr.getGroupService();
//		String groupName = "GROUP_ADMIN";
//		String userName = "admin";
//		groupService.addUserToGroup(userName, groupName);
//	}

	@Test 
	public void testUserService() {
		IUserService userService = appCtr.getUserService();
		UserDetails userDetails = userService.loadUserByUsername("developer");
		String username = userDetails.getUsername();
		String pwd = userDetails.getPassword();
		logger.info("userDetails for " + username + ": '" + pwd + "'");
	}

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

}
