package edu.kit.ipd.alicenlp.ivan.rules;

import java.util.ArrayList;

import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.DiscourseModel;
import edu.stanford.nlp.util.Pair;

/** This rule removes all entities which carry no useful information
 * 
 * @author Jonny
 *
 */
public class EntityPurgeRule {

	ArrayList<EntityInfo> purged = new ArrayList<EntityInfo>(10);
	
	/** Apply this rule and purge useless information
	 * 
	 * @param currentState The state to be cleaned
	 * @return TRUE, if anything has been removed
	 */
	public boolean apply(DiscourseModel currentState) {
		boolean worked = false;
		for (Pair<String, String> thing : currentState.getEntityNames()) {
			String entity = thing.first;
			String alias = thing.second;
			// entities with properly assigned names are useful and will be retained
			
			// get a handle
			String handle = entity != null ? entity : alias;
			ArrayList<EntityInfo> infos = currentState.get(handle);
			// candidates with 1 and 0 info are probably useless
			if(infos.size() < 2){
				// if there is info, see if it has its uses
				if(infos.size() == 1)
				{
					EntityInfo ei = infos.get(0);
					// information with either direction or location are useful and will be retained
					if(ei.hasDirection() || ei.hasLocation())
					{
						continue;
					}
				}
				// delete
				boolean okay = currentState.remove(entity, alias);
				if(okay)
				{
					worked |= okay;
					purged.addAll(infos);
				}
			}
		}
		return worked;
	}

	/**
	 * @return All singleton entities which have been removed from the discourse model.
	 */
	public EntityInfo[] getResults() {
		return purged.toArray(new EntityInfo[]{});
	}

}
