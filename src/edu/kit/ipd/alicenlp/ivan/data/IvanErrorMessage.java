package edu.kit.ipd.alicenlp.ivan.data;

import edu.stanford.nlp.ie.machinereading.structure.Span;

public class IvanErrorMessage 
{
	private final String Message;
	private final Span Range;
	
	private IvanErrorType type = IvanErrorType.UNKNOWN;

	public String getMessage() {
		return Message;
	}

	/** The text area which this error applies to. 
	 * The numbers are character positions. The begin is inclusive, the end is exclusive.
	 * @return
	 */
	public Span getSpan() {
		return Range;
	}
	
	public IvanErrorMessage(Integer start, Integer end, String message)
	{
		Range = Span.fromValues(start, end);
		Message = message;
	}

	public IvanErrorMessage(Span errorspan, String message) {
		Range = errorspan;
		Message = message;
	}
	
	public IvanErrorMessage(IvanErrorType errorId, Span errorspan, String message) {
		type = errorId;
		Range = errorspan;
		Message = message;
	}

	@Override
	public String toString() {
		return Range.toString() +" "+ getMessage();
	}

	public IvanErrorType getType() {
		return type;
	}
}
