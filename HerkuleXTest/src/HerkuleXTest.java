import processing.core.PApplet;
import processing.serial.*;

public class HerkuleXTest extends PApplet {

	private Serial myPort;
	private HerkuleX myHerkuleX;
	
	private int targetPos = 0;
	private int motorId = 0;
	
	public void setup() {
		size(256, 256);
		noStroke();
		
		println(Serial.list());
		String portName = Serial.list()[0];
		
		myPort = new Serial(this, portName, 115200);
		myHerkuleX = new HerkuleX(myPort);
		myHerkuleX.initialize();
		//myHerkuleX.setLed(motorId, HerkuleX.LED_BLUE | HerkuleX.LED_RED | HerkuleX.LED_GREEN);
		//myHerkuleX.moveOneAngle(motorId, -150.0f, 200, 0);
//		myHerkuleX.moveSpeedOne(motorId, -512, 200, 0);
//		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		println(myHerkuleX.getSpeed(motorId));
		
//		myHerkuleX.moveSpeedAll(0, -256, HerkuleX.LED_BLUE);
//		myHerkuleX.moveSpeedAll(1, -789, HerkuleX.LED_RED);
//		myHerkuleX.actionAll(100);
		
//		myHerkuleX.moveAll(0, 235, HerkuleX.LED_BLUE);
//		myHerkuleX.moveAll(1, 789, HerkuleX.LED_GREEN);
//		myHerkuleX.actionAll(300);
		
//		myHerkuleX.moveAllAngle(0, 90.0f, HerkuleX.LED_BLUE);
//		myHerkuleX.moveAllAngle(1, -90.0f, HerkuleX.LED_RED);
//		myHerkuleX.actionAll(700);
	}
	
	public void draw() {
		targetPos += 50;
		if (targetPos < 50 || targetPos > 970) {
			targetPos = 40;
			return;
		}
		
		myHerkuleX.moveOne(motorId, targetPos, 100, HerkuleX.LED_RED | HerkuleX.LED_GREEN);
		println(myHerkuleX.getPosition(motorId));	// It takes 20ms. See. WAIT_TIME_BY_ACK in HerkuleX class
		//println(myHerkuleX.getAngle(motorId));	// It takes 20ms
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
