package edu.kit.ipd.alicenlp.ivan.data;



/**
 * The {@link EntityInfo} is a triple of <Entity, Locations, Direction> and represents a single Entity's state in Alice.
 * @author Jonny
 *
 */
public class EntityInfo 
{

	private String Entity = null,
		Location = null,
		Direction = null;

	public String getEntity() {
		return Entity;
	}

	public String getLocation() {
		return Location;
	}

	public void setLocation(String location) {
		Location = location;
	}

	public String getDirection() {
		return Direction;
	}

	public void setDirection(String direction) {
		Direction = direction;
	}
	
	public EntityInfo(String Entity)
	{
		if (Entity == null) {
			throw new IllegalArgumentException("null is not a valid Entity description");
		}
		this.Entity = Entity;
	}
	
	public EntityInfo(String Entity, String Location)
	{
		if (Entity == null) {
			throw new IllegalArgumentException("null is not a valid Entity description");
		}
		this.Entity = Entity;
		this.Location = Location;
	}
	
	public EntityInfo(String Entity, String Location, String Direction)
	{
		if (Entity == null) {
			throw new IllegalArgumentException("null is not a valid Entity description");
		}
		this.Entity = Entity;
		this.Location = Location;
		this.Direction = Direction;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this.Entity == null) {
			// underspecified entities never equal anything
			return false;
		}
		try {
			if (obj.getClass().equals(EntityInfo.class)) {
				EntityInfo other = (EntityInfo) obj;
				
				// are they the same thing? this is usually the case if both features are null
				boolean locsSame = other.Location == Location;
				boolean dirsSame = other.Direction == Direction;
				
				// are they at least equal? this may be the case if both features are strings
				// perform check only if there was a difference in the previous check
				boolean locsEqual = locsSame;
				if(!locsSame)
					locsEqual = other.Location.equalsIgnoreCase(Location);
				boolean dirsEqual = dirsSame;
				if(!dirsSame)
					dirsEqual = other.Direction.equalsIgnoreCase(Direction);
				
				// if the references equal OR the values equal, the features is alright
				boolean locsOk = locsSame || locsEqual;
				boolean dirsOk = dirsSame || dirsEqual;
				
				// if the names are equal, return the feature equality
				if (other.Entity.equalsIgnoreCase(Entity)) {
					// features are equal if all features are equal
					return locsOk && dirsOk;
				}
				
			}
		} catch (NullPointerException e) {
			// underspecified entities never equal anything
			return false;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		// two entityinfos are equal, when their string representation looks the same
		return this.toString().hashCode();	
	}

	@Override
	public String toString() {
		return "<"+ Entity + ", " + Location + ", " + Direction + ">";
	}

	public boolean hasLocation() {
		return Location != null;
	}
	
	public boolean hasDirection() {
		return Direction != null;
	}

	/**
	 * Overwrite this Entityinfos's location and direction with the given data (if not null)
	 * @param e
	 */
	public void update(EntityInfo e) {
		if (e.hasDirection()) {
			this.Direction = e.getDirection();
		}		
		if (e.hasLocation()) {
			this.Location = e.getLocation();
		}
	}
}
