/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * PhysicalNamingStrategy for use in cdmlib-test to create test database schema.
 * For some reason this was created at the beginning of the EDIT test framework
 * with upper case table names. Generally this is not a problem as tests are
 * case insensitive. However, to have a result as close to the prior version as possible
 * we try to have at least table names and column names with upper case.
 * Not handled correctly this way are constraint names which are lower case now.
 *
 * @see https://dev.e-taxonomy.eu/redmine/issues/6714
 *
 * @author a.mueller
 * @since 24.08.2020
 */
public class UpperCasePhysicalNamingStrategyStandardImpl extends PhysicalNamingStrategyStandardImpl {

    private static final long serialVersionUID = -3630661711065522790L;

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return toUpperCase(super.toPhysicalCatalogName(name, context));
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return toUpperCase(super.toPhysicalSchemaName(name, context));
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return toUpperCase(super.toPhysicalTableName(name, context));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return toUpperCase(super.toPhysicalSequenceName(name, context));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return toUpperCase(super.toPhysicalColumnName(name, context));
    }


    private Identifier toUpperCase(Identifier identifier) {
        if (identifier == null){
            return null;
        }else{
            return new Identifier(identifier.getText() == null? null:
                identifier.getText().toUpperCase(), identifier.isQuoted());
        }
    }

}
