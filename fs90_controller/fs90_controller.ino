/*
 * FS90 controller on ESP32DevkitC
 */

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <CircularBuffer.h>

// UART service UUID
#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

// COMMAND
#define CMD_ANGLE     0
#define CMD_DUTYCYCLE 1

// pin
const int pwmPin = 25;



// angle range
const double min_angle = 5;
const double max_angle = 175;

// PWM properties
const double frequency = 226.24434389140271493212669683258;
const int pwmChannel = 0;
const int resolution = 10;

// max/min Dutycycle(when frequency is 226.24434389140271493212669683258)
const double max_dutycycle = 495;  // when 175 degrees(resolution = 10)2,130us
const double mid_dutycycle = 285; // when 90 degrees(resolution = 10) 1,220us
const double min_dutycycle = 110; // when 5 degrees(resolution = 10) 460us

// max/min Dutycycle(when frequency is 238.09523809523809523809523809524)
//const double max_dutycycle = 540;  // when 180 degrees(resolution = 10)2,210us
//const double mid_dutycycle = 300; // when 90 degrees(resolution = 10) 1,220us
//const double min_dutycycle = 114; // when 5 degrees(resolution = 10) 460us
const double interval = (max_dutycycle - min_dutycycle) / (max_angle - min_angle);

// dutycycle
double dutyCycle = mid_dutycycle;

// BLE variables
BLEServer *pServer = NULL;
BLECharacteristic * pTxCharacteristic;
bool deviceConnected = false;
bool oldDeviceConnected = false;
bool isEngineStarted = false;
uint8_t txValue = 0;
CircularBuffer<String, 10> buffer;

// Server callback
class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.println("connected");
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.println("disconnected");
      deviceConnected = false;
      pServer->startAdvertising(); // restart advertising
    }
};

// Data recieved callback
class DataRecievedCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      buffer.push(pCharacteristic->getValue().c_str());
    }
};
 
void setup(){
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("FS90 Controller");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

    // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pTxCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_UUID_TX,
                       BLECharacteristic::PROPERTY_NOTIFY
                      );
                      
  pTxCharacteristic->addDescriptor(new BLE2902());

  BLECharacteristic * pRxCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_UUID_RX,
                        BLECharacteristic::PROPERTY_WRITE
                      );

  pRxCharacteristic->setCallbacks(new DataRecievedCallbacks());

  // Start the service
  pService->start();

  // Start advertising
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to notify...");
  
  // configure PWM functionalitites
  ledcSetup(pwmChannel, frequency, resolution);
  
  // attach the channel to the GPIO to be controlled
  ledcAttachPin(pwmPin, pwmChannel);
}

void parse_command() {
  String command = buffer.pop();
  if (command == "") {
    return;
  }
  String type  = command.substring(0, 1); // command type
  String value = command.substring(2);    // command value
  switch (type.toInt()) {
    case CMD_ANGLE:
    {
      Serial.println("CMD_ANGLE");
      double angle = value.toDouble();
      Serial.println(angle);
      if (angle > max_angle) {
        angle = max_angle;
      } else if (angle < min_angle) {
        angle = min_angle;
      }
      dutyCycle = min_dutycycle + ((angle - min_angle) * interval);
      if (dutyCycle > max_dutycycle) {
        dutyCycle = max_dutycycle;
      }
      Serial.println(dutyCycle);
      break;
    }
    case CMD_DUTYCYCLE:
    {
      Serial.println("CMD_DUTYCYCLE");
      dutyCycle = value.toDouble();
      if (dutyCycle > max_dutycycle) {
        dutyCycle = max_dutycycle;
      }
      if (dutyCycle < min_dutycycle) {
        dutyCycle = min_dutycycle;
      }
      Serial.println(dutyCycle);
      break;
    }
    default:
    {
      Serial.println("CMD_UNKNOWN");
    }
  }
}

void loop(){
  parse_command();
  ledcWrite(pwmChannel, dutyCycle);
  delay(50);
}
