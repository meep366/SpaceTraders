package view;

import controller.GameEngine;
import model.Encounter;
import model.Location;
import model.NPCEncounter;
import model.Planet;
import model.Player;
import model.Ship;
import model.Universe;

import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller for the screen directing travel between planets
 * 
 * @author Hemen Shah
 *
 */
public class TravelScreenController implements Controller {
    private GameEngine game;
    private Planet selectedPlanet;
    private List<Encounter> encounters;

    @FXML
    private Canvas miniMapCanvas;

    @FXML
    private Button backBtn;

    @FXML
    private Canvas localMapCanvas;

    @FXML
    private Button goBtn;

    @FXML
    private Button nextBtn;

    @FXML
    private Label planetNameLbl;

    @FXML
    private Label governmentLbl;

    @FXML
    private Label planetTechLevelLbl;

    @FXML
    private Label distanceToPlanetLbl;

    @FXML
    private Label policeLevelLbl;

    @FXML
    private Label pirateLevelLbl;

    @FXML
    private Label traderLevelLbl;

    @FXML
    private Label fuelLbl;

    private GraphicsContext localGraphicsContext;
    private int universeSize;
    private int mapSize = 400;

    /**
     * Changes the currently selected planet to the next planet in the list
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void showNextPlanet(Event event) {
        if (MultiPageController.isValidAction(event)) {
            List<Planet> withinRange = game.getPlanetsWithinRange();
            int index = withinRange.indexOf(selectedPlanet);
            selectedPlanet = withinRange.get((index + 1) % withinRange.size());
            setPlanetInfo();
        }
    }

    /**
     * Selects a given region of space that has been clicked in the mini-map
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void selectRegionOfSpace(Event eve) {
//        MouseEvent event = (MouseEvent) eve;
//        double x = event.getX();
//        double y = event.getY();
    }

    /**
     * Selects a planet that has been clicked on the local map
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void selectPlanet(Event eve) {
        MouseEvent event = (MouseEvent) eve;
        double xpos = event.getX() / (mapSize / universeSize);
        double ypos = event.getY() / (mapSize / universeSize);
        Planet planet = game.getPlanetAtLocation(new Location((int) xpos, (int) ypos));
        if (planet != null) {
            selectedPlanet = planet;
            setPlanetInfo();
        }
    }

    /**
     * Sends the player back to the planet screen
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void toPlanetScreen(Event event) {
        if (MultiPageController.isValidAction(event)) {
            try {
                Stage stage = (Stage) backBtn.getScene().getWindow();
                stage.hide();
                FXMLLoader loader = new FXMLLoader(
                        ClassLoader.getSystemResource("view/PlanetScreen.fxml"));
                Parent newScene = loader.load();
                stage.setScene(new Scene(newScene, 600, 400));
                PlanetScreenController controller = loader.getController();
                controller.initializePage();
                stage.show();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Starts the process for sending the user to a new planet, and checks to make sure a valid
     * planet is selected
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void goToSelectedPlanet(Event event) {
        int distance = game.getDistanceToPlanet(selectedPlanet);
        Player player = game.getPlayer();
        if (selectedPlanet != player.getPlanet()) {
            if (player.getShip().getFuel() >= distance) {
                payMercenaries();
                encounters = game.goToPlanet(selectedPlanet);
                doEncounters();
                setPlanetInfo();
            } else {
                displayMessage("You do not have enough fuel", "Error");
            }
        } else {
            displayMessage("You are already on this planet", "Error");
        }
    }

    /**
     * Pays the mercenaries the player has hired. If the player can't afford to pay,
     * the mercenaries return to their home planet
     */
    @FXML
    private void payMercenaries() {
        int payment = game.payMercenaries();
        if (payment == -1) {
            displayMessage("You do not have enough credits to pay for your mercenaries. "
                    + "They leave your ship and return to their home planets.", "Mercenaries leave");
        } else if (payment > 0) {
            displayMessage("You have paid your mercenaries " + payment
                    + " credits for their hard work.", "Mercenaries paid!");
        }
    }
    
    @Override
    public void updatePage() {
        if (encounters.size() > 0) {
            doEncounters();
        } else {
            Ship ship = game.getPlayer().getShip();
            ship.addShieldHp(ship.getMaxShieldHp());
        }
    }

    /**
     * Method for continuously displaying the next encounter to the player
     */
    private void doEncounters() {
        boolean npcEncounter = false;
        while (encounters.size() != 0 && !npcEncounter) {
            Encounter encounter = encounters.remove(0);
            Dialogs.create().owner(goBtn.getScene().getWindow()).title("Encounter")
                    .message(encounter.doEncounter()).showInformation();

            if (encounter instanceof NPCEncounter) {
                try {
                    Stage encounterPopup = new Stage();
                    encounterPopup.initModality(Modality.APPLICATION_MODAL);
                    encounterPopup.initOwner((Stage) goBtn.getScene().getWindow());

                    FXMLLoader loader = new FXMLLoader(
                            ClassLoader.getSystemResource("view/NPCEncounterPopup.fxml"));
                    Parent newScene = loader.load();
                    encounterPopup.setScene(new Scene(newScene, 400, 300));

                    NPCEncounterController controller = loader.getController();
                    controller.initializePage(this, (NPCEncounter) encounter);
                    encounterPopup.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        public void handle(WindowEvent we) {
                            we.consume();
                        }
                    });
                    encounterPopup.show();

                    npcEncounter = true;
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads up the travel screen
     */
    public void initializePage() {
        localGraphicsContext = localMapCanvas.getGraphicsContext2D();
        game = GameEngine.getGameEngine();
        Universe uni = game.getUniverse();
        universeSize = uni.getUniverseSize();
        int xpos;
        int ypos;
        int radius;
        localGraphicsContext.setFill(Color.BLACK);
        localGraphicsContext.fillRect(0, 0, 400, 400);
        for (Planet p : uni.getPlanets()) {
            xpos = p.getLocation().getX() * (mapSize / universeSize);
            ypos = p.getLocation().getY() * (mapSize / universeSize);
            radius = p.getDiameter();
            localGraphicsContext.setFill(p.getColor());
            localGraphicsContext.fillOval(xpos, ypos, radius, radius);
        }
        selectedPlanet = game.getPlayer().getPlanet();
        setPlanetInfo();
    }

    /**
     * Sets all the planet labels with the relevant planet information
     */
    public void setPlanetInfo() {
        planetNameLbl.setText(selectedPlanet.getName());
        governmentLbl.setText("Government: " + selectedPlanet.getGovernment());
        planetTechLevelLbl.setText("Tech Level: " + selectedPlanet.getTechLevel());
        distanceToPlanetLbl.setText("Distance: " + game.getDistanceToPlanet(selectedPlanet)
                + " parsecs");
        policeLevelLbl.setText("Police Level: " + selectedPlanet.getPoliceEncounterRate());
        pirateLevelLbl.setText("Pirate Level: " + selectedPlanet.getPirateEncounterRate());
        traderLevelLbl.setText("Trader Level: " + selectedPlanet.getTraderEncounterRate());
        fuelLbl.setText("Fuel: " + game.getPlayer().getShip().getFuel());
    }

    /**
     * Creates a dialog error box with the given message
     * 
     * @param msg
     *            The message for the error dialog to display
     * @param title
     *            The title for the error dialog to display
     */
    private void displayMessage(String msg, String title) {
        if (title.equals("Error")) {
            Dialogs.create().owner(goBtn.getScene().getWindow()).title(title).message(msg)
            .showError();
        } else {
            Dialogs.create().owner(goBtn.getScene().getWindow()).title(title).message(msg);
        }
        
    }
}