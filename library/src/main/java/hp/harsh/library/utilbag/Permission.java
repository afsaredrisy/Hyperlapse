/*
* Copyright 2016 Harsh Patel
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package hp.harsh.library.utilbag;

public class Permission {

    // Required for Camera
    public static final String[] PERMISSION_CAMERA = new String[]{"android.permission.CAMERA"};

    // Required for member of Location group
    public static final String[] PERMISSION_FINE_LOCATION = new String[]{"android.permission.ACCESS_FINE_LOCATION"};
    public static final String[] PERMISSION_COARSE_LOCATION = new String[]{"android.permission.ACCESS_COARSE_LOCATION"};

    // Required for member of Calender group
    public static final String[] PERMISSION_READ_CALENDER = new String[]{"android.permission.READ_CALENDAR"};
    public static final String[] PERMISSION_WRITE_CALENDER = new String[]{"android.permission.WRITE_CALENDAR"};

    // Required for member of Contacts group
    public static final String[] PERMISSION_READ_CONTACT = new String[]{"android.permission.READ_CONTACTS"};
    public static final String[] PERMISSION_WRITE_CONTACT = new String[]{"android.permission.WRITE_CALENDAR"};
    public static final String[] PERMISSION_GET_ACCOUNTS = new String[]{"android.permission.GET_ACCOUNTS"};

    // Required for Microphone
    public static final String[] PERMISSION_RECORD_AUDIO = new String[]{"android.permission.RECORD_AUDIO"};

    // Required for member of Phone group
    public static final String[] PERMISSION_READ_PHONE_STATE = new String[]{"android.permission.READ_PHONE_STATE"};
    public static final String[] PERMISSION_CALL_PHONE = new String[]{"android.permission.CALL_PHONE"};
    public static final String[] PERMISSION_READ_CALL_LOG = new String[]{"android.permission.READ_CALL_LOG"};
    public static final String[] PERMISSION_WRITE_CALL_LOG = new String[]{"android.permission.WRITE_CALL_LOG"};
    public static final String[] PERMISSION_USE_SIP = new String[]{"android.permission.USE_SIP"};
    public static final String[] PERMISSION_PROCESS_OUTGOIG_CALL = new String[]{"android.permission.PROCESS_OUTGOING_CALLS"};

    // Required for member of SMS group
    public static final String[] PERMISSION_DEND_SMS = new String[]{"android.permission.SEND_SMS"};
    public static final String[] PERMISSION_RECEIVE_SMS = new String[]{"android.permission.RECEIVE_SMS"};
    public static final String[] PERMISSION_READ_SMS = new String[]{"android.permission.READ_SMS"};
    public static final String[] PERMISSION_RECEIVE_WAP_PUSH = new String[]{"android.permission.RECEIVE_WAP_PUSH"};
    public static final String[] PERMISSION_RECEIVE_MMS = new String[]{"android.permission.RECEIVE_MMS"};

    // Required for member of Storage group
    public static final String[] PERMISSION_READ_EXTERNAL_STORAGE = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
    public static final String[] PERMISSION_WRITE_EXTERNAL_STORAGE = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
}
