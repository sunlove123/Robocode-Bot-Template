import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
//import dev.robocode.tankroyale.botapi.util.Ut


public class Challengers extends Bot {

    // The main method starts our bot
    public static void main(String[] args)
    {
        new Challengers().start();
    }

    // Constructor, which loads the bot config file
    Challengers() {
        super(BotInfo.fromFile("Challengers.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        setBodyColor(Color.YELLOW_GREEN);
        setGunColor(Color.RED);
        setRadarColor(Color.YELLOW_GREEN);

        setAdjustRadarForBodyTurn(true);
        setAdjustRadarForGunTurn(true);

        // spin Radar
       // while (true) {
         //   setTurnRadarRight(360);
        //}

        // Repeat while the bot is running
        while (isRunning()) {
            forward(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
            setTurnRadarRight(360);
        }
    }

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
       // double angle = Utils.normalRelativeAngleDegrees(e.getBearing() + 90);
      //  if (e.getD)
        fire(0);


    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        var bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnLeft(90 - bearing);
    }
}

