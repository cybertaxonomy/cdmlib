/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.springframework.core.env.Environment;

/**
 * Main class providing property keys for the application configuration.
 * <p>
 * Other component specific keys are found in:
 * <ul>
 * <li>{@link SendEmailConfigurer}: Theses keys are replacing those which would come with
 * {@code org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration} which can not
 * yet be used in this project.</li>
 * </ul>
 *
 * @author a.kohlbecker
 * @since Feb 15, 2018
 */
public class CdmConfigurationKeys {

    /**
     * Key for the spring environment to the datasource bean id aka instance name.
     * <p>
     * By now this key can be used to retrieve the datasource bean id from the Spring {@link Environment} from
     * all applications that are making use of the {@code eu.etaxonomy.cdm.opt.config.DataSourceConfigurer}.
     * - as far as other classes that configure the data sources do not yet implement setting this property.
     */
    public static final String CDM_DATA_SOURCE_ID = "cdm.dataSource.id";

    /**
     * The email address of the help desk. This is usually the same for all instances running on the same server and
     * therefore should be configured as as
     */
    public static final String MAIL_ADDRESS_SUPPORT = "cdm.mailAddress.support";

}