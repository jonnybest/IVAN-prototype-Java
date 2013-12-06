package de.uka.ipd.resi;

/**
 * A mark in the specification for later inpection etc.
 * 
 * @author Torben Brumm
 */
public class Mark {
	
	/**
	 * Comment to this mark.
	 */
	private String comment;
	
	/**
	 * Returns the comment to this mark.
	 * 
	 * @return comment
	 */
	public String getComment() {
		return this.comment;
	}
	
	/**
	 * Sets the comment to this mark.
	 * 
	 * @param comment New comment.
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.comment;
	}
}
