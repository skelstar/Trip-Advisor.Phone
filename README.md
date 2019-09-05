# TripAdvisor

Writing an Android app (TripAdvsior.Phone) that will connect to a BLE Server that runs on an ESP32 (TripAdvisor.Server). The ESP32 will talk to a VESC motor controller. The idea is that when you disconnect the power to the e-skateboard, the ESP32 will send a notification to the phone which will popup a notification letting the user know some stats like trip-distance, amp-hours used, battery voltage etc. 

Later features will include accumlation of trip kms with the ability to zero, or have the ESP32 zero the trip meter when the battery is charged.

[Android sample for Notifications](https://github.com/googlesamples/android-NotificationChannels/blob/master/kotlinApp/Application/src/main/java/com/example/android/notificationchannels/MainActivity.kt)

[Good YT series for Notification](https://www.youtube.com/watch?v=FH7DF-qDKcc&list=PLk7v1Z2rk4hjM2NPKqtWQ_ndCuoqUj5Hh&index=8)

[https://www.javatpoint.com/kotlin-android-toast](https://www.javatpoint.com/kotlin-android-toast)

[ble code](https://github.com/appsinthesky/Kotlin-Bluetooth/blob/ae814bfa1769326445b5b6e15d5218767c9cd474/app/src/main/res/layout/select_device_layout.xml)

https://github.com/skelstar/Esk8Monitor

[Android Kotlin BLE Client](https://github.com/chenineazeddine/Android-BLE-GATT-Client/blob/5a4edaecf2360edbf5021c077a709812400ecf69/app/src/main/java/com/cerist/summer/blelightswitcher/BlueToothActivity.kt#L100)

##Road Map
- ESP32 sends notification on button press (M5Stack/M5Stick?)
- Test notifications when device locked etc
- Decide on metrics
  - distance
  - AHs consumed
  - average AHs/minute
- Show stats on app screen
- Some kind of "Sessions" concept
