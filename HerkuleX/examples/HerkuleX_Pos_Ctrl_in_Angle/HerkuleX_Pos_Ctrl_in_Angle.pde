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

int motorID = 0;

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

float targetAng = -160.0f;

void draw() {
  targetAng += 3.25f;
  
  if (targetAng > 160.0f) {
    targetAng = -160.0f;
    return;
  }
  
  myHerkuleX.moveOneAngle(motorID, targetAng, 50, HerkuleX.LED_GREEN);
  println(myHerkuleX.getAngle(motorID)); // It takes 30ms.
  
  delay(100);
}
