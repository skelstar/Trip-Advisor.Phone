#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <myPushButton.h>
#include <ArduinoJson.h>
#include <vesc_comms.h>

/*--------------------------------------------------------------------------------*/

const char compile_date[] = __DATE__ " " __TIME__;
const char file_name[] = __FILE__;

//--------------------------------------------------------------
struct VESC_DATA {
	float batteryVoltage;
	float motorCurrent;
  float ampHours;
	bool moving;
	bool vescOnline;
  float odometer;
};
VESC_DATA vescdata;

uint8_t vesc_packet[PACKET_MAX_LENGTH];

vesc_comms vesc;

#include "vesc.h"

//--------------------------------------------------------------------------------

#define BUTTON_A_PIN  39
#define BUTTON_B_PIN  38
#define BUTTON_C_PIN  37
#define BUTTON_BOOT   0

//--------------------------------------------------------------------------------

void button_callback( int eventCode, int eventPin, int eventParam );
void notifyClient();
bool deviceConnected = false;

#define 	PULLUP	true
#define 	OFF_STATE_HIGH	1
myPushButton button(BUTTON_BOOT, PULLUP, OFF_STATE_HIGH, button_callback, 500);

void button_callback( int eventCode, int eventPin, int eventParam ) {

  switch (eventCode) {
    case button.EV_BUTTON_PRESSED:
      break;
    case button.EV_RELEASED:
      notifyClient();
      break;
    case button.EV_SPECFIC_TIME_REACHED:
      break;
    default:    
        break;
  }
}

//--------------------------------------------------------------------------------

#define SERVICE_UUID        "4FAFC201-1FB5-459E-8FCC-C5C9C331914B"
#define CHARACTERISTIC_UUID "BEB5483E-36E1-4688-B7F5-EA07361B26A8"

BLECharacteristic *pCharacteristic;

/**************************************************************/

class MyServerCallbacks: public BLECharacteristicCallbacks {
	// receive
  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();
    if (value.length() > 0) {
      Serial.println("*********");
      Serial.print("New value: ");
      for (int i = 0; i < value.length(); i++) {
        Serial.print(value[i]);
      }
      Serial.println();
      Serial.println("*********");
    }
  }

	void onConnect(BLEServer* pServer) {
		Serial.printf("device connected\n");
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
		Serial.printf("device disconnected\n");
    deviceConnected = false;
  }
};

//--------------------------------------------------------------------------------

void setupBLE();
void notifyClient();
void getVescValues();
bool poweringDown();


void setup()
{
	Serial.begin(115200);
  Serial.println("Starting TripAdvisor.Server!");

  vesc.init(115200);
  
  setupBLE();
}

//*************************************************************

long now = 0;

void loop() {

  button.serviceEvents();

  if (millis() - now > 1000) {
    now = millis();
    getVescValues();

    if (poweringDown()) {
      notifyClient();
    }
  }
  delay(10);
}
//*************************************************************
bool controllerOnline = true;

//--------------------------------------------------------------
void notifyClient() {

// https://arduinojson.org/v6/assistant/
  const size_t capacity = JSON_OBJECT_SIZE(3);
  DynamicJsonDocument doc(capacity);

  doc["volts"] = vescdata.batteryVoltage;
  doc["amphours"] = vescdata.ampHours;
  doc["distance"] = vescdata.odometer;

  String output;
  serializeJson(doc, output);

  pCharacteristic->setValue(output.c_str());
	Serial.printf("notifying!: %s\n", output.c_str());
	pCharacteristic->notify();
}
//--------------------------------------------------------------
void getVescValues() {

    int numbytes = vesc.fetch_packet( vesc_packet );
    if ( numbytes > 1 ) {
      vescdata.batteryVoltage = vesc.get_voltage(vesc_packet);
      vescdata.motorCurrent = vesc.get_motor_current(vesc_packet);
      vescdata.ampHours = vesc.get_amphours_discharged(vesc_packet);
      vescdata.odometer = getOdometer();
      Serial.printf("Batt: %.1f \n", vescdata.batteryVoltage);
    }
    else {
      Serial.printf("VESC not responding!\n");
    }
}

bool poweringDown() {
  return vescdata.batteryVoltage > 20.0 && vescdata.batteryVoltage < 30.0;
}
//--------------------------------------------------------------
void setupBLE() {

    BLEDevice::init("Trip Advisor ESPDEV");
    BLEServer *pServer = BLEDevice::createServer();
    BLEService *pService = pServer->createService(SERVICE_UUID);
    Serial.printf("UUID: %d\n", pServer->getConnId());
    pCharacteristic = pService->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
      BLECharacteristic::PROPERTY_WRITE |
      BLECharacteristic::PROPERTY_NOTIFY
    );
	  pCharacteristic->addDescriptor(new BLE2902());

    pCharacteristic->setCallbacks(new MyServerCallbacks());
    pCharacteristic->setValue("Hello World says Neil");
    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
    Serial.printf("Characteristic defined!\n");
}

