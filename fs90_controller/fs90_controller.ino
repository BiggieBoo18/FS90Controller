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
#define CMD_ANGLE1    0
#define CMD_ANGLE2    1
#define CMD_ANGLE3    2
#define CMD_DUTYCYCLE 3

// pin
const int pwmPin1 = 25;
const int pwmPin2 = 17;
const int pwmPin3 = 16;

// angle range
const double min_angle = 5;
const double max_angle = 175;

// PWM properties
const double frequency = 226.24434389140271493212669683258;
const int pwmChannel1 = 0;
const int pwmChannel2 = 1;
const int pwmChannel3 = 2;
const int resolution = 10;

// max/min Dutycycle(when frequency is 226.24434389140271493212669683258)
const double max_dutycycle1 = 495;  // when 175 degrees(resolution = 10)2,130us
const double mid_dutycycle1 = 285; // when 90 degrees(resolution = 10) 1,220us
const double min_dutycycle1 = 110; // when 5 degrees(resolution = 10) 460us
const double interval1 = (max_dutycycle1 - min_dutycycle1) / (max_angle - min_angle);

const double max_dutycycle2 = 550;  // when 175 degrees(resolution = 10)2,320us
const double mid_dutycycle2 = 300; // when 90 degrees(resolution = 10) 1,260us
const double min_dutycycle2 = 115; // when 5 degrees(resolution = 10) 460us
const double interval2 = (max_dutycycle2 - min_dutycycle2) / (max_angle - min_angle);

const double max_dutycycle3 = 515;  // when 175 degrees(resolution = 10)2,200us
const double mid_dutycycle3 = 300; // when 90 degrees(resolution = 10) 1,260us
const double min_dutycycle3 = 117; // when 5 degrees(resolution = 10) 480us
const double interval3 = (max_dutycycle3 - min_dutycycle3) / (max_angle - min_angle);

// dutycycle
double dutyCycle1 = mid_dutycycle1;
double dutyCycle2 = mid_dutycycle2;
double dutyCycle3 = mid_dutycycle3;

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
  ledcSetup(pwmChannel1, frequency, resolution);
  ledcSetup(pwmChannel2, frequency, resolution);
  ledcSetup(pwmChannel3, frequency, resolution);
  
  // attach the channel to the GPIO to be controlled
  ledcAttachPin(pwmPin1, pwmChannel1);
  ledcAttachPin(pwmPin2, pwmChannel2);
  ledcAttachPin(pwmPin3, pwmChannel3);
}

void parse_command() {
  String command = buffer.pop();
  if (command == "") {
    return;
  }
  Serial.println(command);
  String type  = command.substring(0, 1); // command type
  String value = command.substring(2);    // command value
  switch (type.toInt()) {
    case CMD_ANGLE1:
    {
      Serial.println("CMD_ANGLE1");
      double angle = value.toDouble();
      Serial.println(angle);
      if (angle > max_angle) {
        angle = max_angle;
      } else if (angle < min_angle) {
        angle = min_angle;
      }
      dutyCycle1 = min_dutycycle1 + ((angle - min_angle) * interval1);
      if (dutyCycle1 > max_dutycycle1) {
        dutyCycle1 = max_dutycycle1;
      }
      Serial.println(dutyCycle1);
      break;
    }
    case CMD_ANGLE2:
    {
      Serial.println("CMD_ANGLE2");
      double angle = value.toDouble();
      Serial.println(angle);
      if (angle > max_angle) {
        angle = max_angle;
      } else if (angle < min_angle) {
        angle = min_angle;
      }
      dutyCycle2 = min_dutycycle2 + ((angle - min_angle) * interval2);
      if (dutyCycle2 > max_dutycycle2) {
        dutyCycle2 = max_dutycycle2;
      }
      Serial.println(dutyCycle2);
      break;
    }
    case CMD_ANGLE3:
    {
      Serial.println("CMD_ANGLE3");
      double angle = value.toDouble();
      Serial.println(angle);
      if (angle > max_angle) {
        angle = max_angle;
      } else if (angle < min_angle) {
        angle = min_angle;
      }
      dutyCycle3 = min_dutycycle3 + ((angle - min_angle) * interval3);
      if (dutyCycle3 > max_dutycycle3) {
        dutyCycle3 = max_dutycycle3;
      }
      Serial.println(dutyCycle3);
      break;
    }
    case CMD_DUTYCYCLE:
    {
      Serial.println("CMD_DUTYCYCLE");
      dutyCycle1 = value.toDouble();
//      if (dutyCycle1 > max_dutycycle1) {
//        dutyCycle1 = max_dutycycle1;
//      }
      if (dutyCycle1 < min_dutycycle1) {
        dutyCycle1 = min_dutycycle1;
      }
      Serial.println(dutyCycle1);
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
  ledcWrite(pwmChannel1, dutyCycle1);
  ledcWrite(pwmChannel2, dutyCycle2);
  ledcWrite(pwmChannel3, dutyCycle3);
  delay(50);
}
