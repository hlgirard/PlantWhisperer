#define _DEBUG_

#include <ESP8266WiFi.h>
#include <ThingerESP8266.h>
#include <credentials.h>

ThingerESP8266 thing(thinger_usrname, thinger_deviceID, thinger_deviceCred);

int sensor_pin_a = A0;

int readMoisture() {
  int output_value = 0;
  for (int i=0; i<10; i++) {
    output_value = output_value + analogRead(sensor_pin_a);
    delay(100);
  }
  
  output_value = output_value / 10;
  
  output_value = map(output_value,700,0,0,100);
  return output_value;
}

void sleep(int interval) {
  Serial.println("Going to Sleep");
  ESP.deepSleep(interval); //microseconds
}

void setup() {
  Serial.begin(74880);
  Serial.println("Starting up");
  
  // Setup wifi and set server address
  Serial.println("Setting up Wifi");
  thing.add_wifi(wifi_ssid, wifi_password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  // Set sensor input pin
  pinMode(sensor_pin_a, INPUT);

  // Setup the moisture thing
  thing["moisture"] >> [](pson& out){
      out = readMoisture();
  };
}

void loop() {
  // Handle the connection
  thing.handle();

  // Write moisture data to the bucket ("bucket_Id", "thing_name")
  Serial.println("Writing moisture data to bucket");
  thing.write_bucket("soil_moisture", "moisture");

  // Sleep
  delay(500);
  sleep(20 * 60 * 1000000); // sampling interval in microseconds (20 mn)
}
