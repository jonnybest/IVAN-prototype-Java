/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.util.TypesafeMap;

/**
 * @author Jonny
 *
 */
public class ErrorMessageAnnotation implements TypesafeMap.Key<ErrorMessageAnnotation>
{
	private final String docId;
	private final String Message;
	private final Span Range;

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

	public String getDocId() {
		return docId;
	}
	
	public ErrorMessageAnnotation(String document, Integer start, Integer end, String message)
	{
		docId = document;
		Range = Span.fromValues(start, end);
		Message = message;
	}

	public ErrorMessageAnnotation(String Id, Span errorspan, String message) {
		docId = Id;
		Range = errorspan;
		Message = message;
	}
}
