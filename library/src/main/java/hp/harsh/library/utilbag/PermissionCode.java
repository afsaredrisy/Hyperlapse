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

public class PermissionCode {

    public static final int CODE_PERMISSION_CAMERA = 0;

    // Required for member of Location group
    public static final int CODE_PERMISSION_FINE_LOCATION = 1;
    public static final int CODE_PERMISSION_COARSE_LOCATION = 2;

    // Required for member of Calendar group
    public static final int CODE_PERMISSION_READ_CALENDAR = 3;
    public static final int CODE_PERMISSION_WRITE_CALENDAR = 4;

    // Required for member of Contacts group
    public static final int CODE_PERMISSION_READ_CONTACT = 5;
    public static final int CODE_PERMISSION_WRITE_CONTACT = 6;
    public static final int CODE_PERMISSION_GET_ACCOUNTS = 7;

    // Required for Microphone
    public static final int CODE_PERMISSION_RECORD_AUDIO = 8;

    // Required for member of Phone group
    public static final int CODE_PERMISSION_READ_PHONE_STATE = 9;
    public static final int CODE_PERMISSION_CALL_PHONE = 10;
    public static final int CODE_PERMISSION_READ_CALL_LOG = 11;
    public static final int CODE_PERMISSION_WRITE_CALL_LOG = 12;
    public static final int CODE_PERMISSION_USE_SIP = 13;
    public static final int CODE_PERMISSION_PROCESS_OUTGOIG_CALL = 14;

    // Required for member of SMS group
    public static final int CODE_PERMISSION_DEND_SMS = 15;
    public static final int CODE_PERMISSION_RECEIVE_SMS = 16;
    public static final int CODE_PERMISSION_READ_SMS = 17;
    public static final int CODE_PERMISSION_RECEIVE_WAP_PUSH = 18;
    public static final int CODE_PERMISSION_RECEIVE_MMS = 19;

    // Required for member of Storage group
    public static final int CODE_PERMISSION_READ_EXTERNAL_STORAGE = 21;
    public static final int CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 22;
}
