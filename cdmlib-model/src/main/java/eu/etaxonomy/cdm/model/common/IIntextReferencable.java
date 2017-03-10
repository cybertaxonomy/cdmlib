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

    /**
     * Adds an {@link IntextReference} and
     * sets the text of the referenced entity.<BR>
     *
     * NOTE: this will override any existing
     * text.
     *
     * @param target
     * @param start
     * @param inner
     * @param end
     * @return
     */
    public IntextReference addIntextReference(IIntextReferenceTarget target, String start, String inner, String end);

    /**
     * Adds an
     * @param target
     * @param start
     * @param end
     * @return
     */
    public IntextReference addIntextReference(IIntextReferenceTarget target, int start, int end);
}
