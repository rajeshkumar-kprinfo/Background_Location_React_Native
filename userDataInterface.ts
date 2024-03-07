interface GeoLocation {
  lat: number;
  lng: number;
}

interface GeoFencing {
  customer_id: string;
  location: GeoLocation;
  radius: number;
}

export default interface UserData {
  user_id: string;
  url: string;
  geo_fencing_url: string;
  auth_token: string;
  org_id: string;
  time_interval: number;
  is_auto_start: boolean;
  days: number[];
  start_time: string;
  end_time: string;
  is_attendance_in: boolean;
  is_attendance_out: boolean;
  geofencing: GeoFencing[];
}
