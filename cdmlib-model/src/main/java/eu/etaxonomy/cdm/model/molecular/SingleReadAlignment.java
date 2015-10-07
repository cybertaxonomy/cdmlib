/**
 *
 */
package eu.etaxonomy.cdm.model.molecular;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleReadAlignment", propOrder = {
	"consensusAlignment",
	"singleRead",
	"shifts",
	"editedSequence",
	"reverseComplement",
	"firstSeqPosition",
	"leftCutPosition",
	"rightCutPosition"
})
@XmlRootElement(name = "SingleReadAlignment")
@Entity
@Audited
public class SingleReadAlignment extends VersionableEntity implements Serializable {
	private static final long serialVersionUID = 6141518347067279304L;

	/** @see #getDnaMarker() */
	@XmlElement(name = "ConsensusAlignment")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	//for now we do not cascade but expect the user to save the sequence manually
	private Sequence consensusAlignment;

	/** @see #getDnaMarker() */
	@XmlElement(name = "SingleRead")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private SingleRead singleRead;

	//TODO XML mapping / user type
	@Type(type="shiftUserType")
    private Shift[] shifts = new Shift[0];

	private Integer firstSeqPosition;

	private Integer leftCutPosition;

	private Integer rightCutPosition;

	@XmlElement(name = "EditedSequence")
    @Lob
    private String editedSequence;

	@XmlElement(name = "ReverseComplement")
    private boolean reverseComplement;


	public static class Shift implements Cloneable, Serializable {
		public int position;
		public int shift;

		public Shift(){};
		public Shift(int position, int steps) {
			this.position = position;
			this.shift = steps;
		}

		@Override
		public String toString(){
			return String.valueOf(position) + "," + String.valueOf(shift);
		}
		@Override
		protected Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}

//****************** FACTORY *******************/

	public static SingleReadAlignment NewInstance(Sequence consensusSequence, SingleRead singleRead){
		return new SingleReadAlignment(consensusSequence, singleRead, null, null);
	}

	public static SingleReadAlignment NewInstance(Sequence consensusSequence, SingleRead singleRead,
			Shift[] shifts, String editedSequence){
		return new SingleReadAlignment(consensusSequence, singleRead, shifts, editedSequence);
	}

// ***************** CONSTRUCTOR *************************/

	protected SingleReadAlignment(){};

	private SingleReadAlignment(Sequence consensusAlignment, SingleRead singleRead,
			Shift[] shifts, String editedSequence){
		setConsensusAlignment(consensusAlignment);
		setSingleRead(singleRead);
		this.shifts = shifts;
		this.editedSequence = editedSequence;
	}


// ****************** GETTER / SETTER ***********************/

	//consensus sequence
	public Sequence getConsensusSequence() {
		return consensusAlignment;
	}
	public void setConsensusAlignment(Sequence consensusAlignment) {
		if (this.consensusAlignment != null && this.consensusAlignment.getSingleReadAlignments().contains(this)){
			this.consensusAlignment.removeSingleReadAlignment(this);
		}
		this.consensusAlignment = consensusAlignment;
		if (consensusAlignment != null && ! consensusAlignment.getSingleReadAlignments().contains(this)){
			consensusAlignment.addSingleReadAlignment(this);
		}
	}

	public SingleRead getSingleRead() {
		return singleRead;
	}
	public void setSingleRead(SingleRead singleRead) {
//		if (this.singleRead != null xxx){
//			this.singleRead.removeSingleReadAlignment(this);
//		}
		this.singleRead = singleRead;
//		if (singleRead != null && singleRead.getSingleReadAlignments().contains(this)){
//			singleRead.addSingleReadAlignment(this);
//		}
	}

	//shifts
	public Shift[] getShifts() {
		return shifts == null ? new Shift[0] : shifts;
	}
	public void setShifts(Shift[] shifts) {
		this.shifts = shifts;
		if (shifts == null){
			shifts = new Shift[0];
		}
	}

	//edited sequence
	public String getEditedSequence() {
		return editedSequence;
	}

	public void setEditedSequence(String editedSequence) {
		this.editedSequence = editedSequence;
	}


	public boolean isReverseComplement() {
		return reverseComplement;
	}

	public void setReverseComplement(boolean reverseComplement) {
		this.reverseComplement = reverseComplement;
	}

// ******************* CLONE *********************/



	@Override
	public Object clone() throws CloneNotSupportedException {
		SingleReadAlignment result = (SingleReadAlignment)super.clone();

		//deep copy shifts
		Shift[] oldShifts = this.getShifts();
		int shiftLength = oldShifts.length;
		Shift[] newShift = new Shift[shiftLength];
		for (int i = 0; i< shiftLength; i++){
			Shift oldShift = oldShifts[i];
			newShift[0] = (Shift)oldShift.clone();
		}

		//all other objects can be reused
		return result;
	}

    /**
     * Returns the position in the {@link Sequence sequence}
     * this {@link SingleReadAlignment single read align} is attached to
     * where the output of the visible part of the pherogram starts.
     * @return a valid index in the sequence carrying this data area
     * @see http://bioinfweb.info/LibrAlign/Documentation/api/latest/info/bioinfweb/libralign/dataarea/implementations/pherogram/PherogramArea.html#getFirstSeqPos
     *
     */
    public Integer getFirstSeqPosition() {
        return firstSeqPosition;
    }

    /**
     * @see #getFirstSeqPosition()
     */
    public void setFirstSeqPosition(Integer firstSeqPosition) {
        this.firstSeqPosition = firstSeqPosition;
    }

    /**
     * Returns the first base call index of the pherogram which has not been cut off.
     * @return a base call index > 0
     * @see http://bioinfweb.info/LibrAlign/Documentation/api/latest/info/bioinfweb/libralign/pherogram/PherogramComponent.html#getLeftCutPosition
     */
    public Integer getLeftCutPosition() {
        return leftCutPosition;
    }

    /**
     * @param see {@link #getLeftCutPosition()}
     */
    public void setLeftCutPosition(Integer leftCutPosition) {
        this.leftCutPosition = leftCutPosition;
    }

    /**
     * Returns the first base call index of the pherogram that has been cut off (so that the length of the visible
     * area of the pherogram can be calculated as getRightCutPosition()
     * @return a base call inde
     * @see http://bioinfweb.info/LibrAlign/Documentation/api/latest/info/bioinfweb/libralign/pherogram/PherogramComponent.html#getRightCutPosition
     */
    public Integer getRightCutPosition() {
        return rightCutPosition;
    }

    /**
     * @param see {@link #getRightCutPosition()}
     */
    public void setRightCutPosition(Integer rightCutPosition) {
        this.rightCutPosition = rightCutPosition;
    }

}
