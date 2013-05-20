import java.util.ArrayList;

import processing.serial.*;

public class HerkuleX {
	
	private final int BASIC_PKT_SIZE 	= 7;
	private final int WAIT_TIME_BY_ACK 	= 20;
	
	// HERKULEX Broadcast Servo ID
	public final byte BROADCAST_ID = (byte) 0xFE;
		
	// SERVO HERKULEX COMMAND - See Manual p40
	private final byte HEEPWRITE = 	0x01; 		//Rom write
	private final byte HEEPREAD  = 	0x02; 		//Rom read
	private final byte HRAMWRITE = 	0x03; 		//Ram write
	private final byte HRAMREAD  = 	0x04; 		//Ram read
	private final byte HIJOG	 =	0x05; 		//Write n servo with different timing
	private final byte HSJOG	 =	0x06; 		//Write n servo with same time
	private final byte HSTAT	 = 	0x07; 		//Read error
	private final byte HROLLBACK =	0x08; 		//Back to factory value
	private final byte HREBOOT   =	0x09; 		//Reboot

	// HERKULEX LED - See Manual p29
	public static final byte LED_RED   = 	0x10;
	public static final byte LED_GREEN =	0x04;
	public static final byte LED_BLUE  =   	0x08;

	// HERKULEX STATUS ERROR - See Manual p39
	public static final byte H_STATUS_OK				= 0x00;
	public static final byte H_ERROR_INPUT_VOLTAGE 		= 0x01;
	public static final byte H_ERROR_POS_LIMIT			= 0x02;
	public static final byte H_ERROR_TEMPERATURE_LIMIT	= 0x04;
	public static final byte H_ERROR_INVALID_PKT		= 0x08;
	public static final byte H_ERROR_OVERLOAD			= 0x10;
	public static final byte H_ERROR_DRIVER_FAULT  		= 0x20;
	public static final byte H_ERROR_EEPREG_DISTORT		= 0x40;
	
	private Serial mPort;
	
	private ArrayList<Byte> multipleMoveData;
	
	public HerkuleX(Serial port) {
		mPort = port;
		multipleMoveData = new ArrayList<>();
	}
	
	// initialize servos
	public void initialize() {
		try {
			Thread.sleep(100);
			clearError(BROADCAST_ID);	// clear error for all servos
		    Thread.sleep(10);
		    setAckPolicy(1);			// set ACK policy
		    Thread.sleep(10);
		    torqueON(BROADCAST_ID);		// torqueON for all servos
		    Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// ACK  - 0=No Replay, 1=Only reply to READ CMD, 2=Always reply
	public void setAckPolicy(int valueACK) {
		byte[] optData = new byte[3];
		optData[0] = 0x34;             	// Address
		optData[1] = 0x01;             	// Length
		optData[2] = (byte) valueACK;   // Value. 0=No Replay, 1=Only reply to READ CMD, 2=Always reply
		
		byte[] packetBuf = buildPacket((byte) 0xFE, HRAMWRITE, optData);
		sendData(packetBuf);
	}
	
	// clearError
	public void clearError(byte servoID) {
		byte[] optData = new byte[4];
		optData[0] = 0x30;               // Address
		optData[1] = 0x02;               // Length
		optData[2] = 0x00;               // Write error=0
		optData[3] = 0x00;               // Write detail error=0
		
		byte[] packetBuf = buildPacket(servoID, HRAMWRITE, optData);
		sendData(packetBuf);
	}
	
	// torque on - 
	public void torqueON(byte servoID) {
		byte[] optData = new byte[3];
		optData[0] = 0x34;               	// Address
		optData[1] = 0x01;              	// Length
		optData[2] = 0x60;               	// 0x60=Torque ON
		
		byte[] packetBuf = buildPacket(servoID, HRAMWRITE, optData);
		sendData(packetBuf);
	}
	
	public void torqueOFF(byte servoID) {
		byte[] optData = new byte[3];
		optData[0] = 0x34;               	// Address
		optData[1] = 0x01;              	// Length
		optData[2] = 0x00;               	// 0x60=Torque ON
		
		byte[] packetBuf = buildPacket(servoID, HRAMWRITE, optData);
		sendData(packetBuf);
	}
	
	// move one servo with continous rotation
	public void moveSpeedOne(int servoID, int goalSpeed, int playTime, int led)
	{
		if (goalSpeed > 1023 || goalSpeed < -1023) return;       // speed (goal) non correct
		if ((playTime <0) || (playTime > 2856)) return;
		
		int goalSpeedSign;
		if (goalSpeed < 0) {
			goalSpeedSign = (-1) * goalSpeed;
			goalSpeedSign |= 0x4000; 
		} 
		else {
			goalSpeedSign = goalSpeed;
		}

		int speedGoalLSB = goalSpeedSign & 0X00FF; 		       // MSB speedGoal 
		int speedGoalMSB = (goalSpeedSign & 0xFF00) >> 8;      // LSB speedGoal 

		playTime = (int) (playTime / 11.2f);		// ms --> value
		led = (byte) (led | 0x02);					// Speed Ctrl Mode
		
		byte[] optData = new byte[5];
		optData[0] = (byte) playTime;  		// Execution time	
		optData[1] = (byte) speedGoalLSB;
		optData[2] = (byte) speedGoalMSB;
		optData[3] = (byte) led;
		optData[4] = (byte) servoID;
		
		byte[] packetBuf = buildPacket((byte)servoID, HSJOG, optData);
		sendData(packetBuf);
	}
	
	// get speed
	public int getSpeed(int servoID) {
		byte[] optData = new byte[2];
		optData[0] = 0x40;               	// Address
		optData[1] = 0x02;              	// Length
		
		byte[] packetBuf = buildPacket((byte)servoID, HRAMREAD, optData);
		sendData(packetBuf);

	    try {
			Thread.sleep(WAIT_TIME_BY_ACK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	    byte[] readBuf = readData();
	    
	    if (!isRightPacket(readBuf)) {
	    	return -1;
	    }
	   
	    int speedy = ((readBuf[10] & 0x03) << 8) | (readBuf[9] & 0xFF);
	    
	    if ((readBuf[10] & 0x40) == 0x40)
	    	speedy *= -1;
	    
	    return speedy;
	}
	
	// move one servo at goal position 0 - 1023
	public void moveOne(int servoID, int goalPos, int playTime, int led)
	{
		if (goalPos > 1023 || goalPos < 0) return;       // speed (goal) non correct
		if ((playTime <0) || (playTime > 2856)) return;
		
		// Position definition
		int posLSB = goalPos & 0X00FF;			// MSB Pos
		int posMSB = (goalPos & 0XFF00) >> 8;	// LSB Pos
		playTime = (int) (playTime / 11.2f);	// ms --> value
		led = (byte) (led & 0xFD);				// Pos Ctrl Mode
		
		byte[] optData = new byte[5];
		optData[0] = (byte) playTime;  		// Execution time	
		optData[1] = (byte) posLSB;
		optData[2] = (byte) posMSB;
		optData[3] = (byte) led;
		optData[4] = (byte) servoID;
		
		byte[] packetBuf = buildPacket((byte)servoID, HSJOG, optData);
		sendData(packetBuf);
	}
	
	// get Position
	public int getPosition(int servoID) {
		byte[] optData = new byte[2];
		optData[0] = 0x3A;               	// Address
		optData[1] = 0x02;              	// Length
		
		byte[] packetBuf = buildPacket((byte)servoID, HRAMREAD, optData);
		sendData(packetBuf);

	    try {
			Thread.sleep(WAIT_TIME_BY_ACK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	    byte[] readBuf = readData();
	    
	    if (!isRightPacket(readBuf)) {
	    	return -1;
	    }
	   
		int pos = ((readBuf[10] & 0x03) << 8) | (readBuf[9] & 0xFF);
	    return pos;
	}

	// move one servo to an angle between -160 and 160
	public void moveOneAngle(int servoID, float angle, int pTime, int iLed) {
		if (angle > 167.0|| angle < -167.0) return;	
		int position = (int)(angle/0.325) + 512;
		moveOne(servoID, position, pTime, iLed);
	}

	public float getAngle(int servoID) {
		int pos = (int)getPosition(servoID);
		return (pos-512) * 0.325f;
	}
	
	// move all servo at the same time to a position: servo list building
	public void moveAll(int servoID, int goal, int led)
	{
		if (goal > 1023 || goal < 0)
			return;						 //0 <--> 1023 range

		// Position definition
		int posLSB = goal & 0X00FF;			// MSB Pos
		int posMSB = (goal & 0XFF00) >> 8;	// LSB Pos
		led = (byte) (led & 0xFD);			// Pos Ctrl Mode
			
		byte[] optData = new byte[4];
		optData[0] = (byte) posLSB;
		optData[1] = (byte) posMSB;
		optData[2] = (byte) led;
		optData[3] = (byte) servoID;

		addData(optData);	//add servo data to list, pos mode
	}
	
	// move all servo at the same time to a position: servo list building
	public void moveAllAngle(int servoID, float angle, int led)
	{
		if (angle > 167.0|| angle < -167.0) return; // out of the range	
		int position = (int)(angle/0.325) + 512;
		moveAll(servoID, position, led);
	}
	
	// move all servo at the same time with different speeds: servo list building
	public void moveSpeedAll(int servoID, int goalSpeed, int led)
	{
		if (goalSpeed > 1023 || goalSpeed < -1023) return;       // speed (goal) non correct
		
		int goalSpeedSign;
		if (goalSpeed < 0) {
			goalSpeedSign = (-1) * goalSpeed;
			goalSpeedSign |= 0x4000; 
		} 
		else {
			goalSpeedSign = goalSpeed;
		}

		int speedGoalLSB = goalSpeedSign & 0X00FF; 		       // MSB speedGoal 
		int speedGoalMSB = (goalSpeedSign & 0xFF00) >> 8;      // LSB speedGoal 

		led = (byte) (led | 0x02);					// Speed Ctrl Mode
		
		byte[] optData = new byte[4];
		optData[0] = (byte) speedGoalLSB;
		optData[1] = (byte) speedGoalMSB;
		optData[2] = (byte) led;
		optData[3] = (byte) servoID;
		
		addData(optData);		//add servo data to list, speed mode
	}
	
	// add data to variable list servo for syncro execution
	private void addData(byte[] optData)
	{
		for (int i = 0; i < optData.length; i++) {
			multipleMoveData.add(optData[i]);
		}
	}
	
	// move all servo with the same execution time
	public void actionAll(int playTime)
	{
		if ((playTime <0) || (playTime > 2856)) return;
		
		int optDataSize = multipleMoveData.size();
		if (optDataSize < 4) return;
		
		byte[] optData = new byte[optDataSize + 1];
		
		optData[0] = (byte) playTime;
		for (int i = 0; i < optDataSize; i++) {
			optData[i+1] = multipleMoveData.get(i);
		}
		
		byte[] packetBuf = buildPacket((byte)0xFE, HSJOG, optData);
		sendData(packetBuf);

		multipleMoveData.clear();
	}
	
	// LED  -  GREEN, BLUE, RED 
	public void setLed(int servoID, int led)
	{
		byte[] optData = new byte[3];
		optData[0] = 0x35;               	// Address
		optData[1] = 0x01;              	// Length
		
		byte led2 = 0x00;
		if ((led & LED_GREEN) == LED_GREEN) led2 |= 0x01;
		if ((led & LED_BLUE) == LED_BLUE) led2 |= 0x02;
		if ((led & LED_RED) == LED_RED) led2 |= 0x04;
		
		optData[2] = led2;
		byte[] packetBuf = buildPacket((byte)servoID, HRAMWRITE, optData);
		sendData(packetBuf);
	}
	
	// reboot single servo - pay attention 253 - all servos doesn't work!
	public void reboot(int servoID) {
		if (servoID == 0xFE) return;
		
		byte[] packetBuf = buildPacket((byte)servoID, HREBOOT, null);
		sendData(packetBuf);
	}
	
	// stat
	public byte stat(int servoID)
	{
		byte[] packetBuf = buildPacket((byte)servoID, HSTAT, null);
		sendData(packetBuf);
		
	    try {
			Thread.sleep(WAIT_TIME_BY_ACK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		byte[] readBuf = readData();
	    
	    if (!isRightPacket(readBuf)) {
	    	return -1;
	    }

		return readBuf[7];			// return status
	}
	
	// model - 1=0101 - 2=0201
	public byte model(int servoID)
	{
		byte[] optData = new byte[2];
		optData[0] = 0x00;               	// Address
		optData[1] = 0x01;              	// Length
		
		byte[] packetBuf = buildPacket((byte)servoID, HEEPREAD, optData);
		sendData(packetBuf);
	  	
		try {
			Thread.sleep(WAIT_TIME_BY_ACK);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		 
		byte[] readBuf = readData();
	    
	    if (!isRightPacket(readBuf)) {
	    	return -1;
	    }

		return readBuf[8];			// return model
	}
	
	// setID - Need to restart the servo
	public void set_ID(int ID_Old, int ID_New)
	{
		byte[] optData = new byte[3];
		optData[0] = 0x06;               	// Address
		optData[1] = 0x01;              	// Length
		optData[2] = (byte) ID_New;
		
		byte[] packetBuf = buildPacket((byte)ID_Old, HEEPWRITE, optData);
		sendData(packetBuf);
	}
	
	// write registry in the RAM: one byte 
	public void writeRegistryRAM(int servoID, int address, int writeByte)
	{
		byte[] optData = new byte[3];
		optData[0] = (byte) address;        // Address
		optData[1] = 0x01;              	// Length
		optData[2] = (byte) writeByte;
		
		byte[] packetBuf = buildPacket((byte)servoID, HRAMWRITE, optData);
		sendData(packetBuf);
	}

	// write registry in the EEP memory (ROM): one byte 
	public void writeRegistryEEP(int servoID, int address, int writeByte)
	{
		// Do not use it. Instead, use HerkuleX Manager to setup EEP ROM
		// If you want to really use it, uncomment these lines, and be careful.
//		byte[] optData = new byte[3];
//		optData[0] = (byte) address;        // Address
//		optData[1] = 0x01;              	// Length
//		optData[2] = (byte) writeByte;
//		
//		byte[] packetBuf = buildPacket((byte)servoID, HEEPWRITE, optData);
//		sendData(packetBuf);
	}
	
	private boolean isRightPacket(byte[] buf) {
	    byte chksum1 = checksum1(buf);			// Checksum1
	    byte chksum2 = checksum2(chksum1);		// Checksum2

	    if (chksum1 != buf[5]) return false;
		if (chksum2 != buf[6]) return false;
		
		return true;
	}
	
	private byte[] buildPacket(byte pId, byte cmd, byte[] optData) {
		int pktSize;
		
		if (optData == null) {
			pktSize = BASIC_PKT_SIZE;
		} else {
			pktSize = BASIC_PKT_SIZE + optData.length;
		}

		byte[] packetBuf = new byte[pktSize];
		
		packetBuf[0] = (byte) 0xFF;					// Packet Header
		packetBuf[1] = (byte) 0xFF;					// Packet Header	
		packetBuf[2] = (byte) pktSize;	 			// Packet Size
		packetBuf[3] = pId;							// Servo ID
		packetBuf[4] = cmd;							// Command
		
		for (int i = 0; i < pktSize - BASIC_PKT_SIZE; i++) {
			packetBuf[BASIC_PKT_SIZE+i] = optData[i];
		}
		
		packetBuf[5] = checksum1(packetBuf);		// Checksum 1
		packetBuf[6] = checksum2(packetBuf[5]);		// Checksum 2

		return packetBuf;
	}

	// checksum1
	private byte checksum1(byte[] buf) {
	  byte chksum1 = 0x00;	
	  
	  for (int i = 0; i < buf.length; i++) {
		  if (i == 0 || i == 1 || i == 5 || i == 6) continue;
		  chksum1 ^= buf[i];
	  }
	  
	  return (byte) (chksum1 & 0xFE);
	}
	
	// checksum2
	private byte checksum2(byte chksum1) {
	  return (byte) ((~chksum1) & 0xFE);
	}
	
	// Sending the buffer long legnth to Serial port
	private void sendData(byte[] buffer) {
		mPort.write(buffer);
	}
	
	private byte[] readData() {
		int size = 0;
		byte readBuf[] = new byte[255];
		
	    while (mPort.available() > 0) {
	    	int inBuffer = mPort.read();
	    	readBuf[size++] = (byte) (inBuffer & 0xFF);
//	    	System.out.println(inBuffer & 0xFF);
	    }
	    
	    byte retBuf[] = new byte[size];
	    System.arraycopy(readBuf, 0, retBuf, 0, retBuf.length);
	    return retBuf;
	}
}