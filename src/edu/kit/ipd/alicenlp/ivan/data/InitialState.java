/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.kit.ipd.alicenlp.ivan.rules.EntitiesSynonymsErrorRule;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.TypesafeMap;


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
	private HashMap<String,ArrayList<EntityInfo>> namesset = new HashMap<String, ArrayList<EntityInfo>>();

	/** Clears ALL the state!
	 * 
	 */
	@Override
	public void clear() {
		// don't forget to pop the nameset
		namesset.clear();
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
	 * @param alias A proper name
	 * @param entity The entity information to save under this alias 
	 * @return A reference to the representative {@link EntityInfo} 
	 */
	public EntityInfo map(String alias, EntityInfo entity){
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
			if (!namesset.containsKey(e.getEntity())) {
				System.err.println("Warning: Unknown entity key. InitialState is inconsistent.");
			}
			return false;
		}
		ArrayList<EntityInfo> elist;
		
		if (namesset.containsKey(e.getEntity())) {
			elist = namesset.get(e.getEntity());
			elist.add(e);
			System.out.println("Yet another " + e.getEntity() + " added. This one now knows " + elist.size() + " " + e.getEntity() +"s.");
		}
		else {
			elist = new ArrayList<EntityInfo>();
			System.out.println("Oh, a " + e.getEntity() + ".");
		}
		
		elist.add(e);
		ArrayList<EntityInfo> result = namesset.put(e.getEntity(), elist);
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


	public boolean containsName(String n) {
		return namesset.containsKey(n);
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
		List<EntityInfo> infos = namesset.get(name);
		assert infos.size() == 1;
		return infos.get(0);
	}
	
	/**
	 * Returns the values to which the specified name is mapped, or null if this map contains no mapping for the name. 
	 * Use {@code getSingle(String)} if you want to assert a singleton result. 
	 * 
	 * @param name The given name or the entity name to look for
	 * @return all the entities found under this name 
	 */
	public ArrayList<EntityInfo> get(String name)
	{
		return namesset.get(name);
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
		return !namesset.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return super.contains(o);
	}

	public boolean hasName(String entity) {
		return null != getAssignedName(entity);
	}

	private EntityInfo getAssignedName(String entity) {
		ArrayList<EntityInfo> relatedentities = namesset.get(entity);
		for (EntityInfo entityInfo : relatedentities) {
			if(entityInfo.isProperName())
				return entityInfo;
		}
		return null;
	}

	/** Adds an alias which is also an EntityInfo
	 * 
	 * @param entity
	 */
	public void addAlias(EntityInfo entity) {
		entity.setIsProperName(true);
		ArrayList<EntityInfo> list = namesset.get(entity.getEntity());
		if(list == null)
			list = new ArrayList<>();
		
		list.add(entity);
		namesset.put(entity.getEntity(), list);
	}
	
	
}
