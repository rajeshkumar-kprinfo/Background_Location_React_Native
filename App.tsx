import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

import {NativeModules} from 'react-native';
var LocationServiceModule = NativeModules.LocationService;

const UserId = '19cs123';
const Url = 'http://localhost:3000/api/location';
const AuthToken = 'Bearer 1234567890';

const data = {
  userId: '19cs123',
  url: 'http://localhost:3000/api/location',
  auth_token: 'Bearer 123',
  org_id: '123',
  time_interval: 10000,
  is_auto_start: true,
  days: [0, 1, 2, 3, 4, 5, 6],
  start_time: '09:00',
  end_time: '17:00',
};

function App() {
  async function StartBtn() {
    LocationServiceModule.startLocationService(
      UserId,
      Url,
      AuthToken,
      (err: any) => {
        console.log(err);
      },
      (msg: any) => {
        console.log(msg);
      },
    );
  }

  async function StopBtn() {
    LocationServiceModule.stopLocationService(
      (err: any) => {
        console.log(err);
      },
      (msg: any) => {
        console.log(msg);
      },
    );
  }

  return (
    <SafeAreaView
      style={{
        flex: 1,
      }}>
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={{
          flex: 1,
        }}>
        <View
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100%',
            flexDirection: 'column',
            flex: 1,
          }}>
          <View style={{width: '80%', marginVertical: 20}}>
            <Text
              style={{
                textAlign: 'center',
                marginVertical: 30,
              }}>
              Background Location Tracking (BLT)
            </Text>
            <TouchableOpacity
              onPress={StartBtn}
              style={{
                padding: 10,
                backgroundColor: 'blue',
                width: '100%',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 10,
                marginTop: 10,
                marginHorizontal: 10,
              }}>
              <Text style={{color: 'white'}}>Start</Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={StopBtn}
              style={{
                padding: 10,
                backgroundColor: 'red',
                width: '100%',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 10,
                marginTop: 10,
                marginHorizontal: 10,
              }}>
              <Text style={{color: 'white'}}>Stop</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
