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

void draw() {
  myHerkuleX.moveSpeedOne(motor0_ID, -512, 1, HerkuleX.LED_BLUE);  // Infinite Turn CW -512
  myHerkuleX.moveSpeedOne(motor1_ID, 512, 1, HerkuleX.LED_BLUE);  // Infinite Turn CCW +512
  println("Motor0 PWM (-1023~1023): " + myHerkuleX.getSpeed(motor0_ID));
  println("Motor1 PWM (-1023~1023): " + myHerkuleX.getSpeed(motor1_ID));
  delay(3000);   // Run 3s
  myHerkuleX.moveSpeedOne(motor0_ID, 512, 1, 0);  // Infinite Turn CCW +512
  myHerkuleX.moveSpeedOne(motor1_ID, -512, 1, 0);  // Infinite Turn CW -512
  println("Motor0 PWM (-1023~1023): " + myHerkuleX.getSpeed(motor0_ID));
  println("Motor1 PWM (-1023~1023): " + myHerkuleX.getSpeed(motor1_ID));
  delay(3000);   // Run 3s  
}
