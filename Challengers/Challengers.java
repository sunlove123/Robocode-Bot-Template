import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.*;

public class Challengers extends Bot {

    private int moveDirection = 1;

    public static void main(String[] args) {
        new Challengers().start();
    }

    Challengers() {
        super(BotInfo.fromFile("Challengers.json"));
    }

    @Override
    public void run() {
        // Bot color setup
        setBodyColor(Color.YELLOW_GREEN);
        setGunColor(Color.RED);
        setRadarColor(Color.YELLOW_GREEN);

        while (isRunning()) {
            // Basic patrol movement
            forward(100 * moveDirection);
            turnGunRight(360); // Scan while moving
            turnRadarRight(360); // Spin radar to scan

            // Check if close to wall
            if (getX() < 100 || getX() > getArenaWidth() - 100 ||
                    getY() < 100 || getY() > getArenaHeight() - 100) {
                moveDirection *= -1; // Reverse direction
                back(100); // Move away from wall
            }
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Aim gun at enemy
        Point2D.Double enemyPos = new Point2D.Double(e.getX(),e.getY());
       // double angleToTarget = calcBearing(enemyPos);
      //  turnGunRight(normalRelativeAngleDegrees(angleToTarget - getGunDirection()));

        // Fire with power depending on distance
        double distance = distanceTo(e.getX(), e.getY());
        double firepower = distance < 150 ? 3.0 : 1.5;

        if (getEnergy() > 10) {
            fire(firepower);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Dodge: turn perpendicular to bullet
        double bulletDir = e.getBullet().getDirection();
        turnLeft(normalRelativeAngleDegrees(bulletDir - getDirection()) + 90);
        forward(50); // Move away
    }

    // Helper to calculate relative angle
    private double normalRelativeAngleDegrees(double angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}

