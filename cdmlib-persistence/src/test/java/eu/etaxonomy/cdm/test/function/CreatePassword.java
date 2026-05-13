/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.function;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author muellera
 * @since 13.05.2026
 */
public class CreatePassword {

    private void createPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("00000");
        System.out.println(encoded);
    }

    public static void main(String[] args) {
        System.out.println("CreatePassword gestartet.");
        CreatePassword cp = new CreatePassword();
        cp.createPassword();
    }

}
