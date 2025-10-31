# Ali-LangEngine-DocuSign

DocuSign electronic signature tool calling module for Ali-LangEngine framework.

## Overview

This module provides comprehensive DocuSign eSignature API integration for the Ali-LangEngine framework, enabling AI agents to automate document signing workflows, manage envelopes, templates, recipients, and handle the complete document lifecycle.

## Features

### Core Tools (17 tools)

#### Template Management
- ✅ **DocuSignListTemplatesTool** - List all available DocuSign templates
- ✅ **DocuSignCreateTemplateTool** - Create new document templates

#### Envelope Operations
- ✅ **DocuSignSendEnvelopeTool** - Send envelopes from templates
- ✅ **DocuSignCreateEnvelopeFromDocumentTool** - Create envelopes from documents (without template)
- ✅ **DocuSignGetEnvelopeStatusTool** - Check envelope status
- ✅ **DocuSignListEnvelopesTool** - List all envelopes
- ✅ **DocuSignVoidEnvelopeTool** - Cancel/void envelopes
- ✅ **DocuSignBulkSendTool** - Send envelopes to multiple recipients

#### Recipient Management
- ✅ **DocuSignListRecipientsTool** - List recipients of an envelope
- ✅ **DocuSignAddRecipientTool** - Add new recipients to envelopes
- ✅ **DocuSignUpdateRecipientTool** - Update recipient information
- ✅ **DocuSignSendReminderTool** - Send signing reminders

#### Document Management
- ✅ **DocuSignListDocumentsTool** - List documents in an envelope
- ✅ **DocuSignDownloadDocumentTool** - Download signed documents
- ✅ **DocuSignAddDocumentTool** - Add documents to envelopes
- ✅ **DocuSignGetAuditTrailTool** - Get certificate of completion

#### System Tools
- ✅ **DocuSignHealthCheckTool** - Verify API connectivity

### Advanced Features
- 🔄 Automatic retry with exponential backoff
- 📝 Comprehensive logging and error handling
- 🔐 Multiple authentication methods support
- 🎯 Batch operations for bulk processing
- 📊 Detailed audit trail and document tracking

## Installation

### Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-docusign</artifactId>
    <version>1.2.6-202508111516</version>
</dependency>
```

## Prerequisites

1. **DocuSign Account**: You need a DocuSign developer or production account
2. **API Access**: Enable API access in your DocuSign account settings
3. **Access Token**: Obtain an access token via OAuth 2.0 or JWT

### Getting DocuSign Credentials

1. **Sign up for DocuSign Developer Account**
   - Visit: https://developers.docusign.com/
   - Create a free developer account

2. **Create an Integration Key (Client ID)**
   - Go to Admin → Apps and Keys
   - Click "Add App and Integration Key"
   - Note your Integration Key

3. **Generate Access Token**
   - For development: Use the "Generate Token" feature
   - For production: Implement OAuth 2.0 or JWT authentication

4. **Get Account ID**
   - Available in your DocuSign account settings
   - Format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`

## Configuration

### Environment Variables

```bash
# Required
export docusign_base_url="https://demo.docusign.net"  # or https://www.docusign.net for production
export docusign_account_id="your-account-id-here"
export docusign_access_token="your-access-token-here"

# Optional
export docusign_request_timeout="30000"  # milliseconds
export docusign_max_retries="3"
export docusign_retry_interval="1000"  # milliseconds
export docusign_webhook_enabled="false"
export docusign_webhook_url="https://your-domain.com/webhook"
export docusign_default_email_subject="Please sign this document"
export docusign_default_email_blurb="Please review and sign the document."
export docusign_enable_logging="true"
```

### Properties File

Create `application.properties` or `config.properties`:

```properties
docusign_base_url=https://demo.docusign.net
docusign_account_id=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
docusign_access_token=eyJ0eXAiOiJNVCIsImFs...
docusign_request_timeout=30000
docusign_max_retries=3
docusign_retry_interval=1000
```

## Quick Start

### Example 1: Basic Document Signing

```java
import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.docusign.tool.*;

import java.util.Arrays;
import java.util.List;

public class QuickStart {
    public static void main(String[] args) {
        // Create DocuSign tools
        List<BaseTool> tools = Arrays.asList(
            new DocuSignListTemplatesTool(),
            new DocuSignSendEnvelopeTool(),
            new DocuSignGetEnvelopeStatusTool()
        );

        // Create agent with tools
        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        // Create executor
        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        // Execute task
        String result = executor.run(
            "List DocuSign templates, then send the first template to john@example.com"
        );
        
        System.out.println(result);
    }
}
```

### Example 2: Direct Tool Usage

```java
import com.alibaba.langengine.docusign.tool.DocuSignSendEnvelopeTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.core.callback.ExecutionContext;

public class DirectUsage {
    public static void main(String[] args) {
        DocuSignSendEnvelopeTool tool = new DocuSignSendEnvelopeTool();
        
        String input = "{\n" +
            "  \"templateId\": \"template-123\",\n" +
            "  \"email\": \"signer@example.com\",\n" +
            "  \"name\": \"John Signer\"\n" +
            "}";
        
        ToolExecuteResult result = tool.run(input, new ExecutionContext());
        System.out.println("Result: " + result.getOutput());
    }
}
```

## Tool Reference

### 1. DocuSignListTemplatesTool

List all available templates in your DocuSign account.

**Name**: `DocuSign.list_templates`

**Parameters**: None

**Example Output**:
```json
{
  "envelopeTemplates": [
    {
      "templateId": "template-123",
      "name": "Sales Contract",
      "description": "Standard sales agreement template"
    }
  ]
}
```

### 2. DocuSignSendEnvelopeTool

Send an envelope using a template.

**Name**: `DocuSign.send_envelope`

**Parameters**:
- `templateId` (string, required): Template ID
- `email` (string, required): Recipient email
- `name` (string, required): Recipient name

**Example**:
```json
{
  "templateId": "template-123",
  "email": "signer@example.com",
  "name": "John Doe"
}
```

### 3. DocuSignBulkSendTool

Send envelopes to multiple recipients at once.

**Name**: `DocuSign.bulk_send`

**Parameters**:
- `template_id` (string, required): Template ID
- `recipients` (array, required): Array of {email, name} objects

**Example**:
```json
{
  "template_id": "template-123",
  "recipients": [
    {"email": "alice@company.com", "name": "Alice Smith"},
    {"email": "bob@company.com", "name": "Bob Johnson"}
  ]
}
```

### 4. DocuSignCreateEnvelopeFromDocumentTool

Create envelope from a Base64-encoded document.

**Name**: `DocuSign.create_envelope_from_document`

**Parameters**:
- `document_base64` (string, required): Base64 encoded document
- `document_name` (string, required): File name
- `email` (string, required): Recipient email
- `recipient_name` (string, required): Recipient name
- `email_subject` (string, optional): Email subject
- `status` (string, optional): "sent" or "created" (default: "sent")

### 5. DocuSignGetEnvelopeStatusTool

Check the status of an envelope.

**Name**: `DocuSign.get_envelope_status`

**Parameters**:
- `envelope_id` (string, required): Envelope ID

**Example Output**:
```json
{
  "envelopeId": "envelope-123",
  "status": "sent",
  "sentDateTime": "2025-10-05T10:30:00Z",
  "statusChangedDateTime": "2025-10-05T10:30:00Z"
}
```

### 6. DocuSignListEnvelopesTool

List all envelopes in your account.

**Name**: `DocuSign.list_envelopes`

**Parameters**: None

### 7. DocuSignVoidEnvelopeTool

Cancel/void an envelope.

**Name**: `DocuSign.void_envelope`

**Parameters**:
- `envelope_id` (string, required): Envelope ID
- `reason` (string, optional): Reason for voiding

### 8. DocuSignListRecipientsTool

List all recipients of an envelope.

**Name**: `DocuSign.list_recipients`

**Parameters**:
- `envelope_id` (string, required): Envelope ID

### 9. DocuSignAddRecipientTool

Add a new recipient to an envelope.

**Name**: `DocuSign.add_recipient`

**Parameters**:
- `envelope_id` (string, required): Envelope ID
- `email` (string, required): Recipient email
- `name` (string, required): Recipient name
- `recipient_type` (string, optional): "signer", "carbonCopy", or "certifiedDelivery"
- `routing_order` (integer, optional): Signing order

### 10. DocuSignUpdateRecipientTool

Update recipient information.

**Name**: `DocuSign.update_recipient`

**Parameters**:
- `envelope_id` (string, required): Envelope ID
- `recipient_id` (string, required): Recipient ID
- `email` (string, required): New email
- `name` (string, required): New name

### 11. DocuSignSendReminderTool

Send signing reminder to recipients.

**Name**: `DocuSign.send_reminder`

**Parameters**:
- `envelope_id` (string, required): Envelope ID

### 12. DocuSignListDocumentsTool

List all documents in an envelope.

**Name**: `DocuSign.list_documents`

**Parameters**:
- `envelope_id` (string, required): Envelope ID

### 13. DocuSignDownloadDocumentTool

Download a signed document (Base64 encoded).

**Name**: `DocuSign.download_document`

**Parameters**:
- `envelope_id` (string, required): Envelope ID
- `document_id` (string, required): Document ID

### 14. DocuSignAddDocumentTool

Add a document to an existing envelope.

**Name**: `DocuSign.add_document`

**Parameters**:
- `envelope_id` (string, required): Envelope ID
- `file_name` (string, required): File name
- `document_base64` (string, required): Base64 encoded document

### 15. DocuSignGetAuditTrailTool

Get the certificate of completion (audit trail).

**Name**: `DocuSign.get_audit_trail`

**Parameters**:
- `envelope_id` (string, required): Envelope ID

### 16. DocuSignCreateTemplateTool

Create a new template.

**Name**: `DocuSign.create_template`

**Parameters**:
- `name` (string, required): Template name
- `subject` (string, required): Email subject
- `email_blurb` (string, optional): Email body text

### 17. DocuSignHealthCheckTool

Verify API connectivity.

**Name**: `DocuSign.health_check`

**Parameters**: None

## Advanced Usage

### Custom DocuSignService Configuration

```java
import com.alibaba.langengine.docusign.service.DocuSignService;
import com.alibaba.langengine.docusign.tool.DocuSignSendEnvelopeTool;

// Create custom service with different credentials
DocuSignService customService = new DocuSignService(
    "https://demo.docusign.net",
    "different-account-id",
    "different-access-token"
);

// Use custom service in tools
DocuSignSendEnvelopeTool tool = new DocuSignSendEnvelopeTool(customService);
```

### Error Handling

```java
try {
    ToolExecuteResult result = tool.run(input, new ExecutionContext());
    
    if (result.isError()) {
        System.err.println("Tool execution failed: " + result.getOutput());
    } else {
        System.out.println("Success: " + result.getOutput());
    }
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
}
```

### Retry Configuration

The module automatically retries failed requests with exponential backoff. Configure retry behavior:

```properties
docusign_max_retries=5
docusign_retry_interval=2000  # Start with 2 second delay
```

## Best Practices

1. **Use Templates**: Templates are more efficient than creating envelopes from scratch
2. **Batch Operations**: Use `DocuSignBulkSendTool` for multiple recipients
3. **Status Checking**: Always verify envelope status before proceeding
4. **Error Handling**: Implement proper error handling for network issues
5. **Audit Trails**: Download audit trails for compliance
6. **Reminders**: Send reminders for pending signatures
7. **Void Carefully**: Voided envelopes cannot be restored

## Security Considerations

1. **Token Management**:
   - Never commit access tokens to source control
   - Rotate tokens regularly
   - Use OAuth 2.0 for production

2. **Data Protection**:
   - Documents are transmitted over HTTPS
   - Sensitive data should be encrypted
   - Follow your organization's data handling policies

3. **Access Control**:
   - Use least privilege principle
   - Limit API permissions to necessary operations
   - Monitor API usage for suspicious activity

## Troubleshooting

### Common Issues

#### Authentication Error
```
Error: AUTHORIZATION_INVALID_TOKEN
```
**Solution**: Token expired or invalid. Generate a new access token.

#### Account Not Found
```
Error: ACCOUNT_NOT_FOUND
```
**Solution**: Verify your account ID is correct and matches the token.

#### Template Not Found
```
Error: TEMPLATE_NOT_FOUND
```
**Solution**: Check template ID exists in your account using `DocuSignListTemplatesTool`.

#### Recipient Error
```
Error: RECIPIENT_EMAIL_INVALID
```
**Solution**: Ensure email addresses are valid and properly formatted.

### Enable Debug Logging

```properties
docusign_enable_logging=true
```

Check logs for detailed error information.

## API Rate Limits

DocuSign imposes rate limits:
- **Demo**: 1000 API calls per hour
- **Production**: Varies by plan

The module automatically retries on rate limit errors (429 status).

## Examples

See comprehensive examples in:
- `DocuSignExamples.java` - 10+ usage scenarios
- `DocuSignToolsTest.java` - Unit tests
- `DocuSignExtendedToolsTest.java` - Extended test cases

## Migration Guide

### From Previous Versions

If upgrading from an earlier version:

1. Update dependency version in `pom.xml`
2. New tools are automatically registered
3. No breaking changes in existing tools
4. Configuration properties remain backward compatible

## Resources

- [DocuSign Developer Center](https://developers.docusign.com/)
- [DocuSign eSignature API Reference](https://developers.docusign.com/docs/esign-rest-api/)
- [OAuth 2.0 Authentication](https://developers.docusign.com/platform/auth/)
- [Ali-LangEngine Documentation](../../README.md)

## Support

For issues and questions:
- GitHub Issues: [Create an issue](https://github.com/your-org/ali-langengine/issues)
- Documentation: [Wiki](https://github.com/your-org/ali-langengine/wiki)

## License

Copyright (C) 2024 AIDC-AI

Licensed under the Apache License, Version 2.0

## Changelog

### Version 1.2.6
- ✨ Added 7 new tools (total 17 tools)
- ✨ Bulk send support
- ✨ Enhanced document management
- ✨ Audit trail retrieval
- ✨ Reminder sending
- 🔧 Improved error handling
- 🔧 Better retry mechanism
- 📝 Comprehensive documentation
- 📝 10+ usage examples

### Version 1.0.0
- 🎉 Initial release
- ✅ 10 basic tools
- ✅ Template and envelope management
- ✅ Basic document operations
