package de.uka.ipd.resi;

/**
 * TODO comments
 * 
 * @author Torben Brumm
 */
public class DeterminerInfo {
	
	private static final int DEFINITE_ARTICLE = -1;
	
	private static final int INDEFINITE_ARTICLE = -2;
	
	public static final int INFINITY = Integer.MAX_VALUE - 10;
	
	private Mark mark;
	
	private int maxValue;
	
	private int minValue;
	
	/**
	 * @return the mark
	 */
	public Mark getMark() {
		return this.mark;
	}
	
	/**
	 * @return the maxValue
	 */
	public int getMaxValue() throws IllegalStateException {
		if (this.maxValue < 0) {
			throw new IllegalStateException("No max value set");
		}
		return this.maxValue;
	}
	
	public int getMinValue() {
		if (this.minValue < 0) {
			throw new IllegalStateException("No min value set");
		}
		return this.minValue;
	}
	
	public boolean isDefiniteArticle() {
		return (this.minValue == DeterminerInfo.DEFINITE_ARTICLE);
	}
	
	public boolean isIndefiniteArticle() {
		return (this.minValue == DeterminerInfo.INDEFINITE_ARTICLE);
	}
	
	public void setIsDefiniteArticle() {
		this.minValue = DeterminerInfo.DEFINITE_ARTICLE;
		this.maxValue = DeterminerInfo.DEFINITE_ARTICLE;
	}
	
	public void setIsIndefiniteArticle() {
		this.minValue = DeterminerInfo.INDEFINITE_ARTICLE;
		this.maxValue = DeterminerInfo.INDEFINITE_ARTICLE;
	}
	
	/**
	 * @param mark the mark to set
	 */
	public void setMark(final Mark mark) {
		this.mark = mark;
	}
	
	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(final int maxValue) throws IllegalArgumentException {
		if (maxValue < 0) {
			throw new IllegalArgumentException("Only positive values for max value allowed");
		}
		this.maxValue = maxValue;
	}
	
	public void setMinValue(final int minValue) {
		if (minValue < 0) {
			throw new IllegalArgumentException("Only positive values for min value allowed");
		}
		this.minValue = minValue;
	}
	
	public void setRange(final int minValue, final int maxValue) {
		this.setMinValue(minValue);
		this.setMaxValue(maxValue);
	}
	
}
