/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.gcp.storage;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * Google Cloud Storage Configuration
 * 
 * @author LangEngine Team
 */
public class GcpStorageConfiguration {

    /**
     * GCP Project ID
     */
    public static String GCP_PROJECT_ID = WorkPropertiesUtils.get("gcp_project_id");
    
    /**
     * GCP Service Account Credentials JSON file path
     * Can be set via environment variable GOOGLE_APPLICATION_CREDENTIALS
     */
    public static String GCP_CREDENTIALS_PATH = WorkPropertiesUtils.get("gcp_credentials_path");
    
    /**
     * Default bucket name
     */
    public static String GCP_DEFAULT_BUCKET = WorkPropertiesUtils.get("gcp_default_bucket");
    
    /**
     * Request timeout in seconds
     */
    public static int GCP_REQUEST_TIMEOUT = WorkPropertiesUtils.getInt("gcp_request_timeout", 60);
}
