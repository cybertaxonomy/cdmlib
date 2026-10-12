/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.oauth2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.etaxonomy.cdm.remote.server.ServerRunning;

/**
 * Helper for authorization-code grant smoke tests.
 * <p>
 * Legacy {@code AuthorizationCodeResourceDetails} (spring-security-oauth2) was removed
 * with the Spring Security 6 / Authorization Server migration.
 *
 * @author a.kohlbecker
 * @since Jan 17, 2017
 *
 */
public class AuthorizationCodeGrantTests {

    @Rule
    public ServerRunning serverRunning = ServerRunning.isRunning();

    private String authenticateAndApprove(String location) {

        ResponseEntity<String> page = serverRunning.getForString("/login.jsp");
        String cookie = page.getHeaders().getFirst("Set-Cookie");
        Matcher matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());

        MultiValueMap<String, String> form;
        form = new LinkedMultiValueMap<String, String>();
        form.add("username", "marissa");
        form.add("password", "koala");
        if (matcher.matches()) {
            form.add("_csrf", matcher.group(1));
        }

        HttpHeaders response = serverRunning.postForHeaders("/login", form);

        cookie = response.getFirst("Set-Cookie");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);

        serverRunning.getForString(location, headers);
        // Should be on user approval page now
        form = new LinkedMultiValueMap<String, String>();
        form.add("user_oauth_approval", "true");
        form.add("scope.read", "true");
        response = serverRunning.postForHeaders("/oauth/authorize", form, headers);

        return response.getLocation().toString();
    }

}
