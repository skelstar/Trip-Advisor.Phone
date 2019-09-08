

#define MOTOR_POLE_PAIRS 7
#define WHEEL_DIAMETER_MM 97
#define MOTOR_PULLEY_TEETH 15
#define WHEEL_PULLEY_TEETH 36 // https://hobbyking.com/en_us/gear-set-with-belt.html


int32_t rotations_to_meters(int32_t rotations)
{
  float gear_ratio = float(WHEEL_PULLEY_TEETH) / float(MOTOR_PULLEY_TEETH);
  return (rotations / MOTOR_POLE_PAIRS / gear_ratio) * WHEEL_DIAMETER_MM * PI / 1000;
}

float getOdometer() {
    int32_t distanceMeters = rotations_to_meters(vesc.get_tachometer(vesc_packet) / 6);
    return distanceMeters / 1000.0;
}

uint8_t notMovingCounts = 0;
float stableVolts = 0.0;

void serviceStableVoltage(bool moving, float volts) {
  if (moving == false && volts == stableVolts) {
    notMovingCounts++;
    if (notMovingCounts == 2) {
      vescdata.stableBatteryVoltage = volts;
      Serial.printf("Stored stableBatteryVoltage: %0.1f\n", vescdata.stableBatteryVoltage);
    }
  }
  else {
    stableVolts = volts;
    notMovingCounts = 0;
  }
}

bool poweringDown() {
  return vescdata.batteryVoltage > 20.0 && vescdata.batteryVoltage < 30.0;
}