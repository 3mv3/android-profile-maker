package com.androidprofilemaker

import android.view.Display
import android.view.WindowManager
import android.graphics.Point
import android.app.Application
import android.app.ActivityManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CameraCharacteristics
import android.content.res.Configuration
import android.hardware.camera2.CameraManager
import android.hardware.Sensor
import android.media.AudioManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.DisplayMetrics
import android.os.Build

import java.math.RoundingMode
import kotlin.math.*

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class DeviceMetricsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) 
{
    private val context: ReactApplicationContext = reactContext

    override fun getName() = "DeviceMetricsModule"

    @ReactMethod
    fun pullAdvertisedMemory(promise: Promise) {
      var activitySerivce = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      var memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
      activitySerivce.getMemoryInfo(memInfo);

      val mem = Arguments.createMap().apply {
        putString("advertisedMem", memInfo.advertisedMem.toString())
    }

      promise.resolve(mem)
    }

    @ReactMethod
    fun pullDeviceScreenInfo(promise: Promise) {

      // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      //     // Do something for lollipop and above versions
      // } else{
      //     // do something for phones running an SDK before lollipop
      // }
    
    val resources = context.getResources()
    var resourceMetrics = resources.getDisplayMetrics()

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

// may be API dependent?
    val sizeFromPoint = Point();
val realDisplay: Display = windowManager.getDefaultDisplay();
realDisplay.getRealSize(sizeFromPoint);

val windowManagertMetrics = DisplayMetrics();
realDisplay.getMetrics(windowManagertMetrics)

realDisplay.getRotation()

    val density  = resourceMetrics.density

    val yPixels = sizeFromPoint.y
    val xPixels = sizeFromPoint.x

    // val statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android")

    // val statusBarHeight = resources.getDimensionPixelSize(statusBarHeightId)

    // yPixels += statusBarHeight

    var xInches = xPixels / resourceMetrics.xdpi
    var yInches = yPixels / resourceMetrics.ydpi

    // not sure if rounding up here will work for every phone, but it's roughly there
    var diagonal = sqrt(xInches.pow(2) + yInches.pow(2)).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()

    var long = "long"
    var ratio = (yPixels.toDouble() / xPixels.toDouble());
    
    if (ratio <= 1.667) {
      long = "notlong"
    }

    val dimensions = Arguments.createMap().apply {
        putInt("xDimension", xPixels)
        putInt("yDimension", yPixels)
    }

// https://javadoc.io/doc/com.facebook.react/react-native/0.20.1/index.html
    val params = Arguments.createMap().apply {
        putString("screenSize", "normal")
        putDouble("diagonalLength", diagonal)
        putString("pixelDensity", resourceMetrics.densityDpi.toString())
        putString("screenRatio", long)
        putMap("dimensions", dimensions)
        putDouble("xdpi", resourceMetrics.xdpi.toDouble())
        putDouble("ydpi", resourceMetrics.ydpi.toDouble())
        putInt("sizeFromPointY", sizeFromPoint.y)
        putInt("sizeFromPointX", sizeFromPoint.x)
        putInt("displayMetricsX", resourceMetrics.widthPixels)
        putInt("displayMetricsY", resourceMetrics.heightPixels)
        putString("xInches", xInches.toString())
        putString("yInches", yInches.toString())
        putString("scaledDensity", resourceMetrics.scaledDensity.toString())
        putString("stableDensity", DisplayMetrics.DENSITY_DEVICE_STABLE.toString())
        putInt("apiLevel", Build.VERSION.SDK_INT)
    }

    promise.resolve(params);
  }

  @ReactMethod
  fun pullKeyboardConfig(promise: Promise) {
    // https://developer.android.com/guide/topics/resources/providing-resources#ImeQualifier
    var config = context.getResources().getConfiguration()

    var keyboard = "nokeys"

    if (config.keyboard == Configuration.KEYBOARD_QWERTY) {
      keyboard = "qwerty"
    }
    
    if (config.keyboard == Configuration.KEYBOARD_12KEY) {
      keyboard = "12key"
    }

    promise.resolve(keyboard)
  }

  @ReactMethod
  fun pullNavigationConfig(promise: Promise) {
    // https://developer.android.com/reference/android/content/res/Configuration#navigation
// https://developer.android.com/guide/topics/resources/providing-resources#ImeQualifier
    var config = context.getResources().getConfiguration()

    var navigation = "nonav"

    if (config.navigation == Configuration.NAVIGATION_DPAD) {
      navigation = "dpad"
    }
    
    if (config.navigation == Configuration.NAVIGATION_TRACKBALL) {
      navigation = "trackball"
    }

    if (config.navigation == Configuration.NAVIGATION_WHEEL) {
      navigation = "wheel"
    }

    promise.resolve(navigation)
  }

  @ReactMethod
    fun pullCameraInfo(promise: Promise) {
    
    var camService = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    var cams = camService.getCameraIdList()

    var cameraLists = mutableListOf<String>()

    // https://javadoc.io/doc/com.facebook.react/react-native/0.20.1/index.html
    val response = Arguments.createArray()

    for (item in cams) {
      Log.i(getName(), "CAM: ${item}")

      var characteristics = camService.getCameraCharacteristics(item) as CameraCharacteristics

      var afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
      var hasAf = false

      if (afModes != null && afModes.size > 0) {
          hasAf = true
      }

      var facing = characteristics.get(CameraCharacteristics.LENS_FACING)

      var location = "back"

      if (facing == CameraMetadata.LENS_FACING_FRONT) {
        location = "front"
      }

      if (facing == CameraMetadata.LENS_FACING_EXTERNAL) {
        location = "external"
      }

      var flash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

      val camera = Arguments.createMap().apply {
            putString("location", location)
            putBoolean("autofocus", hasAf ?: false)
            putBoolean("flash", flash ?: false)
        }

        response.pushMap(camera)
    }    

    promise.resolve(response);
  }

  fun buildProfile() {
    // android.os.Build
    // names
    var model = Build.MODEL;
    var mfg = Build.MANUFACTURER;
    var name = Build.PRODUCT;
    var id = "${mfg}_${name}"

    Log.i(getName(), "Model: ${model}, MFG: ${mfg}, PDCT: ${name}")

    /* >>>>>>>>>SCREEN METRICS <<<<<<<<<<< */ 
    var metrics = context.getResources().getDisplayMetrics()
    var xInches = metrics.widthPixels / metrics.xdpi
    var yInches = metrics.heightPixels / metrics.ydpi

    // not sure if rounding up here will work for every phone, but it's roughly there
    var diagonal = sqrt(xInches.pow(2) + yInches.pow(2)).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()

    var long = "long"
    var ratio = (metrics.heightPixels.toDouble() / metrics.widthPixels.toDouble());
    
    if (ratio <= 1.667) {
      long = "notlong"
    }


    /* >>>>>>>>> PACKAGES + SERVICES <<<<<<<<<<< */ 
    var networking = mutableListOf<String>()
    var sensors = mutableListOf<String>()

    var packageManager = context.getPackageManager()

    var hasMic = false

    if(packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
      hasMic = true
    }

    if(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
      networking.add("BLUETOOTH")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)) {
      networking.add("WiFi")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
      networking.add("NFC")
    }

    // https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
    //var sensorService = context.getSystemService(Context.SENSOR_SERVICE)

//https://developer.android.com/reference/android/content/pm/PackageManager#FEATURE_SENSOR_ACCELEROMETER
    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
      sensors.add("Accelerometer")
    }

//https://developer.android.com/reference/android/content/pm/PackageManager#FEATURE_SENSOR_BAROMETER
    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER)) {
      sensors.add("Barometer")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) {
      sensors.add("Compass")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
      sensors.add("GPS")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
      sensors.add("Gyroscope")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT)) {
      sensors.add("LightSensor")
    }

    if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY)) {
      sensors.add("ProximitySensor")
    }

    var mConMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE)
    var camService = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    var cams = camService.getCameraIdList()

    var cameraLists = mutableListOf<String>()

    for (item in cams) {
      Log.i(getName(), "CAM: ${item}")

      var characteristics = camService.getCameraCharacteristics(item) as CameraCharacteristics

      var afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
      var hasAf = false

      if (afModes != null && afModes.size > 0) {
          hasAf = true
      }

      var facing = characteristics.get(CameraCharacteristics.LENS_FACING)

      var stringVal = "back"

      if (facing == CameraMetadata.LENS_FACING_FRONT) {
        stringVal = "front"
      }

      if (facing == CameraMetadata.LENS_FACING_EXTERNAL) {
        stringVal = "external"
      }

      var flash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

      // build xml for each camera
      var cameraString = """
        <d:camera>
          <d:location>${stringVal}</d:location>
          <d:autofocus>${hasAf}</d:autofocus>
          <d:flash>${flash}</d:flash>
        </d:camera>
      """

      Log.i(getName(), "CAMS: ${cameraString}")


      cameraLists.add(cameraString)
    }

  var allCameras = cameraLists.joinToString(separator = "\r\n")

    var networkingString = networking.joinToString(separator = "\r\n")

    Log.i(getName(), "NETWORKING: ${networkingString}")

    var sensorsString = sensors.joinToString(separator = "\r\n")

    Log.i(getName(), "SENSORS: ${sensorsString}")

// https://developer.android.com/guide/topics/resources/providing-resources#ImeQualifier
    var config = context.getResources().getConfiguration()

    var keyboard = "nokeys"

    if (config.keyboard == Configuration.KEYBOARD_QWERTY) {
      keyboard = "qwerty"
    }
    
    if (config.keyboard == Configuration.KEYBOARD_12KEY) {
      keyboard = "12key"
    }

// https://developer.android.com/reference/android/content/res/Configuration#navigation
// https://developer.android.com/guide/topics/resources/providing-resources#ImeQualifier
    var navigation = "nonav"

    if (config.navigation == Configuration.NAVIGATION_DPAD) {
      navigation = "dpad"
    }
    
    if (config.navigation == Configuration.NAVIGATION_TRACKBALL) {
      navigation = "trackball"
    }

    if (config.navigation == Configuration.NAVIGATION_WHEEL) {
      navigation = "wheel"
    }

    // Memory
    var activitySerivce = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    var memInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
    activitySerivce.getMemoryInfo(memInfo);
    var totRam = memInfo.totalMem;

    var lastValue = ""
    // var mb = totRam / 1024.0;
    //     var gb = totRam / 1048576.0;
    //     var tb = totRam / 1073741824.0;

    //     if (tb > 1) {
    //         lastValue = twoDecimalForm.format(tb).concat(" TB");
    //     } else if (gb > 1) {
    //         lastValue = twoDecimalForm.format(gb).concat(" GB");
    //     } else if (mb > 1) {
    //         lastValue = twoDecimalForm.format(mb).concat(" MB");
    //     } else {
    //         lastValue = twoDecimalForm.format(totRam).concat(" KB");
    //     }

    Log.i(getName(), "MEM: ${lastValue}")

    /*
    https://developer.android.com/guide/topics/resources/providing-resources#ImeQualifier
    Touchscreen type	notouch
finger	
notouch: device doesn't have a touchscreen.
finger: device has a touchscreen that is intended to be used through direction interaction of the user's finger.
Also see the touchscreen configuration field, which indicates the type of touchscreen on the device.
     */

    var template = """
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<d:devices xmlns:d="http://schemas.android.com/sdk/devices/7" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <d:device>
    <d:name>${name}</d:name>
    <d:id>${name.lowercase()}_${mfg.lowercase()}</d:id>
    <d:manufacturer>${mfg}</d:manufacturer>
    <d:meta/>
    <d:hardware>
      <d:screen>
        <d:screen-size>normal</d:screen-size>
        <d:diagonal-length>${diagonal}</d:diagonal-length>
        <d:pixel-density>${metrics.densityDpi}</d:pixel-density>
        <d:screen-ratio>${long}</d:screen-ratio>
        <d:dimensions>
          <d:x-dimension>${metrics.widthPixels}</d:x-dimension>
          <d:y-dimension>${metrics.heightPixels}</d:y-dimension>
        </d:dimensions>
        <d:xdpi>${metrics.xdpi}</d:xdpi>
        <d:ydpi>${metrics.ydpi}</d:ydpi> 
        <d:touch>
          <d:multitouch>jazz-hands</d:multitouch>
          <d:mechanism>finger</d:mechanism>
          <d:screen-type>capacitive</d:screen-type>
        </d:touch>
      </d:screen>
      <d:networking>
      ${networkingString}
      </d:networking>
      <d:sensors>
      ${sensorsString}
      </d:sensors>
      <d:mic>${hasMic}</d:mic>
      ${allCameras}
      <d:keyboard>${keyboard}</d:keyboard>
      <d:nav>${navigation}</d:nav>
      <d:ram unit="GiB">2</d:ram>
      <d:buttons>soft</d:buttons>
      <d:internal-storage unit="GiB">
4</d:internal-storage>
      <d:removable-storage unit="TiB"/>
      <d:cpu>Generic CPU</d:cpu>
      <d:gpu>Generic GPU</d:gpu>
      <d:abi>
armeabi
armeabi-v7a
arm64-v8a
x86
x86_64
mips
mips64</d:abi>
      <d:dock/>
      <d:power-type>battery</d:power-type>
      <d:skin>_no_skin</d:skin>
    </d:hardware>
    <d:software>
      <d:api-level>-</d:api-level>
      <d:live-wallpaper-support>true</d:live-wallpaper-support>
      <d:bluetooth-profiles/>
      <d:gl-version>2.0</d:gl-version>
      <d:gl-extensions/>
      <d:status-bar>false</d:status-bar>
    </d:software>
    <d:state default="true" name="Portrait">
      <d:description>The device in portrait orientation</d:description>
      <d:screen-orientation>port</d:screen-orientation>
      <d:keyboard-state>keyssoft</d:keyboard-state>
      <d:nav-state>navhidden</d:nav-state>
    </d:state>
    <d:state name="Landscape">
      <d:description>The device in landscape orientation</d:description>
      <d:screen-orientation>land</d:screen-orientation>
      <d:keyboard-state>keyssoft</d:keyboard-state>
      <d:nav-state>navhidden</d:nav-state>
    </d:state>
  </d:device>
</d:devices>
"""
  }
}