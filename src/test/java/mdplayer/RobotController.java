package mdplayer;

import java.awt.Robot;
import java.awt.event.InputEvent;

public class RobotController {
    static void main(String[] args) {
        try {
            long pid = Long.parseLong(args[0]);
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);

            System.out.println("RobotController starting: pid=" + pid + ", x=" + x + ", y=" + y);

            System.out.println("Using java.awt.Robot to click close button...");
            Robot robot = new Robot();
            robot.setAutoDelay(100);
            
            // Move to close button (macOS top-left of window frame)
            int clickX = x + 15;
            int clickY = y + 15;
            
            System.out.println("Robot moving to: " + clickX + ", " + clickY);
            robot.mouseMove(clickX, clickY);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            System.out.println("Robot click dispatched.");

            // Wait a bit to ensure event is processed
            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
