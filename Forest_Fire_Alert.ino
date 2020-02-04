
#include <SoftwareSerial.h>
SoftwareSerial GSM(8, 7); // RX, TX
int smokeA0 = A5;

int sensorThres = 320;
boolean sent = false;

void readStuff(){
  Serial.write(GSM.read());
}


void sendGSM(const char* msg, int waitMs = 1000) {
  GSM.println(msg);
  delay(waitMs);
 
}


void setup() {
  GSM.begin(9600);
  Serial.begin(9600);
Serial.println("Setting Up.......");
      sendGSM("AT+SAPBR=3,1,\"APN\",\"web.zain.ug.com\"");
//  sendGSM("AT+SAPBR=3,1,\"APN\",\"yellopix.mtn.co.ug\"");
      sendGSM("AT+SAPBR=1,1");
      sendGSM("AT+HTTPINIT");  
      sendGSM("AT+HTTPPARA=\"CID\",1");
      sendGSM("AT+HTTPPARA=\"URL\",\"http://forestfirealert.herokuapp.com/log.php\"");
      sendGSM("AT+HTTPACTION=0", 5000);
      sendGSM("AT+HTTPACTION=0");
      sendGSM("AT+HTTPACTION=0");
      sendGSM("AT+HTTPREAD=0,100");
    Serial.println("Started");
//  pinMode(smokeA0, INPUT);
//  pinMode(13,OUTPUT);
 delay(5000);
 Serial.println("Finishing Up.......");
}


void sendAlert(){
  sendGSM("AT+HTTPPARA=\"URL\",\"http://forestfirealert.herokuapp.com/report_fire.php\""); 
  sendGSM("AT+HTTPACTION=0", 3000);
  //sendGSM("AT+HTTPACTION=0");
  sendGSM("AT+HTTPREAD=0,100");
  
  
  delay(5000);
  sent = true;
}

void logIn(){
   sendGSM("AT+HTTPPARA=\"URL\",\"http://forestfirealert.herokuapp.com/log.php\"");
   sendGSM("AT+HTTPACTION=0", 3000);
   sendGSM("AT+HTTPREAD=0,100");
   delay(5000);
}


void loop() {

  logIn();
   while(GSM.available()) 
    Serial.write(GSM.read());

  while (Serial.available()){
    GSM.write(Serial.read());
  }
  int analogSensor = analogRead(smokeA0);
  Serial.println(analogSensor);
  // Checks if it has reached the threshold value
  if (analogSensor > sensorThres)
  {
   if(!sent){
    Serial.println("FIRE ON THE MOUNTAIN RUN RUN RUN !!!!!!");
   sendAlert();
   }
  }else{
    digitalWrite(13,LOW);
  }



}
