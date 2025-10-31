package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.agent.AgentExecutor;
import com.alibaba.langengine.core.agent.semantickernel.SemanticKernelAgent;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.docusign.tool.*;
import com.alibaba.langengine.docusign.service.DocuSignService;

import java.util.Arrays;
import java.util.List;

/**
 * DocuSign 工具使用示例
 * 
 * 展示如何在 Agent 中使用 DocuSign 工具完成各种文档签署任务
 */
public class DocuSignExamples {

    /**
     * 示例1: 基本的文档签署流程
     * 1. 列出可用模板
     * 2. 选择模板发送信封
     * 3. 检查信封状态
     */
    public static void example1_BasicSigningWorkflow() {
        // 创建 DocuSign 工具
        List<BaseTool> tools = Arrays.asList(
            new DocuSignListTemplatesTool(),
            new DocuSignSendEnvelopeTool(),
            new DocuSignGetEnvelopeStatusTool()
        );

        // 创建 Agent
        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        // 创建执行器
        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        // 执行任务
        String task = "First, list all available DocuSign templates. " +
                     "Then send an envelope using the first template to john@example.com (John Doe). " +
                     "Finally, check the status of the envelope.";
        
        String result = executor.run(task);
        System.out.println("Result: " + result);
    }

    /**
     * 示例2: 批量发送文档
     * 向多个收件人同时发送相同的文档
     */
    public static void example2_BulkSendDocuments() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignBulkSendTool(),
            new DocuSignGetEnvelopeStatusTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        String task = "Send the template '12345-abcde' to the following recipients: " +
                     "1. alice@company.com (Alice Smith), " +
                     "2. bob@company.com (Bob Johnson), " +
                     "3. charlie@company.com (Charlie Brown). " +
                     "Then check the status of all envelopes.";

        String result = executor.run(task);
        System.out.println("Bulk send result: " + result);
    }

    /**
     * 示例3: 管理信封和收件人
     * 创建信封、添加收件人、发送提醒
     */
    public static void example3_ManageEnvelopeAndRecipients() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignCreateEnvelopeFromDocumentTool(),
            new DocuSignAddRecipientTool(),
            new DocuSignListRecipientsTool(),
            new DocuSignSendReminderTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        String task = "Create an envelope with a document, add two recipients: " +
                     "manager@company.com (Manager) and reviewer@company.com (Reviewer), " +
                     "then send a reminder to all recipients.";

        String result = executor.run(task);
        System.out.println("Envelope management result: " + result);
    }

    /**
     * 示例4: 文档管理和下载
     * 列出文档、下载文档、获取审计追踪
     */
    public static void example4_DocumentManagement() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignListEnvelopesTool(),
            new DocuSignListDocumentsTool(),
            new DocuSignDownloadDocumentTool(),
            new DocuSignGetAuditTrailTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        String task = "List all envelopes, find the completed ones, " +
                     "download their documents and get the audit trail (certificate of completion).";

        String result = executor.run(task);
        System.out.println("Document management result: " + result);
    }

    /**
     * 示例5: 信封生命周期管理
     * 创建、发送、跟踪、作废信封
     */
    public static void example5_EnvelopeLifecycle() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignSendEnvelopeTool(),
            new DocuSignGetEnvelopeStatusTool(),
            new DocuSignListRecipientsTool(),
            new DocuSignVoidEnvelopeTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        String task = "Send an envelope using template 'template-123' to test@example.com, " +
                     "check its status, list recipients, and if it's not completed, void it with reason 'Testing'.";

        String result = executor.run(task);
        System.out.println("Envelope lifecycle result: " + result);
    }

    /**
     * 示例6: 健康检查和错误处理
     * 验证 DocuSign 连接和配置
     */
    public static void example6_HealthCheckAndValidation() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignHealthCheckTool(),
            new DocuSignListTemplatesTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(5)
            .build();

        String task = "Check if DocuSign service is healthy and accessible. " +
                     "If yes, list available templates. If no, report the error.";

        String result = executor.run(task);
        System.out.println("Health check result: " + result);
    }

    /**
     * 示例7: 使用自定义 DocuSignService
     * 支持动态配置不同的 DocuSign 账户
     */
    public static void example7_CustomDocuSignService() {
        // 创建自定义服务配置
        DocuSignService customService = new DocuSignService(
            "https://demo.docusign.net",
            "your-account-id",
            "your-access-token"
        );

        // 使用自定义服务创建工具
        List<BaseTool> tools = Arrays.asList(
            new DocuSignListTemplatesTool(customService),
            new DocuSignSendEnvelopeTool(customService),
            new DocuSignGetEnvelopeStatusTool(customService)
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .build();

        String task = "List templates from the custom DocuSign account";
        String result = executor.run(task);
        System.out.println("Custom service result: " + result);
    }

    /**
     * 示例8: 复杂的多步骤工作流
     * 组合多个工具完成复杂任务
     */
    public static void example8_ComplexWorkflow() {
        // 使用所有可用工具
        List<BaseTool> tools = Arrays.asList(
            new DocuSignHealthCheckTool(),
            new DocuSignListTemplatesTool(),
            new DocuSignCreateEnvelopeFromDocumentTool(),
            new DocuSignAddRecipientTool(),
            new DocuSignListDocumentsTool(),
            new DocuSignSendReminderTool(),
            new DocuSignGetEnvelopeStatusTool(),
            new DocuSignDownloadDocumentTool(),
            new DocuSignGetAuditTrailTool(),
            new DocuSignVoidEnvelopeTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(15)
            .build();

        String task = "I need to manage a contract signing process: " +
                     "1. First, verify DocuSign is working properly. " +
                     "2. Create a new envelope with a contract document. " +
                     "3. Add three signers in order: CEO, CFO, and Legal. " +
                     "4. Check the envelope status. " +
                     "5. If no one has signed yet, send reminders. " +
                     "6. List all documents in the envelope. " +
                     "7. Once completed, download the signed document and get the audit trail.";

        String result = executor.run(task);
        System.out.println("Complex workflow result: " + result);
    }

    /**
     * 示例9: 直接调用工具（不使用 Agent）
     */
    public static void example9_DirectToolUsage() throws Exception {
        // 创建工具实例
        DocuSignSendEnvelopeTool sendTool = new DocuSignSendEnvelopeTool();
        
        // 准备输入参数
        String input = "{\n" +
            "  \"templateId\": \"template-123\",\n" +
            "  \"email\": \"recipient@example.com\",\n" +
            "  \"name\": \"John Recipient\"\n" +
            "}";
        
        // 直接调用工具
        com.alibaba.langengine.core.tool.ToolExecuteResult result = 
            sendTool.run(input, new com.alibaba.langengine.core.callback.ExecutionContext());
        
        System.out.println("Direct tool usage result: " + result.getOutput());
        
        // 检查信封状态
        if (!result.isError()) {
            com.alibaba.fastjson.JSONObject envelopeData = 
                com.alibaba.fastjson.JSON.parseObject(result.getOutput().toString());
            String envelopeId = envelopeData.getString("envelopeId");
            
            DocuSignGetEnvelopeStatusTool statusTool = new DocuSignGetEnvelopeStatusTool();
            String statusInput = "{\"envelope_id\": \"" + envelopeId + "\"}";
            com.alibaba.langengine.core.tool.ToolExecuteResult statusResult = 
                statusTool.run(statusInput, new com.alibaba.langengine.core.callback.ExecutionContext());
            
            System.out.println("Envelope status: " + statusResult.getOutput());
        }
    }

    /**
     * 示例10: 批量处理和错误恢复
     */
    public static void example10_BatchProcessingWithErrorRecovery() {
        List<BaseTool> tools = Arrays.asList(
            new DocuSignBulkSendTool(),
            new DocuSignGetEnvelopeStatusTool(),
            new DocuSignSendReminderTool()
        );

        SemanticKernelAgent agent = new SemanticKernelAgent();
        agent.setTools(tools);

        AgentExecutor executor = AgentExecutor.builder()
            .agent(agent)
            .tools(tools)
            .maxIterations(10)
            .build();

        String task = "Send template 'monthly-report' to 50 employees. " +
                     "Track which sends failed and retry them. " +
                     "For successful sends, send reminders after 2 days if not signed.";

        String result = executor.run(task);
        System.out.println("Batch processing result: " + result);
    }

    public static void main(String[] args) {
        System.out.println("=== DocuSign Tool Examples ===\n");
        
        // 运行示例（取消注释需要运行的示例）
        
        // example1_BasicSigningWorkflow();
        // example2_BulkSendDocuments();
        // example3_ManageEnvelopeAndRecipients();
        // example4_DocumentManagement();
        // example5_EnvelopeLifecycle();
        // example6_HealthCheckAndValidation();
        // example7_CustomDocuSignService();
        // example8_ComplexWorkflow();
        
        try {
            example9_DirectToolUsage();
        } catch (Exception e) {
            System.err.println("Error in example: " + e.getMessage());
        }
        
        // example10_BatchProcessingWithErrorRecovery();
    }
}
