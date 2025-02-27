/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Client which retrieves validation configurations.
 */
public class ValidationConfigurationRetrievalClient {

    private static final Log log = LogFactory.getLog(ConfiguredAuthenticatorsRetrievalClient.class);
    private static final String VALIDATION_MGT_API_PATH = "/api/server/v1/validation-rules";
    private static final String CLIENT = "Client ";
    private static final String MIN_LENGTH = "min.length";
    private static final String MAX_LENGTH = "max.length";
    private static final String MIN_UNIQUE_CHR = "min.unique.character";
    private static final String MAX_CONSECUTIVE_CHR = "max.consecutive.character";
    private static final String MAX_LENGTH_KEY = "maxLength";
    private static final String MIN_LENGTH_KEY = "minLength";
    private static final String MIN_NUMBER_KEY = "minNumber";
    private static final String MIN_UPPER_CASE_KEY = "minUpperCase";
    private static final String MIN_LOWER_CASE_KEY = "minLowerCase";
    private static final String MIN_SPECIAL_KEY = "minSpecialChr";
    private static final String MIN_UNIQUE_KEY = "minUniqueChr";
    private static final String MAX_REPEATED_KEY = "maxConsecutiveChr";
    private static final String PROPERTIES = "properties";

    /**
     * Get validation configurations.
     *
     * @param tenantDomain  tenant domain.
     * @return  configurations.
     * @throws ValidationConfigurationRetrievalClientException If an error occurred in retrieving the configurations.
     */
    public JSONArray getConfigurations(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet request = new HttpGet(getValidationMgtEndpoint(tenantDomain));
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    return jsonResponse;
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting validation configurations for tenant: " + tenantDomain;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ValidationConfigurationRetrievalClientException(msg, e);
        }
        return null;
    }

    /**
     * Method to get password configuration.
     *
     * @param tenantDomain  tenant domain.
     * @return  json object of password configuration.
     * @throws ValidationConfigurationRetrievalClientException If an error occurred in getting password configuration.
     */
    public JSONObject getPasswordConfiguration(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        JSONObject passwordConfig = new JSONObject();
        JSONArray configurations = getConfigurations(tenantDomain);

        if (configurations != null && configurations.length() > 0) {
            for (int i = 0; i < configurations.length(); i++) {
                JSONObject config = (JSONObject) configurations.get(i);
                if (((String)config.get("field")).equalsIgnoreCase("password")) {
                    JSONArray rules = (JSONArray) config.get("rules");
                    for (int j = 0; j < rules.length(); j++) {
                        JSONObject rule = rules.getJSONObject(j);
                        String name = (String)rule.get("validator");
                        if (name.equalsIgnoreCase("LengthValidator")) {
                            addValue(MIN_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MIN_LENGTH_KEY);
                            addValue(MAX_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MAX_LENGTH_KEY);
                        } else if (name.equalsIgnoreCase("NumeralValidator")) {
                            addValue(MIN_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MIN_NUMBER_KEY);
                        } else if (name.equalsIgnoreCase("LowerCaseValidator")) {
                            addValue(MIN_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MIN_LOWER_CASE_KEY);
                        } else if (name.equalsIgnoreCase("UpperCaseValidator")) {
                            addValue(MIN_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MIN_UPPER_CASE_KEY);
                        } else if (name.equalsIgnoreCase("SpecialCharacterValidator")) {
                            addValue(MIN_LENGTH, (JSONArray) rule.get(PROPERTIES), passwordConfig,
                                    MIN_SPECIAL_KEY);
                        } else if (name.equalsIgnoreCase("UniqueCharacterValidator")) {
                            addValue(MIN_UNIQUE_CHR, (JSONArray) rule.get(PROPERTIES),
                                    passwordConfig, MIN_UNIQUE_KEY);
                        } else if (name.equalsIgnoreCase("RepeatedCharacterValidator")) {
                            addValue(MAX_CONSECUTIVE_CHR, (JSONArray) rule.get(PROPERTIES),
                                    passwordConfig, MAX_REPEATED_KEY);
                        }
                    }
                }
            }
        }
        if (passwordConfig.length() == 0) {
            passwordConfig.put(MIN_LENGTH_KEY, 8);
            passwordConfig.put(MAX_LENGTH_KEY, 30);
            passwordConfig.put(MIN_NUMBER_KEY, 1);
            passwordConfig.put(MIN_UPPER_CASE_KEY, 1);
            passwordConfig.put(MIN_LOWER_CASE_KEY, 1);
            passwordConfig.put(MIN_SPECIAL_KEY, 1);
        }
        return passwordConfig;
    }

    private void addValue(String propertyName, JSONArray properties, JSONObject response, String key) {

        for(int i = 0; i < properties.length(); i++) {
            JSONObject property = properties.getJSONObject(i);
            if (((String)property.get("key")).equalsIgnoreCase(propertyName)) {
                response.put(key, Integer.parseInt((String) property.get("value")));
            }
        }
        return;
    }

    private String getValidationMgtEndpoint(String tenantDomain)
            throws ValidationConfigurationRetrievalClientException {

        return getEndpoint(tenantDomain, VALIDATION_MGT_API_PATH);
    }

    private String getEndpoint(String tenantDomain, String context)
            throws ValidationConfigurationRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new ValidationConfigurationRetrievalClientException("Error while building url for context: " +
                    context);
        }
    }

    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
