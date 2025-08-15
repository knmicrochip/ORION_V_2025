#include <Stepper.h>
#include <Arduino.h>
#include <include/twi.h>
#include <Wire.h> // I2C library
#include "SparkFun_AS7265X.h"
#define ARDUINOJSON_USE_DOUBLE 0
#include <ArduinoJson.h>
#include "HX711.h"

struct sample{
  int number;
  float mass;
  float temp;
  float lights[18];
  float gasses[4];
};

struct sample sample1;
struct sample sample2;
struct sample sample3;
struct sample sample4;
struct sample sample5;
struct sample sample6;

sample *samples[6] = {&sample1,&sample2,&sample3,&sample4,&sample5,&sample6};
int currSample = 0;

//opto
const int opto1 = 41;
const int opto2 = 42;

//drum
const int drumStepsPer60deg = 2666;
const int dir = 39;
const int step = 40;
Stepper drumStepper(drumStepsPer60deg*6,dir,step);
//rotateDrum();

void rotateDrum(){
  unsigned long prevMillis = millis();
  unsigned long currMillis = millis();
  int pulse = 0;
  bool hilo = false;

  while(true){
    drumStepper.step(1);
    currMillis = millis();
    if(pulse < 5){
      if(hilo){
        if(digitalRead(opto2) == LOW && currMillis - prevMillis > 100){
          hilo = false;
          pulse += 1;
        }
      }else{
        if(digitalRead(opto2) == HIGH && currMillis - prevMillis > 100){
          hilo = true;
        }
      }
    }else{
      drumStepper.step(150);
      break;
    }
  }
}

//drill
const int enableSwiderA = 31;
const int enableSwiderB = 32;
const int btsFbSwiderA = A6;
const int btsFbSwiderB = A7;
const int btsPwmSwiderA = 4;
const int btsPwmSwiderB = 5;

const int enableWindaA = 33;
const int enableWindaB = 34;
const int btsFbWindaA = A8;
const int btsFbWindaB = A9;
const int btsPwmWindaA = 6;
const int btsPwmWindaB = 7;

bool drillDir = true;

void drill(int pwm){
  if(pwm > 0){
    analogWrite(btsPwmSwiderA,pwm);
    analogWrite(btsPwmSwiderB,0);
  }else if(pwm < 0){
    analogWrite(btsPwmSwiderA,0);
    analogWrite(btsPwmSwiderB,pwm);
  }else if(pwm == 0){
    analogWrite(btsPwmSwiderA,0);
    analogWrite(btsPwmSwiderB,0);
  }
}

void elev(int pwm){
  if(pwm > 0){
    analogWrite(btsPwmWindaA,pwm);
    analogWrite(btsPwmWindaB,0);
  }else if(pwm < 0){
    analogWrite(btsPwmWindaA,0);
    analogWrite(btsPwmWindaB,pwm);
  }else if(pwm == 0){
    analogWrite(btsPwmWindaA,0);
    analogWrite(btsPwmWindaB,0);
  }
}

//MQ gas sensors & laser
const int MQon = 24;
const int MQ2 = A2;
const int MQ4 = A3;
const int MQ5 = A4;
const int MQ8 = A5;
const int laserOn = 8;

void measureGas(struct sample *sample){
  digitalWrite(MQon,HIGH);
  delay(45000);
  analogWrite(laserOn,255);
  delay(10000);
  analogWrite(laserOn,0);
  sample->gasses[0] = analogRead(MQ2)*0.00538;
  sample->gasses[1] = analogRead(MQ4)*0.00538;
  sample->gasses[2] = analogRead(MQ5)*0.00538;
  sample->gasses[3] = analogRead(MQ8)*0.00538;
  digitalWrite(MQon,LOW);
}

//conveyor
const int en_tasm = 2;
const int in1 = 27;
const int in2 = 28;

void conveyor(int dir){
  if(dir == 1){
    digitalWrite(in1,HIGH);
    digitalWrite(in2,LOW);
  }else if(dir == -1){
    digitalWrite(in2,HIGH);
    digitalWrite(in1,LOW);
  }else if(dir == 0){
    digitalWrite(in2,LOW);
    digitalWrite(in1,LOW);
  }
}

//spectro
AS7265X spectro;

void measureSpectro(struct sample *sample){
  spectro.takeMeasurementsWithBulb(); //This is a hard wait while all 18 channels are measured

  sample->lights[0] = spectro.getCalibratedA(); //410nm
  sample->lights[1] = spectro.getCalibratedB(); //435nm
  sample->lights[2] = spectro.getCalibratedC(); //460nm
  sample->lights[3] = spectro.getCalibratedD(); //485nm
  sample->lights[4] = spectro.getCalibratedE(); //510nm
  sample->lights[5] = spectro.getCalibratedF(); //535nm

  sample->lights[6] = spectro.getCalibratedG(); //560nm
  sample->lights[7] = spectro.getCalibratedH(); //585nm
  sample->lights[8] = spectro.getCalibratedR(); //610nm
  sample->lights[9] = spectro.getCalibratedI(); //645nm
  sample->lights[10] = spectro.getCalibratedS(); //680nm
  sample->lights[11] = spectro.getCalibratedJ(); //705nm

  sample->lights[12] = spectro.getCalibratedT(); //730nm
  sample->lights[13] = spectro.getCalibratedU(); //760nm
  sample->lights[14] = spectro.getCalibratedV(); //810nm
  sample->lights[15] = spectro.getCalibratedW(); //860nm
  sample->lights[16] = spectro.getCalibratedK(); //900nm
  sample->lights[17] = spectro.getCalibratedL(); //940nm
}

//thermo hack

#define ADDR      0x5A
#define TOBJ_1    0x07

Twi *pTwi = WIRE_INTERFACE;


uint8_t readByte() {
  while (!TWI_ByteReceived(pTwi));
  return TWI_ReadByte(pTwi);
}

void measureTemp(struct sample *sample){
  uint16_t tempUK;
  float tempK;
  uint8_t hB, lB, pec;

  TWI_StartRead(pTwi, ADDR, TOBJ_1, 1);

  lB = readByte();
  hB = readByte();
  
  //last read
  TWI_SendSTOPCondition(pTwi);
  pec = readByte();
  
  while (!TWI_TransferComplete(pTwi));

  tempUK = (hB << 8) | lB;
  tempK = ((float)tempUK * 2) / 100 ;
  sample->temp =  tempK - 273.15;
}

void sendSampleData(sample *sample){
  StaticJsonDocument<512> doc;
  doc["eventType"] = "science";
  JsonObject payload = doc.createNestedObject("payload");
  payload["number"] = sample->number;
  /*if(sample->type){
    payload["type"] = "surface";
  }else{
    payload["type"] = "deep";
  }*/
  payload["mass"] = sample->mass;
  payload["temp"] = sample->temp;

  JsonArray gasses = payload.createNestedArray("gasses");
  for(int i=0;i<4;i++){
    gasses.add(sample->gasses[i]);
  }
  JsonArray lights = payload.createNestedArray("lights");
  for(int i=0;i<18;i++){
    lights.add(sample->lights[i]);
  }

  //serializeJsonPretty(doc, Serial);
  String output;
  serializeJson(doc, output);
  output.concat("\n\n");
  Serial.print(output);
}

//scale
HX711 scale;

uint8_t dataPin = 45;
uint8_t clockPin = 46;

void researchSequence(sample *sample){
  sample->mass = scale.get_units(10);
  rotateDrum();
  delay(1000);
  rotateDrum();
  delay(500);
  measureTemp(sample);
  delay(500);
  rotateDrum();
  measureSpectro(sample);
  rotateDrum();
  measureGas(sample);
  rotateDrum();
  rotateDrum();
  rotateDrum();
  sendSampleData(sample);
}

void sendTelemetry(){
  StaticJsonDocument<255> doc;
  doc["eventType"] = "science";
  //doc["mode"] = "pwm";
  JsonObject payload = doc.createNestedObject("payload");

  payload["FbDrillA"] = float(analogRead(btsFbSwiderA)*0.0274);
  payload["FbDrillB"] = float(analogRead(btsFbSwiderB)*0.0274);

  payload["FbElevatorA"] = float(analogRead(btsFbWindaA)*0.0274);
  payload["FbElevatorB"] = float(analogRead(btsFbWindaB)*0.0274);

  //serializeJsonPretty(doc, Serial);
  String output;
  serializeJson(doc, output);
  output.concat("\n\n");
  Serial.print(output);
}

void setup() {
  sample1.number = 1;
  sample2.number = 2;
  sample3.number = 3;
  sample4.number = 4;
  sample5.number = 5;
  sample6.number = 6;

  Serial.begin(115200);
  while(!Serial);
  Wire.begin(); //Join I2C bus

  int inputs[] = {btsFbSwiderA,btsFbSwiderB,btsFbWindaA,btsFbWindaB,MQ2,MQ4,MQ5,MQ8,dataPin};
  int outputs[] = {enableSwiderA,enableSwiderB,btsPwmSwiderA,btsPwmSwiderB,enableWindaA,enableWindaB,btsPwmWindaA,btsPwmWindaB,
  MQon,laserOn,en_tasm,in1,in2,clockPin};

  for(int i = 0;i< sizeof inputs/sizeof inputs[0]; i++ ){
    pinMode(inputs[i],INPUT);
  }

  for(int i = 0;i< sizeof outputs/sizeof outputs[0]; i++ ){
    pinMode(outputs[i],OUTPUT);
  }

  pinMode(opto1,INPUT_PULLUP);
  pinMode(opto2,INPUT_PULLUP);

  scale.begin(dataPin, clockPin);
  scale.set_scale(350);
  scale.tare(20);

  //motors
  digitalWrite(en_tasm,HIGH);
  drumStepper.setSpeed(7);
  digitalWrite(enableSwiderA,HIGH);
  digitalWrite(enableSwiderB,HIGH);
  digitalWrite(enableWindaA,HIGH);
  digitalWrite(enableWindaB,HIGH);

  //spectro
  spectro.begin();
  spectro.disableIndicator(); //Turn off the blue status LED

  //test
  /*
  sample1.mass = 25.34;
  sample1.rad = 305.3;
  sample1.temp = 36.6;
  sample1.type = true;
  for(int i = 0;i<4;i++){
    sample1.gasses[i] = 100*i+i^2;
  }
  for(int i = 0;i<18;i++){
    sample1.lights[i] = 100*i+0.2;
  }
  */

  //calibrate drum
  if(digitalRead(opto1) == 0){
    while(digitalRead(opto1) == 0){
      drumStepper.step(1);
  }
  drumStepper.step(150);
  }  
}

void loop() {
  /*
  char comm;
  if (Serial.available()>0 ){
    comm = Serial.read();
    Serial.println(comm);
  }

  if(comm == 'a'){
    rotateDrum();
  }

  if(comm == 'd'){
    rotateDrum();
  }

  if(comm == 's'){
    digitalWrite(MQon,HIGH);
    Serial.println(analogRead(MQ2));
    Serial.println(analogRead(MQ4));
    Serial.println(analogRead(MQ5));
    Serial.println(analogRead(MQ8));
    digitalWrite(MQon,LOW);
  }*/

  sendTelemetry();

  if (Serial.available()) {
    String packetBuffer; 
    packetBuffer = Serial.readString();
    StaticJsonDocument<255> command;
    deserializeJson(command, packetBuffer);
    if(command["eventType"] == "science"){
      int drillPWM = command["payload"]["drill"];
      drill(drillPWM);
      int elevPWM = command["payload"]["elev"];
      elev(elevPWM);
      int conv = command["payload"]["conv"];
      conveyor(conv);

      if(command["payload"]["res_seq"] == 1){
        if(currSample < 6){
          drill(0);
          elev(0);
          conveyor(0);
          sendTelemetry();
          currSample += 1;
          researchSequence(samples[currSample-1]);
        }
      }
    }
    
  }
  delay(10);
}
