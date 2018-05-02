/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * The upmost (abstract) class for agents such as persons, teams or institutions.
 * An agent is a conscious entity which can make decisions, act and create
 * according to its own knowledge and goals and which may be approached.
 * Agents can be authors for nomenclatural or bibliographical references as well
 * as creators of pictures or field collectors or administrators of collections.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:57
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AgentBase", propOrder = {
		"contact"
})
@Entity
@Audited
@Table(appliesTo="AgentBase", indexes = { @Index(name = "agentTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class AgentBase<S extends IIdentifiableEntityCacheStrategy<? extends AgentBase<S>>>
        extends IdentifiableMediaEntity<S>
        implements IMergable, IMatchable, IIntextReferenceTarget, Cloneable{
	private static final long serialVersionUID = 7732768617469448829L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AgentBase.class);

	@XmlElement(name = "Contact")
    @Embedded
    @Merge(MergeMode.MERGE)
    @Match(MatchMode.IGNORE)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
	private Contact contact = new Contact();

	/**
	 * Returns the {@link Contact contact} of <i>this</i> person.
	 * The contact contains several ways to approach <i>this</i> person.
	 *
	 * @see 	Contact
	 */
	public Contact getContact(){
		return this.contact;
	}
	/**
	 * @see  #getContact()
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}


	/**
	 * Returns the existing contact, or if it does not exists a new contact.
	 * If <code>create</code> is true the new contact will be set as this agent's
	 * contact.
	 * @param create
	 * @return
	 */
	@Transient
	private Contact getNewOrExistingContact(boolean create){
		if (contact != null){
			return contact;
		}else{
			Contact newContact = Contact.NewInstance();
			if (create){
				contact = newContact;
				this.setContact(contact);
			}
			return contact;
		}
	}


	/**
	 * Adds a new address to this agent
	 * @param street
	 * @param postcode
	 * @param locality
	 * @param country
	 * @param pobox
	 * @param region
	 * @param location
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addAddress(java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.location.Country, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.location.Point)
	 */
	public Address addAddress(String street, String postcode, String locality,
			Country country, String pobox, String region,
			Point location) {
		return getNewOrExistingContact(true).addAddress(street, postcode, locality, country, pobox, region,
				location);
	}
	/**
	 * @param address
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addAddress(eu.etaxonomy.cdm.model.agent.Address)
	 */
	public void addAddress(Address address) {
		getNewOrExistingContact(true).addAddress(address);
	}
	/**
	 * @param emailAddress
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addEmailAddress(java.lang.String)
	 */
	public void addEmailAddress(String emailAddress) {
		getNewOrExistingContact(true).addEmailAddress(emailAddress);
	}
	/**
	 * @param faxNumber
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addFaxNumber(java.lang.String)
	 */
	public void addFaxNumber(String faxNumber) {
		getNewOrExistingContact(true).addFaxNumber(faxNumber);
	}
	/**
	 * @param phoneNumber
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addPhoneNumber(java.lang.String)
	 */
	public void addPhoneNumber(String phoneNumber) {
		getNewOrExistingContact(true).addPhoneNumber(phoneNumber);
	}
	/**
	 * @param url
	 * @see eu.etaxonomy.cdm.model.agent.Contact#addUrl(java.lang.String)
	 */
	public void addUrl(URI url) {
		getNewOrExistingContact(true).addUrl(url);
	}
	/**
	 * @return
	 * @see eu.etaxonomy.cdm.model.agent.Contact#getAddresses()
	 */
	@Transient
	public Set<Address> getAddresses() {
		return getNewOrExistingContact(false).getAddresses();
	}
	/**
	 * @return
	 * @see eu.etaxonomy.cdm.model.agent.Contact#getEmailAddresses()
	 */
	@Transient
	public List<String> getEmailAddresses() {
		return getNewOrExistingContact(false).getEmailAddresses();
	}
	/**
	 * @return
	 * @see eu.etaxonomy.cdm.model.agent.Contact#getFaxNumbers()
	 */
	@Transient
	public List<String> getFaxNumbers() {
		return getNewOrExistingContact(false).getFaxNumbers();
	}
	/**
	 * @return
	 * @see eu.etaxonomy.cdm.model.agent.Contact#getPhoneNumbers()
	 */
	@Transient
	public List<String> getPhoneNumbers() {
		return getNewOrExistingContact(false).getPhoneNumbers();
	}
	/**
	 * @return
	 * @see eu.etaxonomy.cdm.model.agent.Contact#getUrls()
	 */
	@Transient
	public List<String> getUrls() {
		return getNewOrExistingContact(false).getUrls();
	}
	/**
	 * @param address
	 * @see eu.etaxonomy.cdm.model.agent.Contact#removeAddress(eu.etaxonomy.cdm.model.agent.Address)
	 */
	public void removeAddress(Address address) {
		getNewOrExistingContact(false).removeAddress(address);
	}
	/**
	 * @param emailAddress
	 * @see eu.etaxonomy.cdm.model.agent.Contact#removeEmailAddress(java.lang.String)
	 */
	public void removeEmailAddress(String emailAddress) {
		getNewOrExistingContact(false).removeEmailAddress(emailAddress);
	}
	/**
	 * @param faxNumber
	 * @see eu.etaxonomy.cdm.model.agent.Contact#removeFaxNumber(java.lang.String)
	 */
	public void removeFaxNumber(String faxNumber) {
		getNewOrExistingContact(false).removeFaxNumber(faxNumber);
	}
	/**
	 * @param phoneNumber
	 * @see eu.etaxonomy.cdm.model.agent.Contact#removePhoneNumber(java.lang.String)
	 */
	public void removePhoneNumber(String phoneNumber) {
		getNewOrExistingContact(false).removePhoneNumber(phoneNumber);
	}
	/**
	 * @param url
	 * @see eu.etaxonomy.cdm.model.agent.Contact#removeUrl(java.lang.String)
	 */
	public void removeUrl(URI url) {
		getNewOrExistingContact(false).removeUrl(url);
	}

}
