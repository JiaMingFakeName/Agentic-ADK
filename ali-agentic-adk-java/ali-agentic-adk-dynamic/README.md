# Ali-Agent ADK Dynamic

[中文版](README-CN.md)

## Project Introduction

Ali-Agent ADK Dynamic provides a configuration set for atomic changes in the smallest granularity LLM call scenarios. It enables high-frequency iterative experiments to be updated at a different pace from stable business base code releases. It is a more complete upgrade than ordinary prompt management systems.

Specifically, the content that needs to be changed together includes not only the prompt template, but also the model configuration, output variable parser, and input variable formatter. We call this the quartet. If these cannot be changed atomically, online systems will have bugs due to mismatched configurations.
- Model Configuration: Because prompts and models are closely related, different models require very different prompts, especially when switching from large models to small models.
- Output Variable Parser: Similarly, output parsers are also closely related to prompts. Models that follow JSON format well are parsed very differently from those that rely on locking the first few tokens of model output.
- Input Variable Formatter: How system variables become text may also be closely related to the model/prompt. In the era of bare models, each model's history needs to be formatted differently, and FunctionCall result formats also vary. There's also detailed control over input variables (e.g., how to tailor product function return value objects for different prompts).

To address this, we designed configuration definitions and executor implementations for these four items. Based on Google-ADK LLMAgent, we inherited DynamicLLMAgent and added the AtomicPromptTemplateDefine definition. The dynamic configuration inside is optional (most prompt configuration backends don't have complete capabilities). Each dynamic configuration will override the corresponding original configuration of LLMAgent. When a prompt template is configured, the original LLMAgent's multi-agent transfer capability will be discarded (because it's implemented based on prompts).

## Project Core Overview

### Core Concept: Quartet Configuration

The core design idea of ADK-Dynamic is "atomic quartet," which refers to **four configurations that must be changed synchronously for a single LLM call**:

1. **Prompt Template** (ChatPromptTemplateDefine / RawPromptTemplateDefine)
2. **Model Configuration** (ModelDefine)
3. **Output Parser** (OutputParserDefine)
4. **Input Formatter** (InputFormatterDefine)

These four configurations are managed atomically through `AtomicPromptTemplateDefine` to ensure online systems don't encounter errors due to configuration mismatches.

AtomicPromptTemplateDefine is dynamically fetched through AtomicPromptTemplateGetter at runtime and can be changed anytime.

DynamicLLMAgent inherits from LLMAgent and supports setting this parameter.

### Core Domain Object Hierarchy

```
AtomicPromptTemplateDefine (Top-level container)
├── ChatPromptTemplateDefine (Messages API template) [Choose one]
│   ├── instructionTemplate: List<String>
│   ├── preHistoryTemplate: List<MessageTemplateDefine>
│   │   └── MessageTemplateDefine (Message template)
│   │       ├── type: text/partial_text/blob/file
│   │       ├── mineType: MIME type
│   │       ├── template: Template content
│   │       └── role: system/user/assistant
│   ├── historyFormatter: HistoryFormatterDefine
│   │   ├── type: Formatter type
│   │   ├── content: Formatter content
│   │   └── params: Formatter parameters
│   └── userTemplate: List<MessageTemplateDefine>
│
├── RawPromptTemplateDefine (Raw template) [Choose one]
│   ├── totalPromptTemplate: String
│   └── historyFormatter: HistoryFormatterDefine
│
├── syntaxType: SyntaxType (Syntax type enum)
│   └── SIMPLE / HANDLEBARS / STRICT / COMMENT
│
├── model: ModelDefine (Model configuration)
│   ├── name: Model name
│   ├── identifier: Model identifier
│   └── extraConfigs: Extended configuration
│
├── outputParser: OutputParserDefine (Output parser)
│   ├── type: Parser type
│   ├── content: Parser content
│   ├── mergeIncremental: Whether to merge incremental results
│   └── params: Parser parameters
│
├── inputFormatter: List<InputFormatterDefine> (Input formatters)
│   └── InputFormatterDefine
│       ├── type: Formatter type
│       ├── content: Formatter content
│       ├── inputVariableName: Input variable name
│       └── params: Formatter parameters
│
└── extraConfigs: Map<String, Object> (Business extension configuration)
```

### Core Object Details

#### First Layer: Top-level Container

##### AtomicPromptTemplateDefine (Top-level Container)
**Purpose:** The atomic unit of the entire dynamic configuration, containing all configurations of the quartet.

**Core Fields:**
- `chatPromptTemplate` / `rawPromptTemplate`: **Choose one** template definition method (chatPromptTemplate has higher priority)
- `syntaxType`: Syntax type of template variables
- `model`: Model configuration (strongly associated with prompt)
- `outputParser`: Output parser (strongly associated with model output capability)
- `inputFormatter`: List of input formatters (strongly associated with prompt input format)
- `extraConfigs`: Business extension configuration

---

#### Second Layer: Prompt Template (Choose One)

##### 2.1 ChatPromptTemplateDefine (Message Template)
**Purpose:** OpenAI-style multi-turn conversation template supporting role separation.

**Applicable Scenarios:** For the newly popular OpenAI-style Message API. This type of API predefines a List<Message> interface, making it impossible to express history processing in the template. Therefore, the history message processing method needs to be separated. This leads to the need for two lists of prompt templates before and after history. When there's no history, these two templates don't differ much.

**Structure Composition:**
- `instructionTemplate`: System instruction template (system role)
- `preHistoryTemplate`: Prompt template before history messages
  - Type: `List<MessageTemplateDefine>` → See third layer for details
- `historyFormatter`: History message formatter
  - Type: `HistoryFormatterDefine` → See third layer for details
- `userTemplate`: User message prompt template
  - Type: `List<MessageTemplateDefine>` → See third layer for details

##### 2.2 RawPromptTemplateDefine (Raw Template)
**Purpose:** ChatML format bare prompt template.

**Applicable Scenarios:** Minimal user prompts or single-box configuration backend

**Structure Composition:**
- `totalPromptTemplate`: Complete prompt string (supports `{history}` variable to split before and after history)
Its format is as follows: (if it doesn't conform to the format, simply fill in a user prompt)
```
<|im_start|>system
SystemTemplate here,You are {role}
<|im_end|>
<|im_start|>user
Prehistory Template here
<|im_end|>
<|im_start|>assistant
Prehistory Template here
<|im_end|>
{history}
<|im_start|>user
user template here
{var}
<|im_end|>
<|im_start|>image_url
http://mypicurl
<|im_end|>
<|im_start|>video
http://myvideourl
<|im_end|>
<|im_start|>audio
http://myaudiourl
<|im_end|>
<|im_start|>assistant
prefill if support

```
- `historyFormatter`: History message formatter
  - Type: `HistoryFormatterDefine` → See third layer for details

---

#### Third Layer: Prompt Template Subcomponents

##### 3.1 MessageTemplateDefine (Message Template)
**Position:** Referenced by `ChatPromptTemplateDefine`'s `preHistoryTemplate` and `userTemplate`

**Purpose:** Defines the content and type of a single message, supporting multimodal content.

**Core Fields:**
- `type`: Message type
  - `TYPE_TEXT`: Complete text
  - `TYPE_PARTIAL_TEXT`: Prefix text (can be concatenated later)
  - `TYPE_BLOB`: Binary object (image, audio, etc.)
  - `TYPE_FILE`: File
- `mineType`: MIME type (e.g., "image/png")
- `template`: Template content (text types support variable replacement, non-text types don't)
- `role`: Message role (system/user/model)

**Use Cases:** Building multimodal messages, distinguishing role-based conversation content.

##### 3.2 HistoryFormatterDefine (History Formatter)
**Position:** Referenced by `ChatPromptTemplateDefine` and `RawPromptTemplateDefine`

**Purpose:** Controls how history messages are processed.

**Core Fields:**
- `type`: Formatter type (class/groovy, etc.)
- `content`: Formatter content (class name or script)
- `params`: Formatter parameters (e.g., `maxCount` to limit entries)

**Typical Implementation:** `LimitCountHistoryFormatter` (limits history message count)

**Use Cases:** Controlling context window size, compressing history messages.

---

#### Second Layer: Template Syntax

##### SyntaxType (Syntax Type Enum)
**Purpose:** Defines the syntax rules for variable replacement in templates.

**Available Values:**
- `SIMPLE`: `{variable}` - Simple syntax
- `HANDLEBARS`: `{{variable}}` - Handlebars syntax
- `STRICT`: `${!variable}` - Strict syntax
- `COMMENT`: `##variable##` - Comment syntax

---

#### Second Layer: Model Configuration

##### ModelDefine (Model Configuration)
**Purpose:** Defines the AI model and its parameters.

**Core Fields:**
- `name`: Model name (e.g., "gpt-4", "qwen-max"), may affect model parameter configuration
- `identifier`: Model unique identifier, used to retrieve in specific systems, e.g., dashscope_qwen-turbo-0930
- `extraConfigs`: Model extension parameters (e.g., temperature, top_p, max_tokens, etc.)
Standard content should be GenerateContentConfig configuration. Python and Java syntax conversion will be automatic. Additionally, properties in this will be set to the corresponding properties of BaseLlm (if available) to accommodate more customized parameters.

---

#### Second Layer: Input Formatting

##### InputFormatterDefine (Input Formatter)
**Purpose:** Defines how to format system variables into text understandable by the model.

**Core Fields:**
- `type`: Formatter type (bean instance, class, groovy, handlebars, etc.)
- `content`: Specific formatter content
- `inputVariableName`: Specifies the variable name to be formatted
- `params`: InputFormatter configuration parameters

**Action Timing:**
At the very beginning, converts variables in state to text (if there's an inputFormatter, uses inputFormatter) and places them in a special state for template formatting. Scripts can access all variables in the state. Replaces the string at the position corresponding to inputVariableName in the template.

**Script Template (class & bean):**
Inherits com.alibaba.agentic.dynamic.domain.inputformatter.InputFormatter
Class mode directly instantiates with new. Bean mode retrieves the bean with the content name from the Spring container
The third parameter of InputFormatter is the configured params parameter

**Script Template (groovy):**
```
varname + "s"
```
**Script Template (handlebars):**
```
{{varname}}+ "s"
```

---

#### Second Layer: Output Parsing

##### OutputParserDefine (Output Parser)
**Purpose:** Defines how to parse the model's output results.

**Core Fields:**
- `type`: Parser type (class/groovy, etc.)
- `content`: Specific parser content (class name, script code, etc.)
- `mergeIncremental`: Whether to first merge incremental results of streaming output, turning it into content completed so far from the beginning
- `params`: Parser parameters (setter values for Java classes)

For script types (groovy) and the like, note that the available variable name is: output
And only text types are supported.

For bean and class types, inherit com.alibaba.agentic.dynamic.domain.outputparser
Override the parse(LlmResponse llmResponse, String string) method to process llmResponse, more primitive data types.

**Action Timing:**

The output parser is called every time there's a return. If it's streaming content, in most cases it may need to be merged into an incremental format first before processing. Therefore, there's an internal implementation (of course, you can implement this original character-by-character form yourself).

*Note*, if you just want to modify the format from incremental to cumulative, you can choose to configure CumulativeOutputParser, but don't configure mergeIncremental=true again.
```
{
  "type": "class",
  "content": "com.alibaba.agentic.dynamic.domain.outputparser.CumulativeOutputParser",
  "mergeIncremental": false
}
```

---

## Usage Guide

### Quick Start

To start using Ali-Agent ADK Dynamic, follow these steps:

1. Add Maven dependency:
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-agentic-adk-dynamic</artifactId>
    <version>${ali-agentic-adk.version}</version>
</dependency>
```

2. Create a Spring Boot application and add component scanning:
```java
@SpringBootApplication(scanBasePackages = {"com.alibaba.agentic"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Basic Usage Example

#### Refer to AgentTest
```
        AtomicPromptTemplateDefine def = AtomicPromptTemplateDefine.builder()
                .model(ModelDefine.builder()
                        .name("test-model")
                        .identifier("test-model").build())
                .chatPromptTemplate(ChatPromptTemplateDefine.builder()
                        .instructionTemplate("you are a helpful assistant,your name is {var}")
                        .preHistoryTemplate(Collections.singletonList(MessageTemplateDefine.builder()
                                .type(MessageTemplateDefine.TYPE_TEXT)
                                .template("please print {var} at the beginning, and answer hello")
                                .build()))
                        .historyFormatter(HistoryFormatterDefine.builder()
                                .type("class")
                                .content("com.alibaba.agentic.TestHistoryFormatter")
                                .build())
                        .userTemplate("{input}")
                        .build())
                .inputFormatter(Collections.singletonList(InputFormatterDefine.builder()
                                .inputVariableName("var")
                                .type("groovy")
                                .content("var + 's'")
                        .build()
                ))
                .outputParser(OutputParserDefine.builder()
                        .type("groovy")
                        .mergeIncremental(true)
                        .content("return ['a':  input]")
                        .build())
                .build();



        DynamicLLMAgent agent = DynamicLLMAgent.builder()
                .name("test-agent")
                .atomicPromptTemplateGetter(new AtomicPromptTemplateGetter() {
                            @Override
                            public AtomicPromptTemplateInstance get() {
                                return new AtomicPromptTemplateInstance(def);
                            }
                        }).build();