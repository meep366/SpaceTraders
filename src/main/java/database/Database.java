package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.scene.paint.Color;

import org.postgresql.util.PGobject;

import model.Condition;
import model.EncounterRate;
import model.GoodType;
import model.Government;
import model.Location;
import model.Planet;
import model.Player;
import model.Ship;
import model.ShipType;
import model.SpecialResource;
import model.TechLevel;
import model.Universe;

public class Database {
	private Connection connection;
	private String username;
	private Map<Integer, String> techLevelValues;
	private Map<String, String> specialResourceValues;
	private Map<String, String> governmentValues;
	private Map<String, String> encounterRateValues;
	private Map<String, String> conditionValues;
	private Map<String, String> goodTypeValues;
	private Map<String, String> shipTypeValues;

	/**
	 * Creates a new Database class and attempts to connect to the database
	 */
	public Database(String u) {
		username = u;
		techLevelValues = new HashMap<Integer, String>();
		specialResourceValues = new HashMap<String, String>();
		governmentValues = new HashMap<String, String>();
		encounterRateValues = new HashMap<String, String>();
		conditionValues = new HashMap<String, String>();
		goodTypeValues = new HashMap<String, String>();
		shipTypeValues = new HashMap<String, String>();
		connect();
		createMaps();
	}

	/**
	 * Attempts a connection to the database
	 */
	private void connect() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your PostgreSQL JDBC Driver?"
					+ "Include in your library path!");
			e.printStackTrace();
		}

		// System.out.println("PostgreSQL JDBC Driver Registered!");

		try {
			connection = DriverManager
					.getConnection(
							"jdbc:postgresql://spacetraders.cucctpybeipt.us-west-2.rds.amazonaws.com:5432/SpaceTraders",
							"meep366", "chromium");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		}
	}

	/**
	 * Creates the maps of enum values to UUIDs to be referenced in saving and
	 * loading later
	 */
	private void createMaps() {
		try {
			Statement s = connection.createStatement();
			ResultSet techLevels = s
					.executeQuery("SELECT \"TechLevelId\", \"TechLevelValue\" FROM \"TechLevel\"");
			while (techLevels.next()) {
				techLevelValues.put(techLevels.getInt("TechLevelValue"),
						techLevels.getString("TechLevelId"));
			}

			ResultSet specialResources = s
					.executeQuery("SELECT \"SpecialResourceId\", \"SpecialResourceName\" FROM \"SpecialResource\"");
			while (specialResources.next()) {
				specialResourceValues.put(
						specialResources.getString("SpecialResourceName"),
						specialResources.getString("SpecialResourceId"));
			}

			ResultSet governments = s
					.executeQuery("SELECT \"GovernmentId\", \"GovernmentName\" FROM \"Government\"");
			while (governments.next()) {
				governmentValues.put(governments.getString("GovernmentName"),
						governments.getString("GovernmentId"));
			}

			ResultSet conditions = s
					.executeQuery("SELECT \"ConditionId\", \"ConditionName\" FROM \"Condition\"");
			while (conditions.next()) {
				conditionValues.put(conditions.getString("ConditionName"),
						conditions.getString("ConditionId"));
			}

			ResultSet encounterRates = s
					.executeQuery("SELECT \"EncounterRateId\", \"EncounterRateName\" FROM \"EncounterRate\"");
			while (encounterRates.next()) {
				encounterRateValues.put(
						encounterRates.getString("EncounterRateName"),
						encounterRates.getString("EncounterRateId"));
			}

			ResultSet goodTypes = s
					.executeQuery("SELECT \"GoodTypeId\", \"GoodTypeName\" FROM \"GoodType\"");
			while (goodTypes.next()) {
				goodTypeValues.put(goodTypes.getString("GoodTypeName"),
						goodTypes.getString("GoodTypeId"));
			}

			ResultSet shipTypes = s
					.executeQuery("SELECT \"ShipTypeId\", \"ShipTypeName\" FROM \"ShipType\"");
			while (shipTypes.next()) {
				shipTypeValues.put(shipTypes.getString("ShipTypeName"),
						shipTypes.getString("ShipTypeId"));
			}

		} catch (SQLException s) {
			s.printStackTrace();
		}
	}

	/**
	 * Closes a connection with the database
	 */
	public void close() {
		System.out.println("Closing Connection");
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determines whether or not the given user exists in the database or not
	 * 
	 * @return Whether or not the user exists in the database already
	 */
	public boolean userExists() {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s
					.executeQuery("SELECT \"UserId\" FROM \"User\" WHERE \"Username\"='"
							+ username + "'");
			return r.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveGame(Universe universe, Player player) {
		long startTime = System.nanoTime();
		if (userExists()) {
			System.out.println("User exists, implementing update here");
		} else {
			try {
				connection.setAutoCommit(false);

				Ship ship = player.getShip();
				PreparedStatement shipInsertStatement = connection
						.prepareStatement("INSERT INTO \"Ship\" VALUES(?, ?, ?, ?)");
				UUID shipUUID = UUID.randomUUID();
				PGobject shipUUIDObject = new PGobject();
				shipUUIDObject.setType("uuid");
				shipUUIDObject.setValue(shipUUID.toString());
				shipInsertStatement.setObject(1, shipUUIDObject);

				PGobject shipTypeUUIDObject = new PGobject();
				shipTypeUUIDObject.setType("uuid");
				shipTypeUUIDObject.setValue(shipTypeValues.get(ship
						.getShipType().toString()));
				shipInsertStatement.setObject(2, shipTypeUUIDObject);

				shipInsertStatement.setInt(3, ship.getCurrHP());
				shipInsertStatement.setInt(4, ship.getFuel());
				shipInsertStatement.execute();

				for (GoodType g : GoodType.values()) {
					PreparedStatement shipCargoInsertStatement = connection
							.prepareStatement("INSERT INTO \"ShipCargo\" VALUES(?, ?, ?)");
					shipCargoInsertStatement.setObject(1, shipUUIDObject);
					PGobject shipGoodTypeUUIDObject = new PGobject();
					shipGoodTypeUUIDObject.setType("uuid");
					shipGoodTypeUUIDObject.setValue(goodTypeValues.get(g
							.toString()));
					shipCargoInsertStatement.setObject(2,
							shipGoodTypeUUIDObject);
					shipCargoInsertStatement.setInt(3, ship.amountInCargo(g));
					shipCargoInsertStatement.execute();
				}

				PreparedStatement playerInsertStatement = connection
						.prepareStatement("INSERT INTO \"Player\" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

				UUID playerUUID = UUID.randomUUID();
				PGobject playerUUIDObject = new PGobject();
				playerUUIDObject.setType("uuid");
				playerUUIDObject.setValue(playerUUID.toString());
				playerInsertStatement.setObject(1, playerUUIDObject);

				playerInsertStatement.setInt(2, player.getPilotSkill());
				playerInsertStatement.setInt(3, player.getFighterSkill());
				playerInsertStatement.setInt(4, player.getTraderSkill());
				playerInsertStatement.setInt(5, player.getEngineerSkill());
				playerInsertStatement.setInt(6, player.getInvestorSkill());
				playerInsertStatement.setInt(7, player.getTraderRep());
				playerInsertStatement.setInt(8, player.getPoliceRep());
				playerInsertStatement.setInt(9, player.getPirateRep());

				PGobject playerCreditsUUIDObject = new PGobject();
				playerCreditsUUIDObject.setType("numeric");
				playerCreditsUUIDObject.setValue(Double.toString(player
						.getCredits()));
				playerInsertStatement.setObject(10, playerCreditsUUIDObject);
				playerInsertStatement.setObject(11, shipUUIDObject);
				playerInsertStatement.setNull(12, Types.NULL);
				playerInsertStatement.setString(13, player.getName());
				playerInsertStatement.execute();

				PreparedStatement userInsertStatement = connection
						.prepareStatement("INSERT INTO \"User\" VALUES(?, ?, ?)");
				UUID userUUID = UUID.randomUUID();
				PGobject userUUIDObject = new PGobject();
				userUUIDObject.setType("uuid");
				userUUIDObject.setValue(userUUID.toString());
				userInsertStatement.setObject(1, userUUIDObject);
				userInsertStatement.setString(2, username);
				userInsertStatement.setString(3, "password");
				userInsertStatement.execute();

				PreparedStatement userPlayersInsertStatement = connection
						.prepareStatement("INSERT INTO \"UserPlayers\" VALUES(?, ?)");
				userPlayersInsertStatement.setObject(1, userUUIDObject);
				userPlayersInsertStatement.setObject(2, playerUUIDObject);
				userPlayersInsertStatement.execute();

				PreparedStatement planetInsertStatement = connection
						.prepareStatement("INSERT INTO \"Planet\" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				PreparedStatement playerPlanetsInsertStatement = connection
						.prepareStatement("INSERT INTO \"PlayerPlanets\" VALUES(?, ?)");
				List<Planet> planets = universe.getPlanets();
				for (Planet p : planets) {
					UUID planetUUID = UUID.randomUUID();
					PGobject planetUUIDObject = new PGobject();
					planetUUIDObject.setType("uuid");
					planetUUIDObject.setValue(planetUUID.toString());
					planetInsertStatement.setObject(1, planetUUIDObject);

					PGobject techLevelUUIDObject = new PGobject();
					techLevelUUIDObject.setType("uuid");
					techLevelUUIDObject.setValue(techLevelValues.get(p
							.getTechLevel().getValue()));
					planetInsertStatement.setObject(2, techLevelUUIDObject);

					PGobject resourceUUIDObject = new PGobject();
					resourceUUIDObject.setType("uuid");
					resourceUUIDObject.setValue(specialResourceValues.get(p
							.getResource().toString()));
					planetInsertStatement.setObject(3, resourceUUIDObject);

					PGobject governmentUUIDObject = new PGobject();
					governmentUUIDObject.setType("uuid");
					governmentUUIDObject.setValue(governmentValues.get(p
							.getGovernment().toString()));
					planetInsertStatement.setObject(4, governmentUUIDObject);

					planetInsertStatement.setInt(5, p.getLocation().getX());
					planetInsertStatement.setInt(6, p.getLocation().getY());

					PGobject conditionUUIDObject = new PGobject();
					conditionUUIDObject.setType("uuid");
					conditionUUIDObject.setValue(conditionValues.get(p
							.getCondition().toString()));
					planetInsertStatement.setObject(7, conditionUUIDObject);

					PGobject policeEncounterUUIDObject = new PGobject();
					policeEncounterUUIDObject.setType("uuid");
					policeEncounterUUIDObject.setValue(encounterRateValues
							.get(p.getPoliceEncounterRate().toString()));
					planetInsertStatement.setObject(8,
							policeEncounterUUIDObject);

					PGobject pirateEncounterUUIDObject = new PGobject();
					pirateEncounterUUIDObject.setType("uuid");
					pirateEncounterUUIDObject.setValue(encounterRateValues
							.get(p.getPirateEncounterRate().toString()));
					planetInsertStatement.setObject(9,
							pirateEncounterUUIDObject);

					planetInsertStatement.setString(10, p.getName());
					planetInsertStatement.execute();

					playerPlanetsInsertStatement.setObject(1, playerUUIDObject);
					playerPlanetsInsertStatement.setObject(2, planetUUIDObject);
					playerPlanetsInsertStatement.execute();

					// PreparedStatement marketplaceInsertStatement =
					// connection.prepareStatement("INSERT INTO \"Marketplace\" VALUES(?, ?)");
					// UUID marketplaceUUID = UUID.randomUUID();
					// PGobject marketplaceUUIDObject = new PGobject();
					// marketplaceUUIDObject.setType("uuid");
					// marketplaceUUIDObject.setValue(marketplaceUUID.toString());
					// marketplaceInsertStatement.setObject(1,
					// marketplaceUUIDObject);
					//
					// marketplaceInsertStatement.setObject(2,
					// planetUUIDObject);
					// marketplaceInsertStatement.execute();
					//
					//
					//
					// Marketplace m = p.getMarketplace();
					// for(GoodType g: GoodType.values()) {
					// PreparedStatement marketplaceGoodsInsertStatement =
					// connection.prepareStatement("INSERT INTO \"MarketplaceGoods\" VALUES(?, ?, ?, ?)");
					// marketplaceGoodsInsertStatement.setObject(1,
					// marketplaceUUIDObject);
					// PGobject marketplaceGoodTypeUUIDObject = new PGobject();
					// marketplaceGoodTypeUUIDObject.setType("uuid");
					// marketplaceGoodTypeUUIDObject.setValue(goodTypeValues.get(g.toString()));
					// marketplaceGoodsInsertStatement.setObject(2,
					// marketplaceGoodTypeUUIDObject);
					// marketplaceGoodsInsertStatement.setInt(3,
					// m.getQuantity(g));
					// marketplaceGoodsInsertStatement.setInt(4, (int)
					// m.getSellPrice(g));
					// marketplaceGoodsInsertStatement.execute();
					// }

				}

				Statement s = connection.createStatement();
				ResultSet planet = s
						.executeQuery("SELECT \"PlanetId\" FROM \"Planet\" WHERE \"PlanetName\"='"
								+ player.getPlanet().getName() + "'");
				planet.next();
				String planetUUID = planet.getString("PlanetId");
				s.execute("UPDATE \"Player\" SET \"PlanetId\"='" + planetUUID
						+ "' WHERE \"PlayerId\"='" + playerUUID + "'");

				connection.commit();
				connection.setAutoCommit(true);

			} catch (SQLException s) {
				try {
					connection.setAutoCommit(true);
					connection.rollback();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
				s.printStackTrace();
			}
		}

		System.out.println("Save Time: " + (System.nanoTime() - startTime)
				/ 1000000000.);
	}

	/**
	 * Loads the users game from the database
	 * 
	 * @return An object[] of the universe and planets
	 */
	public Object[] loadGame() {
		Player p = null;
		Universe u = null;
		try {

			Statement s = connection.createStatement();
			String execPlayerStatement = "SELECT p.\"PlayerId\", p.\"PlayerName\", p.\"PilotSkill\", p.\"FighterSkill\", p.\"TraderSkill\", p.\"EngineerSkill\", p.\"InvestorSkill\", p.\"TraderReputation\", p.\"PoliceReputation\", p.\"PirateReputation\", p.\"Credits\", s.\"ShipId\", s.\"CurrentHullPoints\",  s.\"CurrentFuel\", st.\"ShipTypeName\" FROM \"User\" u INNER JOIN \"UserPlayers\" up ON u.\"UserId\"=up.\"UserId\" INNER JOIN \"Player\" p ON p.\"PlayerId\"=up.\"PlayerId\" INNER JOIN \"Ship\" s ON p.\"ShipId\"=s.\"ShipId\" INNER JOIN \"ShipType\" st ON s.\"ShipTypeId\"=st.\"ShipTypeId\" WHERE u.\"Username\"='"
					+ username + "'";

			ResultSet playerInfo = s.executeQuery(execPlayerStatement);
			playerInfo.next();
			String shipId = playerInfo.getString("ShipId");
			String playerId = playerInfo.getString("PlayerId");
			double credits = Double.valueOf(playerInfo.getString("Credits"));
			ShipType type = ShipType.valueOf(playerInfo.getString(
					"ShipTypeName").toUpperCase());
			Ship ship = new Ship(type);
			p = new Player(playerInfo.getString("PlayerName"),
					playerInfo.getInt("PilotSkill"),
					playerInfo.getInt("FighterSkill"),
					playerInfo.getInt("TraderSkill"),
					playerInfo.getInt("EngineerSkill"),
					playerInfo.getInt("InvestorSkill"), ship, credits,
					playerInfo.getInt("TraderReputation"),
					playerInfo.getInt("PoliceReputation"),
					playerInfo.getInt("PirateReputation"));

			ResultSet shipCargo = s
					.executeQuery("SELECT goods.\"GoodTypeName\", cargo.\"GoodTypeQuantity\" FROM \"ShipCargo\" cargo INNER JOIN \"GoodType\" goods ON cargo.\"GoodTypeId\"=goods.\"GoodTypeId\" WHERE cargo.\"ShipId\"='"
							+ shipId + "'");

			while (shipCargo.next()) {
				ship.addToCargo(GoodType.valueOf(shipCargo.getString(
						"GoodTypeName").toUpperCase()), shipCargo
						.getInt("GoodTypeQuantity"));
			}
			String execPlanetStatement = "SELECT p.\"PlanetName\", tl.\"TechLevelName\", sr.\"SpecialResourceName\", g.\"GovernmentName\", p.\"LocationX\", p.\"LocationY\", c.\"ConditionName\", er.\"EncounterRateName\" AS \"PoliceEncounterRateId\", pirEr.\"EncounterRateName\" AS \"PirateEncounterRateId\" FROM \"Planet\" p INNER JOIN \"PlayerPlanets\" pp ON p.\"PlanetId\"=pp.\"PlanetId\" INNER JOIN  \"TechLevel\" tl ON tl.\"TechLevelId\"=p.\"TechLevelId\" INNER JOIN \"SpecialResource\" sr ON sr.\"SpecialResourceId\"=p.\"SpecialResourceId\" INNER JOIN \"Government\" g ON g.\"GovernmentId\"=p.\"GovernmentId\" INNER JOIN \"Condition\" c ON c.\"ConditionId\"=p.\"ConditionId\" INNER JOIN \"EncounterRate\" er ON er.\"EncounterRateId\"=p.\"PoliceEncounterRateId\" INNER JOIN \"EncounterRate\" pirEr ON pirEr.\"EncounterRateId\"=p.\"PirateEncounterRateId\" WHERE pp.\"PlayerId\"='"
					+ playerId + "'";
			ResultSet planets = s.executeQuery(execPlanetStatement);
			List<Planet> planetList = new ArrayList<Planet>();
			while (planets.next()) {
				String planetName = planets.getString("PlanetName");
				TechLevel techLevel = TechLevel.getEnum(planets.getString(
						"TechLevelName").toUpperCase());
				SpecialResource resource = SpecialResource.getEnum(planets
						.getString("SpecialResourceName").toUpperCase());
				Government government = Government.valueOf(planets.getString(
						"GovernmentName").toUpperCase());
				Location location = new Location(planets.getInt("LocationX"),
						planets.getInt("LocationY"));
				Condition condition = Condition.getEnum(planets.getString(
						"ConditionName").toUpperCase());
				EncounterRate pirates = EncounterRate.valueOf(planets
						.getString("PirateEncounterRateId").toUpperCase());
				EncounterRate police = EncounterRate.valueOf(planets.getString(
						"PoliceEncounterRateId").toUpperCase());
				EncounterRate trader = EncounterRate.SWARMS;
				Planet planet = new Planet(planetName, techLevel, resource,
						government, location, condition, pirates, police,
						trader, 5, Color.GREEN);
				planetList.add(planet);
				u = new Universe(planetList);
			}

		} catch (SQLException s) {
			s.printStackTrace();
		}

		p.setPlanet(u.getPlanets().get(0));
		return new Object[] { p, u };
	}
}