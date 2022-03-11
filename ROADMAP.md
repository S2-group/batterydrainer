

# BatteryDrainer Roadmap

Currently, BatteryDrainer could make use of the following additional features.

## Draining features

* Enable the vibration motor.
* Enable the flashlight.
* NFC - Repeatedly ask the NFC circuit to read data.
* Bluetooth - Constantly perform [bluetooth scans](https://developer.android.com/guide/topics/connectivity/bluetooth/find-bluetooth-devices) as it is the most power consumptive operation (classic bluetooth and not BLE).
* Acquire a [`WAKE_LOCK`](https://developer.android.com/reference/android/Manifest.permission#WAKE_LOCK) to prevent the screen from dimming and [keep the device awake](https://developer.android.com/training/scheduling/wakelock).
* Baseband processor - Any way to make the baseband processor and digital signal processor active.
* Sound - Produce high frequency inaudible sounds (beyond hearing spectrum) to make the speaker consume energy.

## Visual features

* Display [phone temperature](https://developer.android.com/reference/android/hardware/Sensor#TYPE_AMBIENT_TEMPERATURE).
* Display [battery temperature](https://developer.android.com/reference/android/os/BatteryManager.html#EXTRA_TEMPERATURE).
* Display phone model.
* Add a time-mAH plot, which indicates the battery level.

## Miscellaneous features

* Add a threshold bellow which the app automatically stops to prevent the battery from accidentally dying out (e.g. 10%).

## Unexplored ideas

* Does an I/O bound workload further increase power consumption?
