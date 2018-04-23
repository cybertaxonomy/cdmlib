/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.oauth2;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.etaxonomy.cdm.remote.server.ServerRunning;

/**
 * @author a.kohlbecker
 * @since Jan 17, 2017
 *
 */
public class AuthorizationCodeGrantTests {

    @Rule
    public ServerRunning serverRunning = ServerRunning.isRunning();

    private AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();

    {
        resource.setAccessTokenUri(serverRunning.getUrl("/oauth/token"));
        resource.setClientId("my-client-with-registered-redirect");
        resource.setId("sparklr");
        resource.setScope(Arrays.asList("trust"));
        resource.setUserAuthorizationUri(serverRunning.getUrl("/oauth/authorize"));
    }


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
