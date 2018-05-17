/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import java.util.List;

import javax.validation.Valid;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * A Mock class for testing entity validation tasks. DO NOT MODIFY UNLESS YOU
 * ALSO MODIFY THE UNIT TESTS MAKING USE OF THIS CLASS!
 *
 * @author ayco_holleman
 *
 */
@SuppressWarnings("serial")
public class Employee extends CdmBase {

    @CheckCase(value = CaseMode.UPPER, groups = { Level2.class })
    private String givenName;
    @CheckCase(value = CaseMode.UPPER, groups = { Level3.class })
    private String familyName;
    @Valid
    private Company company;
    @Valid
    private List<Address> addresses;

    public Employee() {
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        Employee emp = (Employee) obj;
        return givenName.equals(emp.givenName) && familyName.equals(emp.familyName);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = (hash * 31) + (givenName == null ? 0 : givenName.hashCode());
        hash = (hash * 31) + (familyName == null ? 0 : familyName.hashCode());
        return hash;
    }

}
