import processing.serial.*;
import dongburobot.herkulex.*;

Serial myPort;
HerkuleX myHerkuleX;

void setup() {
  size(256, 256);
  noStroke();
		
  println(Serial.list());
  String portName = Serial.list()[0];
		
  myPort = new Serial(this, portName, 115200);
  myHerkuleX = new HerkuleX(myPort);
  myHerkuleX.initialize();
  
  myHerkuleX.moveOne(0, 256, 100, HerkuleX.LED_RED | HerkuleX.LED_GREEN);
  //myHerkuleX.moveSpeedOne(0, -500, 60, 0);
}

void draw() {
}
