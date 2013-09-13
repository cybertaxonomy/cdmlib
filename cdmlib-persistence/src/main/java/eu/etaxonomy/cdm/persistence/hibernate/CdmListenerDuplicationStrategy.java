/**
 * 
 */
package eu.etaxonomy.cdm.persistence.hibernate;

import org.hibernate.event.service.spi.DuplicationStrategy;

/**
 * @author a.mueller
 *
 */

public class CdmListenerDuplicationStrategy implements DuplicationStrategy {
	public static final CdmListenerDuplicationStrategy NewInstance = new CdmListenerDuplicationStrategy();

	@Override
	public boolean areMatch(Object listener, Object original) {
		return listener.getClass().equals( original ) && CdmListenerDuplicationStrategy.class.isInstance( listener );
	}

	@Override
	public Action getAction() {
		return Action.KEEP_ORIGINAL;
	}
}
