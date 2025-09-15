import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
//import dev.robocode.*;
import dev.robocode.tankroyale.botapi.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.*;
import dev.robocode.tankroyale.botapi.events.HitWallEvent;

public class Challengers extends Bot {

    private int moveDirection = 1;
    private boolean aggressiveZigzag = false;
    private boolean evasiveZigzag = false;

    public static void main(String[] args) {
        new Challengers().start();
    }

    Challengers() {
        super(BotInfo.fromFile("Challengers.json"));
    }

    @Override
    public void run() {
        setBodyColor(Color.BLACK);
        setGunColor(Color.BLACK);
        setRadarColor(Color.BLACK);

        int zigzagAngle = 30;
        boolean zigzagRight = true;
        boolean aggressiveZigzag = false;
        boolean evasiveZigzag = false;

        while (isRunning()) {
            // Adjust zigzag based on threat level
            int currentZigzagAngle = zigzagAngle;
            if (aggressiveZigzag) currentZigzagAngle = 45;
            if (evasiveZigzag) currentZigzagAngle = 60;

            // Zigzag movement
            if (zigzagRight) {
                turnRight(currentZigzagAngle);
            } else {
                turnLeft(currentZigzagAngle);
            }
            zigzagRight = !zigzagRight;

            forward(100 * moveDirection);
            setTargetSpeed(8);
            turnGunRight(360);
            turnRadarRight(360);

            // Wall avoidance
            // Predict next position
            double nextX = getX() + Math.sin(Math.toRadians(getDirection())) * 100 * moveDirection;
            double nextY = getY() + Math.cos(Math.toRadians(getDirection())) * 100 * moveDirection;

// If next move risks hitting a wall, adjust heading
            if (nextX < 80 || nextX > getArenaWidth() - 80 ||
                    nextY < 80 || nextY > getArenaHeight() - 80) {
                turnRight(90); // Turn away from wall
                moveDirection *= -1; // Reverse movement direction
            }

            //if (getX() < 100 || getX() > getArenaWidth() - 100 ||
              //      getY() < 100 || getY() > getArenaHeight() - 100) {
                //moveDirection *= -1;
                //back(100);
            //}

            // Reset adaptive flags
            aggressiveZigzag = false;
            evasiveZigzag = false;
        }
    }

    public void onHitWall(HitWallEvent e) {
        // Reverse direction and back off
        moveDirection *= -1;
        back(150); // Retreat from wall
        turnRight(90); // Turn away from wall
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        Point2D.Double enemyPos = new Point2D.Double(e.getX(), e.getY());
        double distance = distanceTo(e.getX(), e.getY());
        double firepower = distance < 150 ? 3.0 : 1.5;

        if (getEnergy() > 10) {
            fire(firepower);
        }

        // Trigger aggressive zigzag if enemy is close
        if (distance < 200) {
            aggressiveZigzag = true;
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        double bulletDir = e.getBullet().getDirection();
        turnLeft(normalRelativeAngleDegrees(bulletDir - getDirection()) + 90);
        forward(30);
        // Trigger evasive zigzag when hit
        evasiveZigzag = true;
    }

    // Helper to calculate relative angle
    private double normalRelativeAngleDegrees(double angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
