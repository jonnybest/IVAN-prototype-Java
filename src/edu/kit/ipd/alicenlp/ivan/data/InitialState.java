/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.util.Pair;


/** This class manages a set of names and EntityInfos
 * @author Jonny
 *
 */
public class InitialState extends HashSet<EntityInfo>
{
	/** something to make the warning go away
	 * 
	 */
	private static final long serialVersionUID = 1058L;
	/** The set of all known names, linked to their respective entity
	 * 
	 */
	private HashMap<String,ArrayList<EntityInfo>> nameset = new HashMap<String, ArrayList<EntityInfo>>();

	/** Clears ALL the state!
	 * 
	 */
	@Override
	public void clear() {
		// don't forget to pop the nameset
		nameset.clear();
		super.clear();
	}

	/** Do not use this method.
	 */
	@Override
	public boolean remove(Object o) 
	{
		// nop
		System.err.println("The implementation of this set does not support removal by value.");
		return false;
	}
	
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
		ArrayList<EntityInfo> result = nameset.put(e.getEntity(), elist);
		return true;
	}
	
	/**
	 * This method attempts to add all the EntityInfos into this InitialState. It will not update any existing entries.
	 */
	@Override
	public boolean addAll(Collection<? extends EntityInfo> c) {
		boolean haschanged = false;
		for (EntityInfo entityInfo : c) {
			haschanged = haschanged | this.add(entityInfo);
		}
		return haschanged;
	}

	/**
	 * Updates the given {@code value} with  
	 * Existing data may be overwritten, but no new Entries may be created. Use merge() to create new entries.
	 * @param e
	 * @return
	 */
	private boolean update(EntityInfo e) {
		return update(null, e);		
	}

	/**
	 * Update the entity with the given name and overwrite data. No new entries may be created. Use merge() to create new entries.
	 * @param Name
	 * @param e
	 */
	private boolean update(String Name, EntityInfo e) {		
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

	public boolean containsName(String n) {
		return nameset.containsKey(n);
	}
	
	public boolean containsAllNames(Collection<String> names)
	{
		for (String name : names) {
			if (!containsName(name)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the singleton value to which the specified name is mapped, or null if this map contains no mapping for the name.
	 * It will break, if there is more than one match for the name, so use {@code get(String)} instead.
	 */
	public EntityInfo getSingle(String name)
	{
		List<EntityInfo> infos = nameset.get(name);
		assert infos.size() == 1;
		return infos.get(0);
	}
	
	/**
	 * Returns the values to which the specified name is mapped, or null if this map contains no mapping for the name. 
	 * Use {@code getSingle(String)} if you want to assert a singleton result. 
	 * 
	 * @param name The given name or the entity name to look for
	 * @returns all the entities found under this name 
	 */
	public ArrayList<EntityInfo> get(String name)
	{
		return nameset.get(name);
	}

	/** This method creates a simple view onto the entites with names: The left hand string is the entity, the right hand string its assigned name. 
	 */ 
	public List<Pair<String, String>> getEntityNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/** Returns whether there are any names assigned to the contained entities
	 * 
	 * @return TRUE if at least one entity has a proper name.
	 */
	public boolean hasNames() {
		return !nameset.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return super.contains(o);
	}
}
