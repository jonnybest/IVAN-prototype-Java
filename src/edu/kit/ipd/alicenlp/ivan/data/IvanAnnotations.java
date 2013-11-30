package edu.kit.ipd.alicenlp.ivan.data;

import java.util.List;

import edu.stanford.nlp.util.TypesafeMap;

/** Set of common annotations for Input & Verify Alice NLP.
 * The classes defined here are typesafe keys for getting and setting annotation values. These classes need not be instantiated outside of this class.
 * 
 * @author Jonny
 * @see edu.stanford.nlp.ling.CoreAnnotations
 *
 */
public class IvanAnnotations {


/**
 * @author Jonny
 *
 */
public class DocumentErrorAnnotation implements TypesafeMap.Key<List<IvanErrorMessage>>
{

}
/**
 * @author Jonny
 *
 */
public class IvanEntitiesAnnotation implements TypesafeMap.Key<InitialState>
{
	
}


/**
 * @author Jonny
 *
 */
public class ErrorMessageAnnotation implements TypesafeMap.Key<IvanErrorMessage>
{
	

}



}