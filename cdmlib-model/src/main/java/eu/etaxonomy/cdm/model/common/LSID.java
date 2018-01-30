/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.ibm.lsid.MalformedLSIDException;

/**
 * This class is copied from com.ibm.lsid.LSID, I needed to re-implement this since
 * the domain objects are required to be Serializable
 *
 *
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @author ben.clark
 * @see com.ibm.lsid.client.LSID
 */
@Embeddable
public class LSID implements Serializable {
	private static final long serialVersionUID = -3568951541851092269L;

	private String lsid;

	private String authority;

	private String namespace;

	private String object;

	private String revision;

	private LSID() { }

	/**
	 * Construct a new LSID with the String representation.
	 * @param String The lsid String respresentation
	 */
	public LSID(String lsid) throws MalformedLSIDException {
		if (lsid.endsWith(":")) {
			lsid = lsid.substring(0, lsid.length() - 1);
		}
		StringTokenizer st = new StringTokenizer(lsid, ":");
		// check for urn and lsid
		try {
			String urn = st.nextToken().toLowerCase();
			String l = st.nextToken().toLowerCase();
			if (!urn.equals("urn") || !l.equals("lsid")) {
				throw new MalformedLSIDException("urn:lsid: not found: [" + lsid + "]");
			}
		}
		catch (NoSuchElementException e) {
			throw new MalformedLSIDException(e, "urn:lsid: not found: [" + lsid + "]");
		}

		try {
			authority = st.nextToken().toLowerCase();
		}
		catch (NoSuchElementException e) {
			throw new MalformedLSIDException(e, "authority not found: [" + lsid + "]");
		}

		try {
			namespace = st.nextToken();
		}
		catch (NoSuchElementException e) {
			throw new MalformedLSIDException(e, "namespace not found: [" + lsid + "]");
		}

		try {
			object = st.nextToken();
		}
		catch (NoSuchElementException e) {
			throw new MalformedLSIDException(e, "object not found: [" + lsid + "]");
		}
		if (st.hasMoreTokens()) {
			revision = st.nextToken();
		}

		this.lsid = "urn:lsid:" + this.authority + ":" + this.namespace + ":" + this.object + (this.revision != null ? ":" + this.revision : "");
	}

	/**
	 * Construct a new LSID with the given components
	 * @param String the authority
	 * @param String the namespace
	 * @param String the object
	 * @param String the revision, can be null
	 */
	public LSID(String authority, String namespace, String object, String revision) throws MalformedLSIDException {
		this.authority = authority.toLowerCase();
		this.namespace = namespace;//.toLowerCase();
		this.object = object;//.toLowerCase();
		if (revision != null)
         {
            this.revision = revision;//.toLowerCase();
        }
		lsid = "urn:lsid:" + this.authority + ":" + this.namespace + ":" + this.object + (this.revision != null ? ":" + this.revision : "");
	}

	/**
	 * Returns the lsid
	 * @return String The lsid String representation
	 */
	public String getLsid() {
		return lsid;
	}



	/**
	 * Returns the authority component of the LSID
	 * @return LSIDAuthority the authority
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Returns the namespace component of the LSID
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the object component of the LSID
	 * @return String
	 */
	public String getObject() {
		return object;
	}

	/**
	 * Returns the revision component of the LSID if it exists
	 * @return String
	 */
	public String getRevision() {
		return revision;
	}

	public static boolean isLsid(String strLsid){
		try {
			//TODO use algorithm rather than exceptions
			new LSID(strLsid);
			return true;
		} catch (MalformedLSIDException e) {
			return false;
		}
	}

    /**
     * <code>true</code>, if all of the LSID parts are
     * empty or <code>null</code>.
     */
    @Transient
    public boolean isEmpty(){
        if (isEmpty(authority) && isEmpty(lsid) &&
                isEmpty(namespace) && isEmpty(object) &&
                isEmpty(revision)){
            return true;
        }else{
            return false;
        }
    }
    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

	/**
	 * return the string representation
	 * @return String
	 */
	@Override
    public String toString() {
		return lsid;
	}

	/**
	 * Two LSIDs are equal their string representations are equal disregarding case.
	 */
	@Override
    public boolean equals(Object lsid) {
		if(this == lsid) {
			return true;
		} else if (lsid != null && lsid instanceof LSID) {
			LSID theLSID = (LSID)lsid;
			return theLSID.toString().equals(toString());
		} else {
			return false;
		}
	}
}
