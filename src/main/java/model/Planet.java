package model;

/**
 * Class representing a planet
 * 
 * @author Larry He
 * 
 */
public class Planet {
	private String name;
	private TechLevel techLevel;
	private SpecialResource resource;
	private Government government;
	private Location location;
	private Marketplace marketplace;

	/**
	 * Basic Planet constructor taking a tech level and a resource
	 * 
	 * @param tech
	 *            The tech level for the planet
	 * @param r
	 *            The resource for the planet
	 * @param n
	 *            The name of the planet
	 * @param g
	 *            The government type of the planet
	 * @param l
	 *            The location of the planet in space
	 */
	public Planet(String n, TechLevel tech, SpecialResource r, Government g,
			Location l) {
		setName(n);
		setTechLevel(tech);
		setResource(r);
		setGovernment(g);
		setLocation(l);
		marketplace = new Marketplace(this);
	}

	/**
	 * Private setter for name validation
	 * 
	 * @param n
	 *            The name of the planet to be set
	 */
	private void setName(String n) {
		if (n == null || n.equals(""))
			throw new IllegalArgumentException();
		name = n;
	}

	/**
	 * Private setter for tech level validation
	 * 
	 * @param tl
	 *            The tech level of the planet to be set
	 */
	private void setTechLevel(TechLevel tl) {
		if (tl == null)
			throw new IllegalArgumentException();
		techLevel = tl;
	}

	/**
	 * Private setter for resource validation
	 * 
	 * @param r
	 *            The resource of the planet to be set
	 */
	private void setResource(SpecialResource r) {
		if (r == null)
			throw new IllegalArgumentException();
		resource = r;
	}

	/**
	 * Private setter for government type validation
	 * 
	 * @param r
	 *            The government type of the planet to be set
	 */
	private void setGovernment(Government g) {
		if (g == null)
			throw new IllegalArgumentException();
		government = g;
	}

	/**
	 * Private setter for Location type validation
	 * 
	 * @param r
	 *            The location of the planet to be set
	 */
	private void setLocation(Location l) {
		if (l == null)
			throw new IllegalArgumentException();
		location = l;
	}

	/**
	 * Getter for the name of the planet
	 * 
	 * @return The name of the planet
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the tech level for the planet
	 * 
	 * @return The tech level of the planet
	 */
	public TechLevel getTechLevel() {
		return techLevel;
	}

	/**
	 * Getter for the resource of the planet
	 * 
	 * @return The resource of the planet
	 */
	public SpecialResource getResource() {
		return resource;
	}

	/**
	 * Getter for the government type of the planet
	 * 
	 * @return The government of the planet
	 */
	public Government getGovernment() {
		return government;
	}

	/**
	 * Getter for the location of the planet
	 * 
	 * @return The location of the planet
	 */
	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return name + " Location: " + location + ", Tech Level: " + techLevel
				+ ", Resource: " + resource + ", Goverment: " + government;
	}
}