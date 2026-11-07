# MockLocationDetector
An android library to help detect mock locations

# Gradle
Add the next dependency to your _gradle.build_

    compile 'com.inforoeste.mocklocationdetector:mock-location-detector:1.0.0'

# Usage
From anywhere on your code just call the class MockLocationDetector to access the following available static methods

1. `isLocationFromMockProvider`
2. `checkForAllowMockLocationsApps`
3. `isAllowMockLocationsOn`

To detect if the location object you received is a mock just call the method `isLocationFromMockProvider` and pass
the context and the location object. This method only works on API 18+ if this method is called on lower apis it then
it will check if Allow Mock Locations is ON or not and return the result. This is the recomended method for most cases.

If you want to check if there are apps on the users phone that have "ALLOW_MOCK_LOCATIONS" permission on their
manifest just call `checkForAllowMockLocationsApps`. This method is usefull for apps that use root access to enable/disable
allow mock locations at runtime to try to fool other apps on lower apis (API 17 or lower). Also starting on Marshmallow its
no longer possible to enable/disable allow mock locations at runtime with root permissions.

Finally you can call `isAllowMockLocationsOn` to check if the system setting Allow Mock Locations is enabled or not. Be aware
that this method is deprecated and doesnt work on Marshmallow or up. If you call this method on API 23+ it will always return
false.

# Sample
You can run the sample to test the library

## Mock location simulator
The sample can also simulate a location so you can check that the detector flags it.
The coordinates default to Nguyen Tat Thanh University (10.7610342, 106.7103536); you can type any other values.

1. Install the sample on the phone.
2. Developer options > Select mock location app > choose "Mock Location Detector Sample".
3. Open the sample, check the coordinates (or tap "Nguyen Tat Thanh University") and tap "Start mock".
4. "Is Location Mock" should turn `true` within a few seconds. Tap "Stop mock" to end the simulation.

The simulator pushes the location every second to the GPS test provider and to the fused location provider.

With adb (an emulator only): `adb emu geo fix 106.7103536 10.7610342` (longitude first).

# License
    Copyright 2016 Stéphane Marques

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
