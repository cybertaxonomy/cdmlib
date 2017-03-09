package eu.etaxonomy.cdm.model.common;

import java.util.Set;

public interface IIntextReferencable {


	//*************** INTEXT REFERENCE **********************************************

	public Set<IntextReference> getIntextReferences();

	public void addIntextReference(IntextReference intextReference);

	public void removeIntextReference(IntextReference intextReference);

	/**
	 * Returns the referenced text
     * @return the referenced text
     */
    public String getText();

    /**
     * Sets the referenced text.
     * @param text the new referenced text
     */
    public void setText(String text);
}
