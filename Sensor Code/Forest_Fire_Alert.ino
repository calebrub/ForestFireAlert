#include <SPI.h>

#include <SoftwareSerial.h>
#include <TinyGPS++.h>

const byte rxPin = 2;
const byte txPin = 3;
static const int RXPin = 4, TXPin = 5;
static const uint32_t GPSBaud = 9600;

TinyGPSPlus gps;

SoftwareSerial ss(RXPin, TXPin);

SoftwareSerial Wifi (rxPin, txPin);    


const int smokePin = A0;
int smoke;
String c;

void printResponse() {
  

  while (Wifi.available()) {
    Serial.println(Wifi.readStringUntil('\n')); 
  }
}      

void setup() {

  Serial.begin(9600);   
  Wifi.begin(115200);
  ss.begin(GPSBaud);
  delay(1000);

//  while( c == ""){
    Serial.println("Waiting for Location....");
    delay(1000);
      while (ss.available() > 0){
    gps.encode(ss.read());
    if (gps.location.isUpdated()){

      c = String(gps.location.lat(), 6) + "," + String(gps.location.lng(), 6);
    }
}
//}
  Serial.println("Location Achieved: " +c); 
   Wifi.println("AT+CIPMUX=1");
  delay(500);
  printResponse();

  Wifi.println("AT+CIPSTART=4,\"TCP\",\"192.168.43.167\",5000");
  delay(1000);
  printResponse();
 
    String cmd = "GET /register_sensor?location="+ c +" HTTP/1.1";
    Wifi.println("AT+CIPSEND=4," + String(cmd.length() + 4));
    delay(500);

    Wifi.println(cmd);
    delay(1000);
    Wifi.println(""); 
//  }

  if (Wifi.available()) {
    Serial.write(Wifi.read());
  
  Serial.println("Done SetUP!!"); 

  
}
}



void loop() {
  
  smoke = analogRead(smokePin);

  Wifi.println("AT+CIPMUX=1");
  delay(500);
  Serial.write(Wifi.read());
  printResponse();

  Wifi.println("AT+CIPSTART=4,\"TCP\",\"192.168.43.167\",5000");
  delay(1000);
  printResponse();

    String cmd = "GET /log?smoke="+ String(smoke) +"&location="+c+" HTTP/1.1";
    Wifi.println("AT+CIPSEND=4," + String(cmd.length() + 4));
    delay(500);

    Wifi.println(cmd);
    delay(1000);
    Wifi.println(""); 

     while (ss.available() > 0){
    gps.encode(ss.read());
    if (gps.location.isUpdated()){

      c = String(gps.location.lat(), 6) + "," + String(gps.location.lng(), 6);
    }
     }


  if (Wifi.available()) {
    Serial.write(Wifi.read());
  }

}
