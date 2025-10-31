package com.alibaba.langengine.docusign.service;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.docusign.DocuSignConfiguration.DOCUSIGN_ACCESS_TOKEN;
import static com.alibaba.langengine.docusign.DocuSignConfiguration.DOCUSIGN_ACCOUNT_ID;
import static com.alibaba.langengine.docusign.DocuSignConfiguration.DOCUSIGN_BASE_URL;

@Slf4j
@Data
public class DocuSignService {

    private String baseUrl = DOCUSIGN_BASE_URL;
    private String accountId = DOCUSIGN_ACCOUNT_ID;
    private String accessToken = DOCUSIGN_ACCESS_TOKEN;

    public DocuSignService() { }

    public DocuSignService(String baseUrl, String accountId, String accessToken) {
        this.baseUrl = baseUrl;
        this.accountId = accountId;
        this.accessToken = accessToken;
    }

    public String listTemplates() {
        String url = String.format("%s/restapi/v2.1/accounts/%s/templates", baseUrl, accountId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            log.info("DocuSign listTemplates status={}, body={}", status, body);
            if (status >= 200 && status < 300) {
                return body;
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendEnvelopeFromTemplate(String templateId, String email, String name) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes", baseUrl, accountId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "sent");
        body.put("templateId", templateId);
        Map<String, Object> templateRoles = new HashMap<>();
        templateRoles.put("email", email);
        templateRoles.put("name", name);
        templateRoles.put("roleName", "signer");
        body.put("templateRoles", new Object[]{templateRoles});

        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));

        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            log.info("DocuSign sendEnvelope status={}, body={}", status, respBody);
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEnvelopeStatus(String envelopeId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s", baseUrl, accountId, envelopeId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            log.info("DocuSign envelopeStatus status={}, body={}", status, body);
            if (status >= 200 && status < 300) {
                return body;
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String listEnvelopes() {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes", baseUrl, accountId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return body;
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String voidEnvelope(String envelopeId, String reason) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s", baseUrl, accountId, envelopeId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("status", "voided");
        body.put("voidedReason", reason == null ? "" : reason);
        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String listRecipients(String envelopeId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/recipients", baseUrl, accountId, envelopeId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return body;
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String downloadDocument(String envelopeId, String documentId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/documents/%s", baseUrl, accountId, envelopeId, documentId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            byte[] bytes = resp.getEntity() != null ? EntityUtils.toByteArray(resp.getEntity()) : new byte[0];
            if (status >= 200 && status < 300) {
                return java.util.Base64.getEncoder().encodeToString(bytes);
            }
            String body = new String(bytes, StandardCharsets.UTF_8);
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String createTemplate(String name, String subject, String emailBlurb) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/templates", baseUrl, accountId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("emailSubject", subject);
        body.put("emailBlurb", emailBlurb == null ? "" : emailBlurb);
        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String updateRecipient(String envelopeId, String recipientId, String email, String name) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/recipients", baseUrl, accountId, envelopeId);
        HttpPut put = new HttpPut(url);
        put.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> signer = new HashMap<>();
        signer.put("recipientId", recipientId);
        signer.put("email", email);
        signer.put("name", name);
        body.put("signers", new Object[]{signer});
        put.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(put)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String addDocument(String envelopeId, String fileName, String documentBase64) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/documents", baseUrl, accountId, envelopeId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Map<String, Object> doc = new HashMap<>();
        doc.put("documentBase64", documentBase64);
        doc.put("name", fileName);
        doc.put("documentId", "1");
        Map<String, Object> body = new HashMap<>();
        body.put("documents", new Object[]{doc});
        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String healthCheck() {
        String url = String.format("%s/restapi/v2.1/accounts/%s", baseUrl, accountId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return "OK";
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 列出信封中的所有文档
     */
    public String listDocuments(String envelopeId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/documents", baseUrl, accountId, envelopeId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        get.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return body;
            }
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加新收件人到信封
     */
    public String addRecipient(String envelopeId, String email, String name, String recipientType, Integer routingOrder) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/recipients", baseUrl, accountId, envelopeId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", email);
        recipient.put("name", name);
        recipient.put("recipientId", String.valueOf(System.currentTimeMillis()));
        if (routingOrder != null) {
            recipient.put("routingOrder", routingOrder);
        }
        
        Map<String, Object> body = new HashMap<>();
        if ("carbonCopy".equals(recipientType)) {
            body.put("carbonCopies", new Object[]{recipient});
        } else if ("certifiedDelivery".equals(recipientType)) {
            body.put("certifiedDeliveries", new Object[]{recipient});
        } else {
            // default to signer
            body.put("signers", new Object[]{recipient});
        }
        
        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                return respBody;
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取信封的审计追踪（完成证书）
     */
    public String getAuditTrail(String envelopeId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/documents/certificate", baseUrl, accountId, envelopeId);
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(get)) {
            int status = resp.getStatusLine().getStatusCode();
            byte[] bytes = resp.getEntity() != null ? EntityUtils.toByteArray(resp.getEntity()) : new byte[0];
            if (status >= 200 && status < 300) {
                return java.util.Base64.getEncoder().encodeToString(bytes);
            }
            String body = new String(bytes, StandardCharsets.UTF_8);
            throw new DocuSignError(status, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送签署提醒
     */
    public String sendReminder(String envelopeId) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes/%s/notification", baseUrl, accountId, envelopeId);
        HttpPut put = new HttpPut(url);
        put.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        Map<String, Object> body = new HashMap<>();
        body.put("useAccountDefaults", "false");
        Map<String, Object> reminders = new HashMap<>();
        reminders.put("reminderEnabled", "true");
        reminders.put("reminderDelay", "2");
        reminders.put("reminderFrequency", "2");
        body.put("reminders", reminders);
        
        put.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(put)) {
            int status = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            if (status >= 200 && status < 300) {
                Map<String, Object> result = new HashMap<>();
                result.put("envelope_id", envelopeId);
                result.put("status", "reminder_sent");
                result.put("message", "Reminder notification has been sent to recipients");
                return JSON.toJSONString(result);
            }
            throw new DocuSignError(status, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文档创建信封（不使用模板）
     */
    public String createEnvelopeFromDocument(String documentBase64, String documentName, 
                                            String email, String recipientName, 
                                            String emailSubject, String status) {
        String url = String.format("%s/restapi/v2.1/accounts/%s/envelopes", baseUrl, accountId);
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", status == null ? "sent" : status);
        body.put("emailSubject", emailSubject == null ? "Please sign this document" : emailSubject);
        
        // 添加文档
        Map<String, Object> document = new HashMap<>();
        document.put("documentBase64", documentBase64);
        document.put("name", documentName);
        document.put("documentId", "1");
        document.put("fileExtension", documentName.substring(documentName.lastIndexOf('.') + 1));
        body.put("documents", new Object[]{document});
        
        // 添加收件人
        Map<String, Object> signer = new HashMap<>();
        signer.put("email", email);
        signer.put("name", recipientName);
        signer.put("recipientId", "1");
        signer.put("routingOrder", "1");
        
        // 添加签署位置标签
        Map<String, Object> signHere = new HashMap<>();
        signHere.put("anchorString", "/sn1/");
        signHere.put("anchorUnits", "pixels");
        signHere.put("anchorXOffset", "20");
        signHere.put("anchorYOffset", "10");
        
        Map<String, Object> tabs = new HashMap<>();
        tabs.put("signHereTabs", new Object[]{signHere});
        signer.put("tabs", tabs);
        
        Map<String, Object> recipients = new HashMap<>();
        recipients.put("signers", new Object[]{signer});
        body.put("recipients", recipients);
        
        post.setEntity(new StringEntity(JSON.toJSONString(body), StandardCharsets.UTF_8));
        try (RetryingHttpClient client = new RetryingHttpClient(); CloseableHttpResponse resp = client.executeWithRetry(post)) {
            int statusCode = resp.getStatusLine().getStatusCode();
            String respBody = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8) : "";
            log.info("DocuSign createEnvelopeFromDocument status={}, body={}", statusCode, respBody);
            if (statusCode >= 200 && statusCode < 300) {
                return respBody;
            }
            throw new DocuSignError(statusCode, respBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


