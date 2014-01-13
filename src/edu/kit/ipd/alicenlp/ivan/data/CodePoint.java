package edu.kit.ipd.alicenlp.ivan.data;

import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.util.IntPair;

/** Code points are the coordinates that represent spans in the document.
 * The coordinates in this structure satisfy x <= y.
 * 
 * @author Jonny
 *
 */
public class CodePoint extends IntPair
{

	final public Integer x;
	final public Integer y;

	/** Create a new Code point
	 * 
	 * @param i
	 * @param j
	 */
	public CodePoint(Integer i, Integer j) {		
		if(i < j)
		{
			x = i;
			y = j;
			this.elems()[0] = i;
			this.elems()[1] = j;
		}
		else {
			x = j;
			y = i;
			this.elems()[0] = j;
			this.elems()[1] = i;
		}
	}

	/** Create a code point from a tuple
	 * 
	 * @param tuple
	 */
	public CodePoint(IntPair tuple)
	{
		int i = tuple.getSource();
		int j = tuple.getTarget();
		if(i < j)
		{
			x = i;
			y = j;
			this.elems()[0] = i;
			this.elems()[1] = j;
		}
		else {
			x = j;
			y = i;
			this.elems()[0] = j;
			this.elems()[1] = i;
		}
	}
	
	@Override
	public int hashCode() {
		return this.elems()[0] * 67 + this.elems()[1];
	}

	/** Create a code point from a span
	 * @param span
	 */
	public CodePoint (Span span) {
		this(span.start(), span.end());
	}
}