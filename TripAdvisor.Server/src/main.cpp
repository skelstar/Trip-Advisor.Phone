#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <myPushButton.h>
#include <ArduinoJson.h>

/*--------------------------------------------------------------------------------*/

const char compile_date[] = __DATE__ " " __TIME__;
const char file_name[] = __FILE__;

//--------------------------------------------------------------
struct VESC_DATA {
	float volts;
	float motorA;
	bool moving;
	bool online;
};
VESC_DATA vescdata;

#define BUTTON_A_PIN 39
#define BUTTON_B_PIN 38
#define BUTTON_C_PIN 37

//--------------------------------------------------------------------------------

void button_callback( int eventCode, int eventPin, int eventParam );
void notifyClient();
bool deviceConnected = false;

#define 	PULLUP	true
#define 	OFF_STATE_HIGH	1
myPushButton button(BUTTON_A_PIN, PULLUP, OFF_STATE_HIGH, button_callback, 500);

void button_callback( int eventCode, int eventPin, int eventParam ) {

  switch (eventCode) {
    case button.EV_BUTTON_PRESSED:
      break;
    case button.EV_RELEASED:
      Serial.printf("EV_RELEASED\n");
      //if (deviceConnected) {
          notifyClient();
      //}
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

void setup()
{
	Serial.begin(115200);
  Serial.println("Starting TripAdvisor.Server!");

  vescdata.volts = 35.0;
  vescdata.motorA = 1;
  vescdata.moving = false;
  vescdata.online = true;

  setupBLE();
}

//*************************************************************

long now = 0;

void loop() {

  button.serviceEvents();

  // if (millis() - now > 2000) {
  //   now = millis();
  //   if (deviceConnected) {
  //     notifyClient();
  //   }
  //   if (vescdata.volts > 44.2) {
  //     vescdata.volts = 35.0;
  //   }
  // }
  delay(10);
}
//*************************************************************
bool controllerOnline = true;

//--------------------------------------------------------------

struct TRIP_DATA {
  float volts;
  int amphours;
} tripdata;

void notifyClient() {

  tripdata.volts = vescdata.volts += 0.1;
  tripdata.amphours = 123;

  // https://arduinojson.org/v6/assistant/
  const size_t capacity = JSON_OBJECT_SIZE(2);
  DynamicJsonDocument doc(capacity);

  doc["volts"] = tripdata.volts;
  doc["amphours"] = tripdata.amphours;

  String output;
  serializeJson(doc, output);

  pCharacteristic->setValue(output.c_str());
	Serial.printf("notifying!: %s\n", output.c_str());
	pCharacteristic->notify();
}
//--------------------------------------------------------------
void setupBLE() {

    BLEDevice::init("Trip Advisor Server");
    BLEServer *pServer = BLEDevice::createServer();
    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
      BLECharacteristic::PROPERTY_WRITE |
      BLECharacteristic::PROPERTY_NOTIFY
    );
	  pCharacteristic->addDescriptor(new BLE2902());

    pCharacteristic->setCallbacks(new MyServerCallbacks());
    // initial value
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

