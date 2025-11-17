package com.google.adk.a2a.utils;

import com.google.adk.agents.*;
import com.google.adk.tools.BaseTool;
import io.a2a.spec.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentCardBuilder {

  private static final Logger logger = LoggerFactory.getLogger(AgentCardBuilder.class);

  private final BaseAgent agent;
  private String rpcUrl;
  private AgentCapabilities capabilities;
  private String docUrl;
  private AgentProvider provider;
  private String agentVersion;
  private String protocolVersion;
  private Map<String, SecurityScheme> securitySchemes;

  protected AgentCardBuilder(
      BaseAgent agent,
      String rpcUrl,
      AgentCapabilities capabilities,
      String docUrl,
      AgentProvider provider,
      String agentVersion,
      Map<String, SecurityScheme> securitySchemes) {
    if (agent == null) {
      throw new IllegalArgumentException("Agent cannot be null or empty.");
    }
    this.agent = agent;
    this.rpcUrl = rpcUrl != null ? rpcUrl : "http://localhost:80/a2a";
    this.capabilities =
        capabilities != null ? capabilities : new AgentCapabilities.Builder().build();
    this.docUrl = docUrl;
    this.provider = provider;
    this.agentVersion = agentVersion != null ? agentVersion : "0.0.1";
    this.securitySchemes = securitySchemes;
  }

  public AgentCardBuilder(BaseAgent agent) {
    this.agent = agent;
  }

  public AgentCardBuilder rpcUrl(String rpcUrl) {
    this.rpcUrl = rpcUrl;
    return this;
  }

  public AgentCardBuilder capabilities(AgentCapabilities capabilities) {
    this.capabilities = capabilities;
    return this;
  }

  public AgentCardBuilder docUrl(String docUrl) {
    this.docUrl = docUrl;
    return this;
  }

  public AgentCardBuilder provider(AgentProvider provider) {
    this.provider = provider;
    return this;
  }

  public AgentCardBuilder agentVersion(String agentVersion) {
    this.agentVersion = agentVersion;
    return this;
  }

  public AgentCardBuilder protocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
    return this;
  }

  public AgentCardBuilder securitySchemes(Map<String, SecurityScheme> securitySchemes) {
    this.securitySchemes = securitySchemes;
    return this;
  }

  public AgentCard build() {
    List<AgentSkill> allSkills = new ArrayList<>();
    allSkills.addAll(buildPrimarySkills(agent));
    allSkills.addAll(buildSubAgentSkills(agent));
    return new AgentCard.Builder()
        .name(agent.name())
        .description(agent.description() != null ? agent.description() : "An ADK Agent")
        .url(rpcUrl)
        .provider(provider)
        .version(agentVersion)
        .documentationUrl(docUrl)
        .capabilities(capabilities)
        .skills(allSkills)
        .protocolVersion(protocolVersion)
        .defaultInputModes(List.of("text/plain"))
        .defaultOutputModes(List.of("text/plain"))
        .securitySchemes(securitySchemes)
        .supportsAuthenticatedExtendedCard(false)
        .build();
  }

  private static List<AgentSkill> buildPrimarySkills(BaseAgent agent) {
    if (agent instanceof LlmAgent) {
      return buildLlmAgentSkills((LlmAgent) agent);
    } else {
      return buildNonLlmAgentSkills(agent);
    }
  }

  private static List<AgentSkill> buildLlmAgentSkills(LlmAgent agent) {
    List<AgentSkill> skills = new ArrayList<>();

    String agentDescription = buildLlmAgentDescriptionWithInstructions(agent);
    skills.add(
        new AgentSkill.Builder()
            .id(agent.name())
            .name("model")
            .description(agentDescription)
            .inputModes(getInputModes(agent))
            .outputModes(getOutputModes(agent))
            .tags(List.of("llm"))
            .build());

    if (!agent.tools().isEmpty()) {
      List<AgentSkill> toolSkills = buildToolSkills(agent);
      skills.addAll(toolSkills);
    }
    return skills;
  }

  private static List<AgentSkill> buildSubAgentSkills(BaseAgent agent) {
    List<AgentSkill> subAgentSkills = new ArrayList<>();
    for (BaseAgent subAgent : agent.subAgents()) {
      try {
        List<AgentSkill> subSkills = buildPrimarySkills(subAgent);
        for (AgentSkill skill : subSkills) {
          String subAgentId = subAgent.name() + "_" + skill.id();
          String subAgentName = subAgent.name() + ": " + skill.name();
          List<String> tags = new ArrayList<>();
          tags.add("sub_agent:" + subAgent.name());
          if (skill.tags() != null) {
            tags.addAll(skill.tags());
          }
          subAgentSkills.add(
              new AgentSkill.Builder()
                  .id(subAgentId)
                  .name(subAgentName)
                  .description(skill.description())
                  .examples(skill.examples())
                  .inputModes(skill.inputModes())
                  .outputModes(skill.outputModes())
                  .tags(tags)
                  .build());
        }
      } catch (Exception e) {
        logger.error(
            "Warning: Failed to build skills for sub-agent {}: {}",
            subAgent.name(),
            e.getMessage());
      }
    }
    return subAgentSkills;
  }

  private static List<AgentSkill> buildToolSkills(LlmAgent agent) {
    List<AgentSkill> toolSkills = new ArrayList<>();
    List<BaseTool> canonicalTools = agent.canonicalTools().toList().blockingGet();
    for (BaseTool tool : canonicalTools) {
      String toolName = tool.name() != null ? tool.name() : tool.getClass().getSimpleName();
      toolSkills.add(
          new AgentSkill.Builder()
              .id(agent.name() + "-" + toolName)
              .name(toolName)
              .description(tool.description() != null ? tool.description() : "Tool: " + toolName)
              .tags(List.of("llm", "tools"))
              .build());
    }
    return toolSkills;
  }

  private static AgentSkill buildPlannerSkill(LlmAgent agent) {
    return new AgentSkill.Builder()
        .id(agent.name() + "-planner")
        .name("planning")
        .description("Can think about the tasks to do and make plans")
        .tags(List.of("llm", "planning"))
        .build();
  }

  private static AgentSkill buildCodeExecutorSkill(LlmAgent agent) {
    return new AgentSkill.Builder()
        .id(agent.name() + "-code-executor")
        .name("code-execution")
        .description("Can execute codes")
        .tags(List.of("llm", "code_execution"))
        .build();
  }

  private static List<AgentSkill> buildNonLlmAgentSkills(BaseAgent agent) {
    List<AgentSkill> skills = new ArrayList<>();

    String agentDescription = buildAgentDescription(agent);

    String agentType = getAgentType(agent);
    String agentName = getAgentSkillName(agent);

    skills.add(
        new AgentSkill.Builder()
            .id(agent.name())
            .name(agentName)
            .description(agentDescription)
            .inputModes(getInputModes(agent))
            .outputModes(getOutputModes(agent))
            .tags(List.of(agentType))
            .build());

    if (!agent.subAgents().isEmpty()) {
      AgentSkill orchestrationSkill = buildOrchestrationSkill(agent, agentType);
      if (orchestrationSkill != null) {
        skills.add(orchestrationSkill);
      }
    }
    return skills;
  }

  private static AgentSkill buildOrchestrationSkill(BaseAgent agent, String agentType) {
    List<String> subAgentDescriptions = new ArrayList<>();
    for (BaseAgent subAgent : agent.subAgents()) {
      String description =
          subAgent.description() != null ? subAgent.description() : "No description ";
      subAgentDescriptions.add(subAgent.name() + ": " + description);
    }

    if (subAgentDescriptions.isEmpty()) {
      return null;
    }

    return new AgentSkill.Builder()
        .id(agent.name() + "-sub-agents")
        .name("sub-agents")
        .description("Orchestrates: " + String.join("; ", subAgentDescriptions))
        .tags(List.of(agentType, "orchestration"))
        .build();
  }

  private static String getAgentType(BaseAgent agent) {
    if (agent instanceof LlmAgent) {
      return "llm";
    } else if (agent instanceof SequentialAgent) {
      return "sequential_workflow";
    } else if (agent instanceof ParallelAgent) {
      return "parallel_workflow";
    } else if (agent instanceof LoopAgent) {
      return "loop_workflow";
    } else {
      return "custom_agent";
    }
  }

  private static String getAgentSkillName(BaseAgent agent) {
    if (agent instanceof LlmAgent) {
      return "model";
    } else if (agent instanceof SequentialAgent
        || agent instanceof ParallelAgent
        || agent instanceof LoopAgent) {
      return "workflow";
    } else {
      return "custom";
    }
  }

  private static String buildAgentDescription(BaseAgent agent) {
    List<String> descriptionParts = new ArrayList<>();

    if (agent.description() != null) {
      descriptionParts.add(agent.description());
    }

    if (!(agent instanceof LlmAgent) && !agent.subAgents().isEmpty()) {
      String workflowDescription = getWorkflowDescription(agent);
      if (workflowDescription != null) {
        descriptionParts.add(workflowDescription);
      }
    }

    return !descriptionParts.isEmpty()
        ? String.join(" ", descriptionParts)
        : getDefaultDescription(agent);
  }

  private static String buildLlmAgentDescriptionWithInstructions(LlmAgent agent) {
    List<String> descriptionParts = new ArrayList<>();

    if (agent.description() != null) {
      descriptionParts.add(agent.description());
    }

    if (agent.instruction() != null) {
      descriptionParts.add(replacePronouns(agent.instruction()));
    }

    if (agent.globalInstruction() != null) {
      descriptionParts.add(replacePronouns(agent.globalInstruction()));
    }

    return !descriptionParts.isEmpty()
        ? String.join(" ", descriptionParts)
        : getDefaultDescription(agent);
  }

  private static String replacePronouns(Instruction instruction) {
    String text = "";
    if (instruction instanceof Instruction.Static s) {
      text = s.instruction();
    }
    Map<String, String> pronounMap = new HashMap<>();
    pronounMap.put("you", "I");
    pronounMap.put("your", "my");
    pronounMap.put("yours", "mine");
    pronounMap.put("你", "我");
    pronounMap.put("你们", "我们");

    Pattern pattern = Pattern.compile("\\b(you|your|yours|你|你们)\\b", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(text);
    return matcher.replaceAll(match -> pronounMap.get(match.group().toLowerCase()));
  }

  private static String getWorkflowDescription(BaseAgent agent) {
    if (agent.subAgents().isEmpty()) {
      return null;
    }

    if (agent instanceof SequentialAgent) {
      return buildSequentialDescription((SequentialAgent) agent);
    } else if (agent instanceof ParallelAgent) {
      return buildParallelDescription((ParallelAgent) agent);
    } else if (agent instanceof LoopAgent) {
      return buildLoopDescription((LoopAgent) agent);
    }

    return null;
  }

  private static String buildSequentialDescription(SequentialAgent agent) {
    List<String> descriptions = new ArrayList<>();
    for (int i = 0; i < agent.subAgents().size(); i++) {
      BaseAgent subAgent = agent.subAgents().get(i);
      String subDescription =
          subAgent.description() != null
              ? subAgent.description()
              : "execute the " + subAgent.name() + " agent";
      if (i == 0) {
        descriptions.add("First, this agent will " + subDescription);
      } else if (i == agent.subAgents().size() - 1) {
        descriptions.add("Finally, this agent will " + subDescription);
      } else {
        descriptions.add("Then, this agent will " + subDescription);
      }
    }
    return String.join(" ", descriptions) + ".";
  }

  private static String buildParallelDescription(ParallelAgent agent) {
    List<String> descriptions = new ArrayList<>();
    for (int i = 0; i < agent.subAgents().size(); i++) {
      BaseAgent subAgent = agent.subAgents().get(i);
      String subDescription =
          subAgent.description() != null
              ? subAgent.description()
              : "execute the " + subAgent.name() + " agent";
      if (i == 0) {
        descriptions.add("This agent will " + subDescription);
      } else if (i == agent.subAgents().size() - 1) {
        descriptions.add("and " + subDescription);
      } else {
        descriptions.add(", " + subDescription);
      }
    }
    return String.join("", descriptions) + " simultaneously.";
  }

  private static String buildLoopDescription(LoopAgent agent) {
      int maxIterations = -1;
      try {
          Field field = LoopAgent.class.getDeclaredField("maxIterations");
          field.setAccessible(true);

          @SuppressWarnings("unchecked")
          Optional<Integer> maxIterationsOption = (Optional<Integer>) field.get(agent);
          int maxValue = maxIterationsOption.orElse(-1);

          maxIterations = maxValue;
      } catch (Exception e) {
         logger.error("Failed to get max iterations: {}, set to default -1", e.getMessage(), e);
      }
    List<String> descriptions = new ArrayList<>();
    for (int i = 0; i < agent.subAgents().size(); i++) {
      BaseAgent subAgent = agent.subAgents().get(i);
      String subDescription =
          subAgent.description() != null
              ? subAgent.description()
              : "execute the " + subAgent.name() + " agent ";
      if (i == 0) {
        descriptions.add("This agent will " + subDescription);
      } else if (i == agent.subAgents().size() - 1) {
        descriptions.add("and " + subDescription);
      } else {
        descriptions.add(", " + subDescription);
      }
    }
    return String.join(" ", descriptions)
        + " in a loop (max "
        + (maxIterations == -1 ? "unlimited" : maxIterations)
        + " iterations).";
  }

  private static String getDefaultDescription(BaseAgent agent) {
    Map<Class<?>, String> agentTypeDescriptions = new HashMap<>();
    agentTypeDescriptions.put(LlmAgent.class, "An LLM-based agent");
    agentTypeDescriptions.put(SequentialAgent.class, "A sequential workflow agent");
    agentTypeDescriptions.put(ParallelAgent.class, "A parallel workflow agent");
    agentTypeDescriptions.put(LoopAgent.class, "A loop workflow agent");

    for (Map.Entry<Class<?>, String> entry : agentTypeDescriptions.entrySet()) {
      if (entry.getKey().isInstance(agent)) {
        return entry.getValue();
      }
    }

    return "A custom agent";
  }

  private static List<String> getInputModes(BaseAgent agent) {
    if (!(agent instanceof LlmAgent)) {
      return null;
    }
    return null; // 可以扩展为基于模型的功能检查
  }

  private static List<String> getOutputModes(BaseAgent agent) {
    if (!(agent instanceof LlmAgent)) {
      return null;
    }
    return null; // 可以扩展为从配置中获取
  }
}
