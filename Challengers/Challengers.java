import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
//import dev.robocode.*;
import dev.robocode.tankroyale.botapi.util.*;
import java.awt.*;
import java.awt.event.*;
//import java.awt.geom.Point2D;
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
        setBodyColor(Color.YELLOW_GREEN);
        setGunColor(Color.RED);
        setRadarColor(Color.YELLOW_GREEN);

        int zigzagAngle = 30;
        boolean zigzagRight = true;

        while (isRunning()) {
            // Adaptive zigzag angle
            int currentZigzagAngle = zigzagAngle;
            if (aggressiveZigzag) currentZigzagAngle = 45;
            if (evasiveZigzag) currentZigzagAngle = 60;

            // Randomized evasion
            if (Math.random() < 0.3) {
                turnRight(Math.random() * 60 - 30); // -30° to +30°
            }

            // Zigzag movement
            if (zigzagRight) {
                turnRight(currentZigzagAngle);
            } else {
                turnLeft(currentZigzagAngle);
            }
            zigzagRight = !zigzagRight;

            forward(60 + Math.random() * 40); // 60–100 units
            setTargetSpeed(8);

            // Radar sweep
            turnRadarRight(45); // Narrow sweep for faster lock

            // Predict next position
            double nextX = getX() + Math.sin(Math.toRadians(getDirection())) * 100 * moveDirection;
            double nextY = getY() + Math.cos(Math.toRadians(getDirection())) * 100 * moveDirection;

            // Wall avoidance
            if (nextX < 80 || nextX > getArenaWidth() - 80 ||
                    nextY < 80 || nextY > getArenaHeight() - 80) {
                turnRight(90);
                moveDirection *= -1;
            }

            // Corner escape
            if ((getX() < 120 && getY() < 120) ||
                    (getX() > getArenaWidth() - 120 && getY() < 120) ||
                    (getX() < 120 && getY() > getArenaHeight() - 120) ||
                    (getX() > getArenaWidth() - 120 && getY() > getArenaHeight() - 120)) {
                turnRight(135);
                back(100);
            }

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
        double distance = distanceTo(e.getX(), e.getY());

        // Dynamic firepower scaling
        double firepower;
        if (distance < 100 && getEnergy() > 50) {
            firepower = 2.0; // Max damage for close-range and high energy
        } else if (distance < 200 && getEnergy() > 30) {
            firepower = 1.5;
        } else if (distance < 300 && getEnergy() > 20) {
            firepower = 1;
        } else {
            firepower = 0.5; // Conservative shot for long range or low energy
        }

        // Radar lock
        double angleToEnemy = Math.toDegrees(Math.atan2(e.getX() - getX(), e.getY() - getY()));
        double radarTurn = normalRelativeAngleDegrees(angleToEnemy - getRadarDirection());
        turnRadarRight(radarTurn);

        // Fire only if gun is aligned and energy is sufficient
        double gunTurn = normalRelativeAngleDegrees(angleToEnemy - getGunDirection());
        if (Math.abs(gunTurn) < 10 && getEnergy() > firepower * 2) {
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


