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

ArrayList<Integer> servoIDs;

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
  
  println("Scanning... It takes 8 seconds. Wait please.");
  servoIDs = myHerkuleX.performIDScan();  // 30ms * 254 = 7.62s
  println("Done.");
  println("Num of servos: " + servoIDs.size());
  
  for (int i = 0; i < servoIDs.size(); i++) 
    println("ID: " + servoIDs.get(i));
}

void draw() {
}
