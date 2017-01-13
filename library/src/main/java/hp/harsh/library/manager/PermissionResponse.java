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

package hp.harsh.library.manager;

/**
 * Abstract class is used to store request code. Request code is stored from PermissionRequest class when permission is granted by user.
 * This request code is returned in onGranted() of your calling class.
 */

public class PermissionResponse {

    public int type;

    public PermissionResponse(int type) {
        this.type = type;
    }
}
