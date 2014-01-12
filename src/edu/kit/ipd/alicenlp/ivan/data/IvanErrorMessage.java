package edu.kit.ipd.alicenlp.ivan.data;

import edu.stanford.nlp.ie.machinereading.structure.Span;

/** This class is about telling the user where things went wrong.
 * @author Jonny
 *
 */
public class IvanErrorMessage 
{
	private final String Message;
	private final Span Range;
	
	private IvanErrorType type = IvanErrorType.UNKNOWN;

	/** A human-readable error message for this specific issue.
	 * 
	 * @return Help (hopefully)
	 */
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
	
	/** Create a new error message.
	 * 
	 * @param ErrorType A category which describes the kind of error.
	 * @param start Where the offending words begin
	 * @param end Where the offending words end
	 * @param ErrorMessage What this error is all about. Human-readable, helpful text, please.
	 */
	public IvanErrorMessage(Integer start, Integer end, String ErrorMessage)
	{
		this(IvanErrorType.UNKNOWN, start, end, ErrorMessage);
	}

	/** Create a new error message. The type will be "IvanErrorType.UNKNOWN"
	 * 
	 * @param errorspan Where the offending words begin and end
	 * @param ErrorMessage What this error is all about. Human-readable, helpful text, please.
	 */
	public IvanErrorMessage(Span errorspan, String ErrorMessage) {
		this(IvanErrorType.UNKNOWN, errorspan, ErrorMessage);
	}

	/** Create a new error message.
	 * 
	 * @param ErrorType A category which describes the kind of error.
	 * @param errorspan Where the offending words begin and end
	 * @param ErrorMessage What this error is all about. Human-readable, helpful text, please.
	 */
	public IvanErrorMessage(IvanErrorType ErrorType, Span errorspan, String ErrorMessage) {
		type = ErrorType;
		Range = errorspan;
		Message = ErrorMessage;
	}

	/** Create a new error message.
	 * 
	 * @param ErrorType A category which describes the kind of error.
	 * @param Begin Where the offending words begin
	 * @param End Where the offending words end
	 * @param ErrorMessage What this error is all about. Human-readable, helpful text, please.
	 */
	public IvanErrorMessage(IvanErrorType ErrorType, Integer Begin,
			Integer End, String ErrorMessage) {
		this(ErrorType, Span.fromValues(Begin, End), ErrorMessage);
	}

	@Override
	public String toString() {
		return Range.toString() +" "+ getMessage();
	}

	/** A category which describes the kind of error.
	 * @return An instance of the Enum IvanErrorType
	 */
	public IvanErrorType getType() {
		return type;
	}
}
