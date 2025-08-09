#include <Arduino.h>
#include <ArduinoJson.h>
#include "arm_setup.h"
#include "servo_class.h"
#include "motor_class.h"

#define CAPACITY 400
//1-base
//2-arm
//3-elbow
//4-wrist
//5-rotate
//6-gripper
//7-shovel

ServoDriver rotate(SERVO_1,0,SENS_SERVO_1,SERVO_ENABLE_1,1);
ServoDriver wrist(SERVO_2,0,SENS_SERVO_2,SERVO_ENABLE_2,1);;
ServoDriver shovel(SERVO_3,0,SENS_SERVO_3,SERVO_ENABLE_3,1);;
MotorDriver base(IN_A_A_1, IN_A_A_2, ENABLE_A_A, SENS_A_A,1);
MotorDriver arm(IN_A_B_1, IN_A_B_2, ENABLE_A_B, SENS_A_B,1);;
MotorDriver elbow(IN_B_A_1, IN_B_A_2, ENABLE_B_A, SENS_B_A,1);
MotorDriver gripper(IN_B_B_1, IN_B_B_2, ENABLE_B_B, SENS_B_B,1);


void executePWM(const StaticJsonDocument<CAPACITY>& doc)
{
    int baseData=   doc["payload"]["rotate_turret"];
    int armData=    doc["payload"]["flex_arm"];
    int elbowData=  doc["payload"]["flex_forearm"];
    int wristData=  doc["payload"]["flex_gripper"];
    int rotateData= doc["payload"]["rotate_gripper"];
    int gripperData=doc["payload"]["end_effector"];
    int shovelData= doc["payload"]["end_effector"];

    base.setSpeed(baseData);
    arm.setSpeed(armData);
    elbow.setSpeed(elbowData);
    wrist.setSpeed(wristData);
    rotate.setSpeed(rotateData);
    gripper.setSpeed(gripperData);
    shovel.setSpeed(shovelData);
}

void runMotors()
{
    Serial.print(" inside run motors ");
    base.update();
    arm.update();
    elbow.update();
    wrist.update();
    rotate.update();
    gripper.update();
    shovel.update();

}