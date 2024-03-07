import React from 'react';
import {
  SafeAreaView,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

import {NativeModules} from 'react-native';
import UserData from './userDataInterface';

var LocationServiceModule = NativeModules.LocationService;
const {SharedPreferencesModule} = NativeModules;

const UserId = '19cs123';
const Url = 'http://localhost:1337/visits/distance';
const AuthToken = 'Bearer 1234567890';

const data: UserData = {
  user_id: '65e2eae34ff27561b01d5d4d',
  url: 'http://10.10.10.148:1337/visits/distance',
  geo_fencing_url: 'http://10.10.10.148:1337/visits/setgeofencing',
  auth_token:
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY1OWNjODNjMjhiNjAxMjU0YTljOGJmNSIsInVzZXJuYW1lIjoiOTA5MDkwODA4MCIsImlhdCI6MTcwODUwODU4MywiZXhwIjoxNzQwMDQ0NTgzfQ.eMJSWhwc5l3CywF-_9-gfXJsYFZv_4K-R4ZGuPj6RLE',
  org_id: '659cc83b28b601254a9c8bf4',
  time_interval: 10000,
  is_auto_start: false,
  days: [1, 2, 3, 4, 5, 6],
  start_time: '09:00',
  end_time: '18:34',
  is_attendance_in: true,
  is_attendance_out: false,
  geofencing: [
    {
      customer_id: '001',
      location: {
        lat: 23.8103,
        lng: 90.4125,
      },
      radius: 100,
    },
    {
      customer_id: '002',
      location: {
        lat: 23.8103,
        lng: 90.4125,
      },
      radius: 300,
    },
    {
      customer_id: '003',
      location: {
        lat: 23.8103,
        lng: 90.4125,
      },
      radius: 500,
    },
  ],
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

  async function setData() {
    try {
      SharedPreferencesModule.saveData('key', JSON.stringify(data)).then(
        (message: any) => {
          console.log(message);
        },
      );
    } catch (e) {
      console.log(e);
    }
  }

  async function getData() {
    try {
      SharedPreferencesModule.getData('key').then((value: any) =>
        console.log('Retrieved value:', value),
      );
    } catch (e) {
      console.log(e);
    }
  }

  async function removeData() {
    try {
      SharedPreferencesModule.deleteData('key').then(() => {
        console.log('Data Deleted');
      });
    } catch (e) {
      console.log(e);
    }
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

            <TouchableOpacity
              onPress={setData}
              style={{
                padding: 10,
                backgroundColor: 'gray',
                width: '100%',
                justifyContent: 'center',
                alignItems: 'center',
                borderRadius: 10,
                marginTop: 10,
                marginHorizontal: 10,
              }}>
              <Text style={{color: 'white'}}>Store</Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={getData}
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
              <Text style={{color: 'white'}}>Get</Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={removeData}
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
              <Text style={{color: 'white'}}>Delete</Text>
            </TouchableOpacity>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

export default App;
