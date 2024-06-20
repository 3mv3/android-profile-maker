/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useState } from 'react';
import type {PropsWithChildren} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  Dimensions,
  PixelRatio,
  NativeModules,
  useWindowDimensions
} from 'react-native';

const { DeviceMetricsModule } = NativeModules

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import * as deviceInfo from 'react-native-device-info'

type SectionProps = PropsWithChildren<{
  title: string;
}>;

function Section({children, title}: SectionProps): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}>
        {children}
      </Text>
    </View>
  );
}

type Camera = {
  location: string,
  autofocus: Boolean,
  flash: Boolean
}

type Unit = {
  unit: string,
  value: number
}

type Hardware = {
  screen: Screen
  networking: string[],
  sensors: string[],
  mic: Boolean,
  cameras: Camera[],
  keyboard: string,
  nav: string,
  ram: Unit,
  buttons: string,
  internalStorage: Unit,
  removableStorage: Unit,
  cpu: string,
  gpu: string,
  abi: string[],
  dock: unknown,
  powerType: string // battery,
  skin: string //_no_skin
}

type State = {
  default: Boolean,
  name: string,
  description: string,
  screenOrientation: string,
  keyboardState: string,
  navState: string
}

type Software = {
  apiLevel: string // range 27-27 or just -
  liveWallpaperSupport: Boolean,
  bluetoothProfiles: unknown,
  glVersion: number,
  glExtensions?: string[],
  statusBar: Boolean
}

type Device = {
  name: string,
  id: string,
  manufacturer: string,
  hardware: Hardware,
  software: Software,
  states: State[]
}

type Dimension = {
  xDimension: number,
  yDimension: number
}

type TouchSettings = {
  multitouch: string,
  mechanism: string,
  screenType: string
}

type Screen = {
  screenSize: string, // can be defaulted to normal
  diagonalLength: number,
  pixelDensity: string,
  screenRatio: string
  dimensions: Dimension,
  xdpi: number,
  ydpi: number,
  touch: TouchSettings,
  realSizeX: number,
  realSizeY: number,
  xInches: number,
  yInches: number,
  scaledDensity: number,
  displayMetricsX: number,
  displayMetricsY: number,
  stableDensity: number,
  apiLevle: number
}

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const [vals, setVals] = useState<Device>()
  const {height, width} = useWindowDimensions();

  useEffect(() => {
    // https://developer.android.com/training/multiscreen/screendensities
    function dpiToVerbose(cur: number) {
      if (cur <= 120) {
        return "ldpi"
      }
      else if (cur <= 160) {
        return "mdpi"
      }
      else if (cur <= 240) {
        return "hdpi"
      }
      else if (cur <= 320) {

      }
    }

    async function assignValues() {
      let info: Device = {
        name: deviceInfo.getBrand(),
        manufacturer: await deviceInfo.getManufacturer(),
        hardware: {

        },
        software: {

        }
      }

      info.hardware.screen = await DeviceMetricsModule.pullDeviceScreenInfo()

      // deviceInfo.hasSystemFeature('amazon.hardware.fire_tv').then((hasFeature) => {
      //   // true or false
      // });

      deviceInfo.getSystemAvailableFeatures().then((features) => {
        if (!info.hardware.networking) {
          info.hardware.networking = []
        }

        if (features.includes('android.hardware.bluetooth')) {
          info.hardware.networking.push('Bluetooth')
        }

        if (features.includes('android.hardware.wifi')) {
          info.hardware.networking.push('WiFi')
        }

        if (features.includes('android.hardware.nfc')) {
          info.hardware.networking.push('NFC')
        }

        // sensors
        if (!info.hardware.sensors) {
          info.hardware.sensors = []
        }

        if (features.includes('android.hardware.sensor.accelerometer')) {
          info.hardware.sensors.push('Accelerometer')
        }

        if (features.includes('android.hardware.sensor.barometer')) {
          info.hardware.sensors.push('Barometer')
        }

        if (features.includes('android.hardware.sensor.compass')) {
          info.hardware.sensors.push('Compass')
        }

        if (features.includes('android.hardware.sensor.gyroscope')) {
          info.hardware.sensors.push('Gyroscope')
        }

        if (features.includes('android.hardware.sensor.light')) {
          info.hardware.sensors.push('LightSensor')
        }

        if (features.includes('android.hardware.sensor.proximity')) {
          info.hardware.sensors.push('ProximitySensor')
        }

        if (features.includes('android.hardware.microphone')) {
          info.hardware.mic = true
        }
      });

      info.hardware.cameras = await DeviceMetricsModule.pullCameraInfo()

      let providers = await deviceInfo.getAvailableLocationProviders()

      if (providers['gps']) {
        info.hardware.sensors.push('GPS')
      }

      info.hardware.keyboard = await DeviceMetricsModule.pullKeyboardConfig()

      info.hardware.nav = await DeviceMetricsModule.pullNavigationConfig()

      // Coming back as 17 gib :shrug:
      let totalMem = await deviceInfo.getTotalMemory();
      let { advertisedMem } = await DeviceMetricsModule.pullAdvertisedMemory()
      console.log(`MEM STRING: ${advertisedMem} // TOT: ${totalMem / 1024 / 1024 / 1024}`)

      let kb = Number.parseFloat(advertisedMem) / 1024
      let mb = kb / 1024
      let gb = mb / 1024

      console.log(`KB: ${kb} => MB: ${mb} => GB: ${gb}`)

      if (gb > 1) {
        info.hardware.ram = {
          unit: "GiB",
          value: Number.parseFloat(advertisedMem) / 1000000000
        }
      }
      else if (mb > 1) {
        info.hardware.ram = {
          unit: "MiB",
          value: Math.ceil(mb)
        }
      }
      else {
        info.hardware.ram = {
          unit: "KiB",
          value: Math.ceil(kb)
        }
      }

      info.hardware.abi = await deviceInfo.supportedAbis();

      let ck65 = {
        ram: 2,
        minApi: 10,
        maxApi: 14,
        screen: 4,
        xPix: 480,
        yPix: 800
      }

      let rt10A = {
        ram: 4,
        minApi: 10,
        maxApi: 14,
        screen: 10.1,
        xPix: 1920,
        yPix: 1200
      }

      console.log(`JSON: ${JSON.stringify(info.hardware)}`)

      let calc = 160 * PixelRatio.get()

      console.log(`PIXEL: ${calc}`)
      let h = Dimensions.get('screen').height
      let w = Dimensions.get('screen').width
      let s = Dimensions.get('screen').scale
      console.log(`H: ${height} / W: ${width}`)
      console.log(`DH: ${h} 
        / DW: ${w} 
        / SCALE: ${s}
        / *: ${h * s}`)

        setVals(info);
    }

    assignValues()
    
  }, [])

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <Section title="Hardware">
            Edit <Text style={styles.highlight}>App.tsx</Text> to change this
            screen and then come back to see your edits.
          </Section>
          <Section title="Software">
            <ReloadInstructions />
          </Section>
          <Section title="Debug">
            <DebugInstructions />
          </Section>
          <Section title="Learn More">
            Read the docs to discover what to do next:
          </Section>
          <LearnMoreLinks />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
