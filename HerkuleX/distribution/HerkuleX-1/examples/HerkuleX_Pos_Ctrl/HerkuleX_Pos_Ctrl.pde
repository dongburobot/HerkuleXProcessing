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

int motorID = 253;

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
  
  myHerkuleX.setLed(motorID, HerkuleX.LED_RED | HerkuleX.LED_GREEN | HerkuleX.LED_BLUE);
}

int targetPos = 0;

void draw() {
  targetPos += 50;
  
  if (targetPos < 50 || targetPos > 970) {
    targetPos = 40;
    return;
  }
  
  myHerkuleX.moveOne(motorID, targetPos, 200, HerkuleX.LED_BLUE);
  println(myHerkuleX.getPosition(motorID)); // It takes 30ms.
  
  delay(500);
}
