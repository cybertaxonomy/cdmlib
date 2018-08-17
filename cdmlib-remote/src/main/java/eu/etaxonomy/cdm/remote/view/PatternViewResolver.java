/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.XmlViewResolver;

/**
 * Resolves views by using a {@link PathMatcher} to match the view name to the bean names.
 *
 * By default the {@link AntPathMatcher} is used which supports Ant-style path patterns.
 * The mapping matches views using the following rules:
 * <ul>
 *   <li>? matches one character
 *   <li>* matches zero or more characters
 *   <li>** matches zero or more 'directories' in a path
 *  </ul>
 * @author ben.clark
 */
public class PatternViewResolver extends XmlViewResolver {

	private final Set<String> viewSet = new HashSet<String>();

	private PathMatcher pathMatcher = new AntPathMatcher();



	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

	@Override
	public View resolveViewName(String viewName, Locale locale)  throws Exception {
		// Direct match?
		if (this.viewSet.contains(viewName)) {
			return super.resolveViewName(viewName, locale);
		}
		// Pattern match?
		String bestPathMatch = null;
		for (String registeredPath : this.viewSet) {
			if (getPathMatcher().match(registeredPath, viewName) &&
					(bestPathMatch == null || bestPathMatch.length() < registeredPath.length())) {
				bestPathMatch = registeredPath;
			}
		}
		if (bestPathMatch != null) {
			return super.resolveViewName(bestPathMatch, locale);
		}
		// No view found...
		return null;
	}

	@Override
	 protected synchronized BeanFactory initFactory() throws BeansException {
		AbstractApplicationContext beanFactory = (AbstractApplicationContext)super.initFactory();
		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		for(String beanDefinitionName : beanDefinitionNames) {
			viewSet.add(beanDefinitionName);
			viewSet.addAll(Arrays.asList(beanFactory.getAliases(beanDefinitionName)));
		}
		return beanFactory;
	}

}
