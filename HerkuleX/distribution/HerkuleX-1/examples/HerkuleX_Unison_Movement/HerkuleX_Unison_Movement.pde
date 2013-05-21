import processing.serial.*;
import dongburobot.herkulex.*;

/*
  How to wire up between your PC and HerkuleX servos:
    see. http://www.hovis.co.kr/guide/herkulexeng.pdf
  
  To set up HerkuleX servo parameters (i.e. PID Gain, Pos Limit, Torque and Error Policy, and  etc.)
  use HerkuleX Manager S/W (Windows only). Download link: http://hovis.co.kr/guide/engpc_thum.htm
*/

Serial myPort;
HerkuleX myHerkuleX;

void setup() {
  size(256, 256);
  noStroke();
		
  println(Serial.list());
  String portName = Serial.list()[0];
		
  myPort = new Serial(this, 
                portName /* your USB2Serial Port ex)"COM1" */, 
                115200 /* The default baudrate of HerkuleX servo is 115200 */); 
                
  myHerkuleX = new HerkuleX(myPort);
  myHerkuleX.initialize();
}

int motor0_ID = 0;
int motor1_ID = 1;
int motor2_ID = 2;

void draw() {
  // In unison movement, actionAll() only care about first 50 servo motors.
  // If you want to control over than 50 motors, 
  // do like this 
  // addSth(), ..., addSth() <-- 50th, actionAll(playtime);
  // addSth(), ..., addSth() <-- 100th, actionAll(playtime);
  myHerkuleX.addMove(motor0_ID, 789, HerkuleX.LED_BLUE | HerkuleX.LED_GREEN);
  myHerkuleX.addAngle(motor1_ID, -90.0f, 0);
  myHerkuleX.addSpeed(motor2_ID, -512, HerkuleX.LED_RED);
  myHerkuleX.actionAll(1000);  
  delay(3000);
  
  myHerkuleX.addMove(motor2_ID, 235, HerkuleX.LED_BLUE | HerkuleX.LED_GREEN);
  myHerkuleX.addAngle(motor1_ID, 90.0f, 0);
  myHerkuleX.addSpeed(motor0_ID, 512, HerkuleX.LED_RED);
  myHerkuleX.actionAll(1000);
  delay(3000);
}
