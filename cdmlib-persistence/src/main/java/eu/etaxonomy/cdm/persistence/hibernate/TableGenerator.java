/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.MappingException;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * Subclass of the {@link org.hibernate.id.enhanced.TableGenerator} for the sole purpose to
 * allow overriding the {@link org.hibernate.id.enhanced.TableGenerator#OPT_PARAM} and
 * {@link org.hibernate.id.enhanced.TableGenerator#INITIAL_PARAM} for the testing environment
 * (in principle you can override any of the TableGenerator parameters).
 * Test data may not always contain the hibernate sequences table which often leads to problems
 * with existing primary key values when inserting new entities. This especially occurs when
 * running test in a suite.
 * To circumvent these problems you can set a global override for the high initial parameter which
 * is far beyond any id ever used in test data sets.
 * You may want to set this in your testing application context, eg:
 *
 *<pre>
 * &lt;bean id=&quot;tableGeneratorGlobalOverride&quot; class=&quot;eu.etaxonomy.cdm.persistence.hibernate.TableGeneratorGlobalOverride&quot;&gt;
 *          &lt;property name=&quot;properties&quot;&gt;
	*            &lt;props&gt;
	*				&lt;!--
	*					globally overriding id generation settings
	*					see: eu.etaxonomy.cdm.persistence.hibernate.TableGenerator
	*				--&gt;
	*				&lt;prop key=&quot;optimizer&quot;&gt;none&lt;/prop&gt;
	*				&lt;prop key=&quot;initial_value&quot;&gt;1000&lt;/prop&gt;
 *          	&lt;/props&gt;
 *        &lt;/property&gt;
 *  &lt;/bean&gt;
 *</pre>
 *
 * If you set the optimizer to "none", hibernate will always query the database for each new id.
 * You must tell spring to instantiate the ... before the session factory:
 *
 * <pre>
 * &lt;bean id=&quot;sessionFactory&quot; class=&quot;org.springframework.orm.hibernate5.LocalSessionFactoryBean&quot; depends-on=&quot;tableGeneratorGlobalOverride&quot;&gt;
 * ...
 * </pre>
 *
 *
 *
 * @author Andreas Kohlbecker, 2012
 *
 */
//TODO this class has been moved to cdmlib-persistence preliminarily. It should be moved to
//cdmlib-test again as it should be used only in test. Currently this is not possible because
//sessionFactory bean has a dependsOn relationship to this class and this creates problems in remote-webapp
//as the been is not available.
//see also TableGeneratorGlobalOverride
public class TableGenerator extends org.hibernate.id.enhanced.TableGenerator {


	private static final Logger logger = Logger.getLogger(TableGenerator.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {

		Properties overrideProperies = TableGeneratorGlobalOverride.getProperties();
		if(overrideProperies != null) {
			params.putAll(overrideProperies);
		}
		logger.debug("overrideProperies:" + (overrideProperies != null ? overrideProperies :"NULL"));
		super.configure(type, params, serviceRegistry);
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public synchronized Serializable generate(final SessionImplementor session, Object obj) {
//
//		Serializable nextId =  super.generate(session, obj);
//		logger.debug("next id for " + obj.getClass().getSimpleName() + ":" + obj + " =" + nextId );
//		return nextId;
//
//		/*
//		if(nextId instanceof Number){
//			long nextIdL = ((Number)nextId).longValue();
//			int nextIdOffset = 1000;
//			logger.info("next id = " + (nextIdL + nextIdOffset));
//			return IdentifierGeneratorHelper.createNumber( nextIdL + nextIdOffset, nextId.getClass() );
//		} else {
//			logger.error("identifier expected to be a Number, cannot apply offset");
//			return nextId;
//		}
//		*/
//	}

}
