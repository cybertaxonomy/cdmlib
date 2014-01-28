package ru.xpoft.vaadin;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

import eu.etaxonomy.cdm.remote.vaadin.VaadinConfigurer;
import ru.xpoft.vaadin.DiscoveryNavigator;
import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.VaadinView;
import ru.xpoft.vaadin.DiscoveryNavigator.ViewCache;

public class CdmDiscoveryNavigator extends DiscoveryNavigator {
	
	
	String packageNameScope = null;
	
	@Autowired	
	private VaadinConfigurer vaadinConfigurer;

	public CdmDiscoveryNavigator(UI ui, ComponentContainer container, String packageNameScope) {
		super(ui, container);
		this.packageNameScope = packageNameScope;
		initViews();
	}

	
	public CdmDiscoveryNavigator(UI ui, SingleComponentContainer container, String packageNameScope) {
		super(ui, container);
		this.packageNameScope = packageNameScope;	
		initViews();
	}
	
	public CdmDiscoveryNavigator(UI ui, ViewDisplay display, String packageNameScope) {
		super(ui, display);
		this.packageNameScope = packageNameScope;
	}

	public CdmDiscoveryNavigator(UI ui, NavigationStateManager stateManager,
			ViewDisplay display, String packageNameScope) {
		super(ui, stateManager, display);
		this.packageNameScope = packageNameScope;
	}

	@Override
	protected void initViews()
	{

		if (packageNameScope == null){
			return;
		}
		if (views.isEmpty())
		{


			String[] beansName = SpringApplicationContext.getApplicationContext().getBeanDefinitionNames();

			// Also looking for parent's beans definition
			if (SpringApplicationContext.getApplicationContext().getParent() != null)
			{
				String[] parentBeansName = SpringApplicationContext.getApplicationContext().getParent().getBeanDefinitionNames();
				String[] newBeansName = new String[beansName.length + parentBeansName.length];

				System.arraycopy(beansName, 0, newBeansName, 0, beansName.length);
				System.arraycopy(parentBeansName, 0, newBeansName, beansName.length, parentBeansName.length);

				beansName = newBeansName;
			}
			for (String beanName : beansName)
			{
				Class beanClass = SpringApplicationContext.getApplicationContext().getType(beanName);

				if(beanClass != null)
				{
					if(beanClass.getPackage() != null && beanClass.getPackage().toString().contains(packageNameScope))
					{
						// Check for a valid bean class because "abstract" beans may not have a bean class defined.
						if(beanClass.isAnnotationPresent(VaadinView.class) && View.class.isAssignableFrom(beanClass))
						{
							VaadinView vaadinView = (VaadinView) beanClass.getAnnotation(VaadinView.class);
							String viewName = vaadinView.value();
							boolean viewCached = vaadinView.cached();

							ViewCache viewCache = new ViewCache(viewName, beanName, beanClass, viewCached);
							views.add(viewCache);
						}
					}
				}
			}
		}

		addCachedBeans();
	}


}
