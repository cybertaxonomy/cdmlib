/* Package Annotations*/

@jakarta.xml.bind.annotation.XmlSchema(namespace = "http://etaxonomy.eu/cdm/model/common/1.0", elementFormDefault = jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED)
@org.glassfish.jaxb.runtime.XmlAccessorFactory(eu.etaxonomy.cdm.jaxb.CdmAccessorFactoryImpl.class)
@org.hibernate.annotations.GenericGenerators(
	{
		/* @see {@link eu.etaxonomy.cdm.persistence.hibernate.TableGenerator} */
		@GenericGenerator(
				name="custom-enhanced-table",
				strategy = "eu.etaxonomy.cdm.persistence.hibernate.TableGenerator",
				parameters = {
				    @Parameter(name="table_name", value = "hibernate_sequences"),
				    @Parameter(name="optimizer", value = "pooled"),
				    /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10"),
				    /* we want to have a sequence per table */
				    @Parameter(name="prefer_entity_table_as_segment_value", value="true")
				}
		),
		/* Steve Ebersole of Hibernate highly recommends the use of the two new generators
		 * http://relation.to/2082.lace
		 * Also see: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/mapping.html#mapping-declaration-id-enhanced
		 */
		/* new table generator
		 * always stores sequences in a table. May be configured to return a sequence on a per table basis
		 * RECOMMENDED WHEN RUNNING A CDM DATASOURCE IN A MULTI CLIENT ENVIRONMENT
		 */
		@GenericGenerator(
				name="enhanced-table",
				strategy = "org.hibernate.id.enhanced.TableGenerator",
				parameters = {
				    @Parameter(name="table_name", value = "hibernate_sequences"),
	                @Parameter(name="optimizer", value = "pooled"),
				    /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10"),
				    /* we want to have a sequence per table */
				    @Parameter(name="prefer_entity_table_as_segment_value", value="true")
				}
		),
		/* new sequence generator
		 * Using sequence when the dialect supports it, otherwise it will emulate a sequence using a table in the db
		 * This method will result in database wide unique id's */
		@GenericGenerator(
				name="enhanced-sequence",
				strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
				parameters = {
			        @Parameter(name="optimizer", value = "pooled"),
			        /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10")
			    }
		),
		/* A couple of old style generators */
		/* generates identifiers of type long, short or int that are unique only when no other process
		 * is inserting data into the same table.
		 * DO NOT USE IN A CLUSTER OR MULTIPLE CLIENT ENVIRONMENT */
		@GenericGenerator(
				name="system-increment",
				strategy = "increment"
		),
		/* supports identity columns in DB2, MySQL, MS SQL Server, Sybase and HypersonicSQL.
		 * The returned identifier is of type long, short or int. */
		@GenericGenerator(
				name="system-identity",
				strategy = "identity"

		),
		/* uses a hi/lo algorithm to efficiently generate identifiers of type long, short or int,
		 * given a table and column (by default hibernate_unique_key and next_hi respectively) as
		 * a source of hi values. The hi/lo algorithm generates identifiers that are unique only
		 * for a particular database. */
		@GenericGenerator(
				name="system-hilo",
				strategy = "hilo"
		),
		/* uses a sequence in DB2, PostgreSQL, Oracle, SAP DB, McKoi or a generator in Interbase.
		 * The returned identifier is of type long, short or int */
		@GenericGenerator(
				name="system-sequence",
				strategy = "sequence"
		),
		/* selects identity, sequence or hilo depending upon the capabilities of the underlying database. */
		@GenericGenerator(
				name="system-native",
				strategy = "native"
		)
	}
)
package eu.etaxonomy.cdm.model.common;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
