/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;


/**
 * @author Jonny
 *
 */
@SuppressWarnings("serial")
public class InitialState extends HashSet<EntityInfo>
{
	private HashMap<String,ArrayList<EntityInfo>> nameset = new HashMap<String, ArrayList<EntityInfo>>();
	
	/**
	 * Writes the information in the given {@code value} into the {@link InitialState}. 
	 * Existing data is not overwritten, but new Entries may be created. Use update() to overwrite data.
	 * @param value
	 * @return A reference to the {@link EntityInfo} which is now present in the {@link InitialState}.
	 */
	public EntityInfo merge(EntityInfo value){
		return null;
	}
	
	/**
	 * This method attempts to add the EntityInfo into this InitialState. It will not update any existing entries.
	 */
	@Override
	public boolean add(EntityInfo e) {
		// lets see if this entity is already in our set
		boolean addednewly = super.add(e);
		if (!addednewly) {
			// this one is already present. there is nothing to do here
			if (!nameset.containsKey(e.getEntity())) {
				System.err.println("Warning: Unknown entity key. InitialState is inconsistent.");
			}
			return false;
		}
		ArrayList<EntityInfo> elist;
		
		if (nameset.containsKey(e.getEntity())) {
			elist = nameset.get(e.getEntity());
			elist.add(e);
			System.out.println("Yet another " + e.getEntity() + " added. This one now knows " + elist.size() + " " + e.getEntity() +"s.");
		}
		else {
			elist = new ArrayList<EntityInfo>();
			System.out.println("Oh, a " + e.getEntity() + ".");
		}
		
		elist.add(e);
		nameset.put(e.getEntity(), elist);
		return true;
	}

	/**
	 * Updates the given {@code value} with  
	 * Existing data may be overwritten, but no new Entries may be created. Use merge() to create new entries.
	 * @param e
	 * @return
	 */
	public boolean update(EntityInfo e) {
		return update(null, e);		
	}

	/**
	 * Update the entity with the given name and overwrite data. No new entries may be created. Use merge() to create new entries.
	 * @param Name
	 * @param e
	 */
	public boolean update(String Name, EntityInfo e) {		
		if (Name == null) {	// name is not given, so we will resolve by Entity description only
			ArrayList<EntityInfo> elist = nameset.get(e.getEntity());
			if (elist == null)
			{
				// entry does not exist
				return false;
			}
			if (elist.size() == 1) {
				// update entity
				EntityInfo oldentry = elist.get(0);
				oldentry.update(e);
				return true;
			}
			System.err.println("Entity is not unique. Call updateAll() or try accessing this one by name.");
			return false;
		}
		else {	// a name is given. Try to find the entity to update first by name, then by entity. Also, save the name if not known, yet.
			ArrayList<EntityInfo> elist;
			if (nameset.containsKey(Name)) {
				elist = nameset.get(Name);
			}
			else {
				// introduce a new name
				elist = nameset.get(e.getEntity());
				nameset.put(Name, elist);
			}
			// assert that the state is consistent
			assert elist != null; // "The information for this name or entity is null.");
			assert elist.size() == 1; // "The entry for this entity or name is not unique.");			
			
			// update entity
			EntityInfo oldentry = elist.get(0);
			oldentry.update(e);
			
			return true;
		}
	}
}
