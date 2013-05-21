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
  
  int current_ID = 253;  // (0 ~ 253)
  int new_ID = 0;    // (0 ~ 253)
  
  // If there are duplicated servo IDs on your Serial Line,It does not work. 
  // When you change your servo's ID, make sure there is only the servo on the line if possible.
  if (myHerkuleX.set_ID(current_ID, new_ID)) {  
    println("Servo ID " + current_ID + " has been changed to " + new_ID);
  } else {
    println("Servo ID has not been changed");
  }
}

void draw() {
}
