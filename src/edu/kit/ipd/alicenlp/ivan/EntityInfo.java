package edu.kit.ipd.alicenlp.ivan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.Attributes.Name;

import javax.swing.text.html.parser.Entity;

import org.eclipse.swt.internal.mozilla.nsDynamicFunctionLoad;


/**
 * The {@link EntityInfo} is a triple of <Entity, Location, Direction> and represents a single Entity's state in Alice.
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
				// this first check allows nulls in location and direction
				if (other.Entity.equalsIgnoreCase(Entity) 
						&& other.Location == Location
						&& other.Direction == Direction) {
					return true;
				}
				// if the first check fails, there is a non-null feature, so those have to be compared as well
				else if (other.Entity.equalsIgnoreCase(Entity) 
						&& other.Location.equalsIgnoreCase(Location)
						&& other.Direction.equalsIgnoreCase(Direction)) {
					return true;
				}
			}
		} catch (NullPointerException e) {
			// underspecified entities never equal anything
			return false;
		}
		return super.equals(obj);
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
