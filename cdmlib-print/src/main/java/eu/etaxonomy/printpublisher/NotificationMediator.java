// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.printpublisher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A simple mediator that will mediate messages for {@linkplain IHarvestObserver observer}.
 * 
 * @author n.hoffmann
 * @created Aug 3, 2010
 * @version 1.0
 */
public class NotificationMediator {
	private static final Logger logger = Logger
			.getLogger(NotificationMediator.class);
	

	private List<IHarvestObserver> harvestOberver = new ArrayList<IHarvestObserver>();
	
	/**
	 * Adds an {@link IHarvestObserver} to the list of <code>IHarvestObservers</code>.
	 * 
	 * @see {@link IHarvestObserver}
	 * @param observer
	 */
	public void addObserver(IHarvestObserver observer) {
		harvestOberver.add(observer);
	}
	
	/**
	 * Removes an {@link IHarvestObserver} from the list of <code>IHarvestObservers</code>.
	 * 
	 * @see {@link IHarvestObserver}
	 * @param observer
	 */
	public void removeObserver(IHarvestObserver observer) {
		harvestOberver.remove(observer);
	}
	
	/**
	 * Calls the {@link IHarvestObserver#update(String)} method on all registered {@link IHarvestObserver}.
	 * 
	 * @param message
	 */
	public void notifyObserver(String message) {
		for(IHarvestObserver observer : harvestOberver){
			logger.trace("Notifying HarvestObserver");
			observer.update(message);
			logger.info(message);
		}
	}
}
