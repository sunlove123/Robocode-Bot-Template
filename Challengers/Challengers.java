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
    private double lastEnemyEnergy = 100; // Track enemy energy for bullet dodging
    private long lastFireTime = 0; // Prevent rapid firing
    private static final int FIRE_COOLDOWN = 3; // Minimum ticks between shots
    private boolean emergencyEvasion = false;
    private int evasionCounter = 0;
    private int stuckCounter=0;

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

            // Enhanced wall avoidance with better prediction
            double nextX = getX() + Math.sin(Math.toRadians(getDirection())) * 120 * moveDirection;
            double nextY = getY() + Math.cos(Math.toRadians(getDirection())) * 120 * moveDirection;

            // Improved wall detection with larger safety margin
            if (nextX < 100 || nextX > getArenaWidth() - 100 ||
                    nextY < 100 || nextY > getArenaHeight() - 100) {
                turnRight(90); // Turn away from wall
                moveDirection *= -1; // Reverse movement direction
            }

            // Reset adaptive flags
            aggressiveZigzag = false;
            evasiveZigzag = false;
        }
    }

    public void onHitWall(HitWallEvent e) {
        // Emergency wall escape maneuver
        emergencyEvasion = true;
        evasionCounter = 0;

        // Reverse direction and back off with randomness
        moveDirection *= -1;
        back(200); // Move further back from wall

        // Sharp turn away from wall with randomness
        double turnAngle = 90 + (Math.random() * 90); // Turn between 90-180 degrees
        turnRight(turnAngle);

        // Reset stuck counter
        stuckCounter = 0;
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        Point2D.Double enemyPos = new Point2D.Double(e.getX(), e.getY());
        double distance = calcDistance(e.getX(), e.getY(), getX(), getY());

        // Improved targeting with basic lead calculation
        double enemyHeading = e.getDirection();

        double enemyVelocity = 100;

        // Simple predictive targeting
        double bulletSpeed = 20 - (3 * calculateOptimalFirepower(distance));
        double timeToTarget = distance / bulletSpeed;

        // Predict enemy future position
        double futureX = e.getX() + Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;
        double futureY = e.getY() + Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToTarget;

        // Calculate angle to future position
        double angleToFuture = Math.toDegrees(Math.atan2(futureX - getX(), futureY - getY()));
        double gunTurnNeeded = normalRelativeAngleDegrees(angleToFuture - getGunDirection());

        // Turn gun towards predicted position
        turnGunRight(gunTurnNeeded);

        // Smart firing with energy management and cooldown
        double firepower = calculateOptimalFirepower(distance);
        long currentTime = getTimeLeft();

                if (Math.abs(gunTurnNeeded) < 15 &&
                getEnergy() > firepower * 3 &&
                currentTime - lastFireTime >= FIRE_COOLDOWN &&
                shouldFireAtEnemy(distance));
{ // Smart firing logic

            fire(firepower);
            lastFireTime = currentTime;
        }

        // Enhanced movement adaptation
        if (distance < 200) {
            aggressiveZigzag = true;
        }

        // Detect if enemy fired (energy drop) and trigger evasion
        double energyDrop = lastEnemyEnergy - e.getEnergy();
        if (energyDrop >= 0.1 && energyDrop <= 3.0) {
            // Enemy likely fired, trigger evasive maneuvers
            evasiveZigzag = true;
            // Change direction unpredictably
            if (Math.random() > 0.5) {
                moveDirection *= -1;
            }
        }
        lastEnemyEnergy = e.getEnergy();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        double bulletDir = e.getBullet().getDirection();

        // More sophisticated evasion - perpendicular movement
        double perpendicularAngle = bulletDir + 90;
        if (Math.random() > 0.5) {
            perpendicularAngle = bulletDir - 90;
        }

        turnRight(normalRelativeAngleDegrees(perpendicularAngle - getDirection()));
        forward(50);

        // Trigger evasive zigzag when hit
        evasiveZigzag = true;
    }

    /**
     * Calculate distance between two points
     */
    private double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Calculate optimal firepower based on distance and energy considerations
     */
    private double calculateOptimalFirepower(double distance) {
        if (distance < 100) {
            return getEnergy() > 50 ? 3.0 : 2.0; // High power for close targets
        } else if (distance < 300) {
            return getEnergy() > 30 ? 2.0 : 1.5; // Medium power for medium range
        } else {
            return getEnergy() > 20 ? 1.5 : 1.0; // Low power for distant targets
        }
    }

    /**
     * Determine if we should fire at the enemy based on various factors
     */
    private boolean shouldFireAtEnemy(double distance) {
        // Don't fire if enemy is too far (likely to miss)
        if (distance > 400) {
            return false;
        }

        // Don't fire if we're low on energy and enemy is far
        if (getEnergy() < 20 && distance > 200) {
            return false;
        }

        return true;
    }

    // Helper to calculate relative angle
    private double normalRelativeAngleDegrees(double angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
