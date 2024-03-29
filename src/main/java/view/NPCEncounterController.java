package view;

import controller.GameEngine;

import model.NPCEncounter;
import model.Player;
import model.Ship;
import model.Trader;

import model.enums.EncounterResult;
import model.enums.EncounterType;

import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.util.Optional;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the encounter screen with NPCs
 * 
 * @author Jack Croft
 *
 */
public class NPCEncounterController implements Controller {
    private TravelScreenController parent;
    private NPCEncounter encounter;

    @FXML
    private Label playerShipLbl;

    @FXML
    private Label playerHPLbl;

    @FXML
    private Label playerShieldsLbl;

    @FXML
    private Label NPCShipLbl;

    @FXML
    private Label NPCHPLbl;

    @FXML
    private Label NPCShieldsLbl;

    @FXML
    private Button attackBtn;

    @FXML
    private Button fleeLeaveBtn;

    @FXML
    private Button surrenderConsentTradeBtn;

    @FXML
    private Button bribeBtn;

    /**
     * Initializes the page with all the proper encounter data
     * 
     * @param tsc
     *            The controller that created the popup
     * @param e
     *            The NPCEncounter that is happening
     */
    public void initializePage(TravelScreenController tsc, NPCEncounter encounter) {
        this.encounter = encounter;
        parent = tsc;
        Player player = GameEngine.getGameEngine().getPlayer();
        Ship ship = player.getShip();
        Ship NPCShip = encounter.getNPC().getShip();
        playerShipLbl.setText("Ship Type: " + ship.getShipType().toString());
        playerHPLbl.setText("HP: " + ship.getCurrHp());
        playerShieldsLbl.setText("Shields: " + ship.getCurrShieldHp());
        NPCShipLbl.setText("Ship Type: " + NPCShip.getShipType().toString());
        NPCHPLbl.setText("HP: " + NPCShip.getCurrHp());
        NPCShieldsLbl.setText("Shields: " + NPCShip.getCurrShieldHp());
        if (encounter.getEncounterType() == EncounterType.PIRATE) {
            bribeBtn.setVisible(false);
        } else if (encounter.getEncounterType() == EncounterType.POLICE) {
            surrenderConsentTradeBtn.setText("Consent To Search");
        } else {
            surrenderConsentTradeBtn.setText("Trade");
            fleeLeaveBtn.setText("Leave");
            bribeBtn.setVisible(false);
        }
    }

    /**
     * Updates the labels actively involved in a battle
     */
    private void updateShipLabels() {
        Player player = GameEngine.getGameEngine().getPlayer();
        Ship ship = player.getShip();
        playerHPLbl.setText("HP: " + Integer.toString(ship.getCurrHp()));
        playerShieldsLbl.setText("Shields: " + Integer.toString(ship.getCurrShieldHp()));
        NPCHPLbl.setText("HP: " + encounter.getNPC().getShip().getCurrHp());
        NPCShieldsLbl.setText("Shields: " + encounter.getNPC().getShip().getCurrShieldHp());
    }

    /**
     * Method for executing a player attack
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void attack(Event event) {
        if (MultiPageController.isValidAction(event)) {
            EncounterResult result = GameEngine.getGameEngine().playerAttack(encounter);
            String playerMsg = "";
            boolean encounterComplete = false;
            switch (result) {
            case NPCFLEESUCCESS: {
                playerMsg = "The " + encounter.getNPC().toString()
                        + " successfully fled the battle";
                encounterComplete = true;
                break;
            }
            case NPCFLEEFAIL: {
                playerMsg = "The " + encounter.getNPC().toString()
                        + " tried to flee the battle, but failed";
                break;
            }
            case NPCATTACK: {
                playerMsg = "The " + encounter.getNPC().toString() + " attacked you";
                break;
            }
            case NPCSURRENDER: {
                playerMsg = "The " + encounter.getNPC().toString()
                        + " has surrendered to you. You aquire his cargo";
                encounterComplete = true;
                break;
            }
            case NPCDEATH: {
                playerMsg = "The " + encounter.getNPC().toString() + " has died";
                encounterComplete = true;
                break;
            }
            case PLAYERDEATH: {
                playerMsg = "You have died";
                Dialogs.create().owner(attackBtn.getScene().getWindow()).title("Death")
                        .message(playerMsg).showInformation();
                System.exit(0);
                break;
            }
            default: {
                break;
            }
            }

            Dialogs.create().owner(attackBtn.getScene().getWindow()).title("Result")
                    .message(playerMsg).showInformation();
            updateShipLabels();
            if (encounterComplete) {
                Stage popupStage = (Stage) attackBtn.getScene().getWindow();
                popupStage.close();
                parent.updatePage();
            }
        }
    }

    /**
     * Method for executing a player attempt to flee or leave
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void fleeLeave(Event event) {
        if (MultiPageController.isValidAction(event)) {
            boolean encounterComplete = false;
            String playerMsg = "";
            if (encounter.getEncounterType() != EncounterType.TRADER
                    || (encounter.getEncounterType() == EncounterType.TRADER && encounter
                            .getTurnCount() != 0)) {
                EncounterResult result = GameEngine.getGameEngine().playerFlee(encounter);
                switch (result) {
                case PLAYERFLEESUCCESS: {
                    playerMsg = "You have successfully fled the fight";
                    encounterComplete = true;
                    break;
                }
                case NPCFLEESUCCESS: {
                    playerMsg = "The " + encounter.getNPC().toString()
                            + " successfully fled the battle";
                    encounterComplete = true;
                    break;
                }
                case NPCFLEEFAIL: {
                    playerMsg = "The " + encounter.getNPC().toString()
                            + " tried to flee the battle, but failed";
                    break;
                }
                case NPCATTACK: {
                    playerMsg = "The " + encounter.getNPC().toString() + " attacked you";
                    break;
                }
                case NPCSURRENDER: {
                    playerMsg = "The " + encounter.getNPC().toString()
                            + " has surrendered to you. You aquire his cargo";
                    encounterComplete = true;
                    break;
                }
                case NPCDEATH: {
                    playerMsg = "The " + encounter.getNPC().toString() + " has died";
                    encounterComplete = true;
                    break;
                }
                case PLAYERDEATH: {
                    playerMsg = "You have died";
                    Dialogs.create().owner(attackBtn.getScene().getWindow()).title("Death")
                            .message(playerMsg).showInformation();
                    System.exit(0);
                    break;
                }
                default: {
                    break;
                }
                }
            } else {
                encounterComplete = true;
                playerMsg = "You leave the Trader and continue on";
            }
            updateShipLabels();
            Dialogs.create().owner(fleeLeaveBtn.getScene().getWindow()).title("Result")
                    .message(playerMsg).showInformation();

            if (encounterComplete) {
                Stage popupStage = (Stage) fleeLeaveBtn.getScene().getWindow();
                popupStage.close();
                parent.updatePage();
            }
        }
    }

    /**
     * Method for executing a player surrender, consent to search, or trade
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void surrenderConsentTrade(Event event) {
        if (MultiPageController.isValidAction(event)) {
            GameEngine game = GameEngine.getGameEngine();
            if (encounter.getEncounterType() == EncounterType.PIRATE) {
                game.surrenderToNPC(encounter);
                Dialogs.create().owner(bribeBtn.getScene().getWindow()).title("Surrender")
                        .message("You surrender and lose all your cargo").showInformation();
                Stage popupStage = (Stage) surrenderConsentTradeBtn.getScene().getWindow();
                popupStage.close();
                parent.updatePage();
            } else if (encounter.getEncounterType() == EncounterType.POLICE) {
                boolean policeSuccess = game.consentToSearch(encounter);
                String msg = "";
                if (policeSuccess) {
                    msg = "The police found and confiscated all your illegal cargo";
                } else {
                    msg = "The police did not find any illegal cargo and leave your ship";
                }
                Dialogs.create().owner(bribeBtn.getScene().getWindow()).title("Police Search")
                        .message(msg).showInformation();
                Stage popupStage = (Stage) surrenderConsentTradeBtn.getScene().getWindow();
                popupStage.close();
                parent.updatePage();
            } else {
                Trader trader = (Trader) (encounter.getNPC());
                try {
                    Stage tradePopup = new Stage();
                    tradePopup.initModality(Modality.APPLICATION_MODAL);
                    tradePopup.initOwner((Stage) surrenderConsentTradeBtn.getScene().getWindow());

                    FXMLLoader loader = new FXMLLoader(
                            ClassLoader.getSystemResource("view/TradeGoodPopup.fxml"));
                    Parent newScene = loader.load();
                    tradePopup.setScene(new Scene(newScene, 300, 125));

                    TradeGoodPopupController controller = loader.getController();
                    int maxGood = game.getMaximumTraderTradeAmount(encounter);
                    controller.initializePage(trader.getGoodOfInterest(), this, maxGood,
                            !trader.isBuying(), false);
                    tradePopup.show();
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    /**
     * Method for executing a player bribe
     * 
     * @param e
     *            The event that fired the method
     */
    @FXML
    private void bribe(Event event) {
        if (MultiPageController.isValidAction(event)) {
            GameEngine game = GameEngine.getGameEngine();
            boolean done = false;
            while (!done) {
                Optional<String> bribe = Dialogs.create().owner(bribeBtn.getScene().getWindow())
                        .title("Bribe").message("Please input your bribe amount").showTextInput();
                if (bribe.isPresent()) {
                    int bribeAmt;
                    try {
                        bribeAmt = Integer.valueOf(bribe.get());
                        if (bribeAmt > 0 && bribeAmt <= game.getPlayer().getCredits()) {
                            if (game.bribePolice(encounter, bribeAmt)) {
                                Dialogs.create().owner(bribeBtn.getScene().getWindow())
                                        .title("Success")
                                        .message("The police accept your bribe and leave")
                                        .showInformation();

                                Stage popupStage = (Stage) bribeBtn.getScene().getWindow();
                                popupStage.close();
                                parent.updatePage();
                            } else {
                                Dialogs.create().owner(bribeBtn.getScene().getWindow())
                                        .title("Failure").message("The police refuse your bribe")
                                        .showInformation();
                                bribeBtn.setDisable(true);
                            }
                        } else {
                            Dialogs.create()
                                    .owner(bribeBtn.getScene().getWindow())
                                    .title("Error")
                                    .message(
                                            "You must enter a positive number less"
                                            + " than your total credits")
                                    .showError();
                        }
                    } catch (NumberFormatException n) {
                        Dialogs.create().owner(bribeBtn.getScene().getWindow()).title("Error")
                                .message("You must enter a valid number").showError();
                    }
                } else {
                    done = true;
                }
            }
        }
    }

    @Override
    public void updatePage() {
        if (encounter.getEncounterType() == EncounterType.TRADER) {
            Dialogs.create().owner(bribeBtn.getScene().getWindow()).title("Success")
                    .message("The trader thanks you for your trade and leaves").showInformation();

            Stage popupStage = (Stage) bribeBtn.getScene().getWindow();
            popupStage.close();
            parent.updatePage();
        }
    }

    /**
     * Gets the NPCEncounter associated with this controller
     * 
     * @return The NPCEncounter associated with this controller
     */
    public NPCEncounter getEncounter() {
        return encounter;
    }
}