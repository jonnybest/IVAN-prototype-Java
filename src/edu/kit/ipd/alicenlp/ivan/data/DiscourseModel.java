/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import edu.kit.ipd.alicenlp.ivan.IvanInvalidMappingException;
import edu.stanford.nlp.util.Pair;

/**
 * This class manages a set of names and EntityInfos
 * 
 * @author Jonny
 * 
 */
public class DiscourseModel extends HashSet<EntityInfo> {
	private static Logger log = Logger.getLogger(DiscourseModel.class.toString());

	/**
	 * something to make the warning go away
	 * 
	 */
	private static final long serialVersionUID = 1058L;

	/**
	 * The set of all known aliases and automated aliases, linked to their
	 * respective entity
	 */
	private HashMap<String, ArrayList<EntityInfo>> aliases = new HashMap<String, ArrayList<EntityInfo>>();
	/** The set of all known names */
	private HashMap<String, List<String>> entitiesToAliases = new HashMap<String, List<String>>();

	/**
	 * Clears ALL the state!
	 * 
	 */
	@Override
	public void clear() {
		// don't forget to pop the nameset
		aliases.clear();
		super.clear();
	}

	/**
	 * Create a well-known alias for this entity info
	 * 
	 * @param alias
	 *            A proper name
	 * @param entity
	 *            The entity information to save under this alias
	 * @throws IvanInvalidMappingException 
	 * 
	 */
	public void map(String alias, EntityInfo entity) throws IvanInvalidMappingException{
		// shortcut for handling special null mappings
		if(entity == null)
		{
			// add a null mapping for an unknown entity
			// if this alias does not already exist, add it.
			if(!alias.contains(alias))
			{
				// there are already null mappings in place. append ours
				if (entitiesToAliases.containsKey(null)) {
					List<String> existing = entitiesToAliases.get(null);
					// check for duplicates and append
					if(!existing.contains(alias))
						existing.add(alias);
				}
				// there are no null mappings so far. create a list and append ours
				else {
					List<String> mylist = new ArrayList<>();
					// check for duplicates and append
					if(!mylist.contains(alias))
						mylist.add(alias);
					entitiesToAliases.put(null, mylist);
				}
			}
			// else: if this alias already exists, there is no new information in the null mapping
			return;
		}
		
		// not allowed: creating explicit mappings between identical names
		if(alias.equals(entity.getEntity()))
			throw new IvanInvalidMappingException("Cannot map an alias to an entity with the same name as the alias.");
		
		// make sure we know this entity info
		this.add(entity);
				
		ArrayList<EntityInfo> existingEntityInfos = new ArrayList<>();
		
		// retrieve existing entities under the entity 
		if(aliases.containsKey(entity.getEntity()))
		{	
			ArrayList<EntityInfo> existing1 = aliases.get(entity.getEntity()); // from fake mappings
			// remove previous mapping
			aliases.remove(entity.getEntity());
			// merge existing infos
			existingEntityInfos.addAll(existing1);
		}		
		
		// retrieve existing entities under the alias
		if(aliases.containsKey(alias)) // if there was no fake mapping,...
		{
			// maybe there is a real mapping already existant?
			// retrieve existing aliases for this entity			
			ArrayList<EntityInfo> existing2 = aliases.get(alias);
			
			// merge
			existingEntityInfos.addAll(existing2);
		}		
		
		// create the mapping alias -> entityinfos
		aliases.put(alias, existingEntityInfos);
		
		// remove any null mappings if present
		// get the null mappings
		List<String> unknowns = entitiesToAliases.get(null);
		if(unknowns != null && unknowns.contains(alias))
		{
			// remove this alias from the unknowns. it will get a proper mapping now
			unknowns.remove(alias);
		}
		
		// remove any bad mappings if present. An alias is not allowed to have a entity class mapping
		entitiesToAliases.remove(alias);
		
		// retrieve entity -> alias mapping
		if(!entitiesToAliases.containsKey(entity.getEntity()))
		{			
			// this entity was previously unknown
			// set up new mapping
			ArrayList<String> maplist = new ArrayList<String>();
			maplist.add(alias);
			entitiesToAliases.put(entity.getEntity(), maplist);
		}	
		else {
			// we already know this entity class
			List<String> existingNames = entitiesToAliases.get(entity.getEntity());
			// let's see if we already know this particular named entity
			if(existingNames.contains(alias))
			{
				// yes. there is nothing to do here
			}
			else {
				// no. let's append this new mapping
				existingNames.add(alias);
				// and also, remove any fake mappings
				existingNames.remove(entity.getEntity());
			}
		}
	}

	/**
	 * This method attempts to add the EntityInfo into this DiscourseModel.
	 */
	@Override
	public boolean add(EntityInfo e) {
		// lets see if this entity is already in our set
		boolean addednewly = super.add(e);
		if (!addednewly) {
			// this one is already present. there is nothing to do here
			if (!entitiesToAliases.containsKey(e.getEntity())) {
				if (!aliases.containsKey(e.getEntity()))
					System.err
							.println("Warning: Unknown entity key. DiscourseModel may be inconsistent.");
			}
			return false;
		}

		ArrayList<EntityInfo> elist;

		if (e.isProperName()) {
			// try to find existing entity infos in aliases
			if (aliases.containsKey(e.getEntity())) {
				// simply append
				elist = aliases.get(e.getEntity());
				elist.add(e);
				log.info("Yet another " + e.getEntity()
						+ " added. This one now knows " + elist.size() + " "
						+ e.getEntity() + "s.");
			} else {
				// create a new list and append
				elist = new ArrayList<EntityInfo>();
				elist.add(e);
				// create a new alias
				aliases.put(e.getEntity(), elist);
				// create a null-mapping to this alias
				ArrayList<String> mylist = new ArrayList<>();
				mylist.add(e.getEntity());
				entitiesToAliases.put(null, mylist);
			}
		} else { // no proper name
					// try to find existing entities in entitiesToAliases
			if (entitiesToAliases.containsKey(e.getEntity())) {
				// retrieve list
				String alias = getFirstAlias(e.getEntity());
				elist = aliases.get(alias);
				// append
				elist.add(e);
			} else { // entity not contained
						// create a new elist
				elist = new ArrayList<>();
				elist.add(e);

				// create an "alias" and save this entity to it
				aliases.put(e.getEntity(), elist);

				// set up a new entites mapping with this entity class as "name"
				ArrayList<String> mylist = new ArrayList<String>();
				mylist.add(e.getEntity());
				entitiesToAliases.put(e.getEntity(),
						mylist);
			}
		}

		return true;
	}

	private String getFirstAlias(String entity) {
		return entitiesToAliases.get(entity).get(0);
	}

	/**
	 * This method attempts to add all the EntityInfos into this DiscourseModel.
	 * It will not update any existing entries.
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
		return aliases.containsKey(n);
	}

	public boolean containsAllNames(Collection<String> names) {
		for (String name : names) {
			if (!containsName(name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the first value to which the specified name is mapped, or
	 * null if this map contains no mapping for the name. 
	 * 
	 * @param name
	 * @return
	 */
	public EntityInfo getSingle(String name) {
		if(!aliases.containsKey(name))
			return null;
		
		List<EntityInfo> infos = aliases.get(name); // span = null
		if(infos.size() > 0)
			return infos.get(0);
		else 
			return null;
//		// if there is only one, return that
//		if(infos.size() == 1)
//		{
//			return infos.get(0);
//		}
//		else {
//			// merge existing infos into a new one
//			EntityInfo resultinfo = new EntityInfo(name);
//			for (EntityInfo ei : infos) {
//				EntityInfo tmp = resultinfo; // save progress
//				
//				// if the entity is not a proper name OR if the entity is not from the text, we consider swapping them
//				if (!ei.isProperName() || resultinfo.getEntitySpan() == null) {
//					// the best entity description is always the longest non-aliased mention 
//					if(ei.getEntity().length() >= resultinfo.getEntity().length())
//					{
//						tmp = resultinfo; // swap infos
//						resultinfo = ei; // put best matching info
//					}
//				}
//
//				// merge progress
//				if(!resultinfo.hasLocation())
//				{
//					resultinfo.setLocation(tmp.getLocation());
//				}
//				if(!resultinfo.hasDirection())
//				{
//					resultinfo.setDirection(tmp.getDirection());
//				}
//				
//			}
//			return resultinfo;
//		}
	}

	/**
	 * Returns the values to which the specified name is mapped, or null if this
	 * map contains no mapping for the name. Use {@code getSingle(String)} if
	 * you want to assert a singleton result.
	 * 
	 * @param name
	 *            The given name or the entity name to look for
	 * @return all the entities found under this name
	 */
	public ArrayList<EntityInfo> get(String name) {
		
		ArrayList<EntityInfo> list = new ArrayList<EntityInfo>(); // result collection
		
		if(entitiesToAliases.containsKey(name))
		{
			// it's a class handle. retrieve and merge all the info
			for (String alias : entitiesToAliases.get(name)) {
				list.addAll(aliases.get(alias));
			}
			return list;
		}
		
		if(aliases.containsKey(name))
		{
			// it's an alias. return its list
			return aliases.get(name);
		}
		
		// found nothing
		return null;
	}

	/**
	 * This method creates a simple view onto the entites with names: The left
	 * hand string is the entity, the right hand string its assigned name.
	 * 
	 * @return a list where each pair corresponds to exactly one entity
	 */
	public List<Pair<String, String>> getEntityNames() {
		List<Pair<String, String>> list = new ArrayList<>();
		for (String entity : entitiesToAliases.keySet()) {
			for (String alias : entitiesToAliases.get(entity)) {
				Pair<String, String> pair = new Pair<String, String>(entity,
						alias);
				list.add(pair);
			}
		}
		return list;
	}

	/**
	 * Returns whether there are any names assigned to the contained entities
	 * 
	 * @return TRUE if at least one entity has a proper name.
	 */
	public boolean hasAliases() {
		for (String thing : entitiesToAliases.keySet()) {
			if (thing != entitiesToAliases.get(thing).get(0))
				return true;
		}
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return super.contains(o);
	}

	/**
	 * Describes, if a proper name has been assigned to this entity
	 * 
	 * @param entity
	 * @return
	 */
	public boolean hasName(String entity) {
		if(entity == null)
			return false;
		if(!entitiesToAliases.containsKey(entity))
			return false;
		List<String> mapped = entitiesToAliases.get(entity);
		if(mapped == null)
			return false;
		// this is the criterion for "the user designated a proper name":
		// if the known handle (alias) is the same as the entity class, there is no name
		// if the alias and the entity class differ, a proper name has been assigned
		return !mapped.contains(entity);
	}

	/**
	 * Returns all the names which are assigned to a given entity class
	 * 
	 * @param entityClass
	 *            The type of an entity like "dog" or "cat".
	 * @return The names which are assigned to entitites in this class, like
	 *         "Spanky" or "Fluffy"
	 */
	public List<String> getNames(String entityClass) {
		return entitiesToAliases.get(entityClass);
	}

	/** Find the entity type for a given alias.
	 * 
	 * @param Alias The proper name to look for
	 * @return The known entity type or <code>null</code> if no entity type is known.
	 */
	public String getEntity(String Alias) {
		if(aliases.containsKey(Alias))
		{
			for (Entry<String, List<String>> mapping : entitiesToAliases.entrySet()) {
				if(mapping.getValue().contains(Alias))
					return mapping.getKey();
			}
		}
		return null;
	}

	/** Find out if this alias has been initialized.
	 * Specifically, this method looks at the null-mappings, which contains all unmapped aliases. 
	 * 
	 * @param alias
	 * @return
	 */
	public boolean hasEntity(String alias) {
		return entitiesToAliases.get(null) == null || !entitiesToAliases.get(null).contains(alias);
	}

	@Override
	public String toString()
	{
		StringBuilder cb = new StringBuilder();
		cb.append(this.entitiesToAliases.toString());
		cb.append(super.toString());
		return cb.toString();
	}

	/** Removes a given mapping and all assigned entity information from the state
	 * 
	 * @param entity The known entity or null (if unknown)
	 * @param alias The known alias or null (if unknown)
	 * @return TRUE if anything was removed
	 */
	public boolean remove(String entity, String alias) {
		boolean worked = false;
		List<String> alist = this.entitiesToAliases.get(entity);
		if (alist != null) {
			worked |= alist.remove(alias);
			if (alias.isEmpty()) {
				this.entitiesToAliases.remove(entity);
			}
		}
		ArrayList<EntityInfo> infolist = this.aliases.get(alias);
		if(infolist != null)
		{
			worked |= this.removeAll(infolist);
			this.aliases.remove(alias);
		}
		return worked;
	}
}
