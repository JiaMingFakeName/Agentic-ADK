package com.google.adk.a2a.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.adk.JsonBaseModel;
import com.google.genai.types.*;
import com.google.genai.types.Part;
import io.a2a.spec.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author dlancerc
 * @date 2025-07-28-21:34
 */
public class PartConverter extends JsonBaseModel {
  private static final Logger logger = Logger.getLogger(PartConverter.class.getName());

  public static final String A2A_DATA_PART_METADATA_TYPE_KEY = "type";
  public static final String A2A_DATA_PART_METADATA_IS_LONG_RUNNING_KEY = "is_long_running";
  public static final String A2A_DATA_PART_METADATA_TYPE_FUNCTION_CALL = "function_call";
  public static final String A2A_DATA_PART_METADATA_TYPE_FUNCTION_RESPONSE = "function_response";
  public static final String A2A_DATA_PART_METADATA_TYPE_CODE_EXECUTION_RESULT =
      "code_execution_result";
  public static final String A2A_DATA_PART_METADATA_TYPE_EXECUTABLE_CODE = "executable_code";

  /** Converts an A2A Part to a Google GenAI Part. */
  public static Part convertA2APartToGenaiPart(io.a2a.spec.Part a2aPart) {
    if (a2aPart instanceof io.a2a.spec.TextPart) {
      return Part.builder().text(((TextPart) a2aPart).getText()).build();
    }
    if (a2aPart instanceof FilePart) {
      FilePart filePart = (FilePart) a2aPart;
      Object file = filePart.getFile();
      if (file instanceof FileWithUri) {
        FileWithUri uriFile = (FileWithUri) file;
        return Part.builder()
            .fileData(
                FileData.builder().fileUri(uriFile.uri()).mimeType(uriFile.mimeType()).build())
            .build();
      } else if (file instanceof FileWithBytes) {
        FileWithBytes bytesFile = (FileWithBytes) file;
        byte[] decodedData = Base64.getDecoder().decode(bytesFile.bytes());
        return Part.builder()
            .inlineData(Blob.builder().data(decodedData).mimeType(bytesFile.mimeType()).build())
            .build();
      } else {
        return null;
      }
    }
    if (a2aPart instanceof DataPart) {
      DataPart dataPart = (DataPart) a2aPart;
      Map<String, Object> metadata = dataPart.getMetadata();

      if (metadata != null
          && metadata.containsKey(
              ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY))) {
        String partType =
            metadata
                .get(ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY))
                .toString();

        switch (partType) {
          case A2A_DATA_PART_METADATA_TYPE_FUNCTION_CALL:
            return Part.builder()
                .functionCall(FunctionCall.fromJson(dataPart.getData().toString()))
                .build();
          case A2A_DATA_PART_METADATA_TYPE_FUNCTION_RESPONSE:
            return Part.builder()
                .functionResponse(FunctionResponse.fromJson(dataPart.getData().toString()))
                .build();
          case A2A_DATA_PART_METADATA_TYPE_CODE_EXECUTION_RESULT:
            return Part.builder()
                .codeExecutionResult(CodeExecutionResult.fromJson(dataPart.getData().toString()))
                .build();
          case A2A_DATA_PART_METADATA_TYPE_EXECUTABLE_CODE:
            return Part.builder()
                .executableCode(ExecutableCode.fromJson(dataPart.getData().toString()))
                .build();
          default:
            return Part.builder().text(dataPart.getData().toString()).build();
        }
      } else {
        return Part.builder().text(dataPart.getData().toString()).build();
      }
    }

    return null;
  }

  /** Converts a Google GenAI Part to an A2A Part. */
  public static io.a2a.spec.Part convertGenaiPartToA2APart(Part part) {
    if (part.text().isPresent()) {
      TextPart a2aPart = new TextPart(part.text().get());
      if (part.thought().isPresent()) {
        a2aPart
            .getMetadata()
            .put(ConverterUtils.getAdkMetadataKey("thought"), part.thought().get());
      }
      return a2aPart;
    }

    if (part.fileData().isPresent()) {
      FileData fileData = part.fileData().get();
      return new FilePart(
          new FileWithUri(
              fileData.mimeType().orElse(null),
              fileData.displayName().orElse(null),
              fileData.fileUri().orElse(null)));
    }

    if (part.inlineData().isPresent()) {
      Blob inlineData = part.inlineData().get();
      if (inlineData.mimeType().isPresent() && inlineData.data().isPresent()) {
        String encodedData = Base64.getEncoder().encodeToString(inlineData.data().get());
        FileWithBytes bytesFile =
            new FileWithBytes(
                encodedData, inlineData.displayName().orElse(null), inlineData.mimeType().get());
        FilePart a2aFilePart = new FilePart(bytesFile);

        if (part.videoMetadata().isPresent()) {
          a2aFilePart
              .getMetadata()
              .put(
                  ConverterUtils.getAdkMetadataKey("video_metadata"),
                  part.videoMetadata().get().toString());
        }

        return a2aFilePart;
      }
      return null;
    }

    try {
      if (part.functionCall().isPresent()) {
        FunctionCall functionCall = part.functionCall().get();
        return new DataPart(
            JsonBaseModel.getMapper().readValue(functionCall.toJson(), HashMap.class),
            Map.of(
                ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY),
                A2A_DATA_PART_METADATA_TYPE_FUNCTION_CALL));
      }

      if (part.functionResponse().isPresent()) {
        FunctionResponse functionResponse = part.functionResponse().get();
        return new DataPart(
            JsonBaseModel.getMapper().readValue(functionResponse.toJson(), HashMap.class),
            Map.of(
                ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY),
                A2A_DATA_PART_METADATA_TYPE_FUNCTION_RESPONSE));
      }

      if (part.codeExecutionResult().isPresent()) {
        CodeExecutionResult result = part.codeExecutionResult().get();
        return new DataPart(
            JsonBaseModel.getMapper().readValue(result.toJson(), HashMap.class),
            Map.of(
                ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY),
                A2A_DATA_PART_METADATA_TYPE_CODE_EXECUTION_RESULT));
      }

      if (part.executableCode().isPresent()) {
        ExecutableCode executableCode = part.executableCode().get();
        return new DataPart(
            JsonBaseModel.getMapper().readValue(executableCode.toJson(), HashMap.class),
            Map.of(
                ConverterUtils.getAdkMetadataKey(A2A_DATA_PART_METADATA_TYPE_KEY),
                A2A_DATA_PART_METADATA_TYPE_EXECUTABLE_CODE));
      }
    } catch (JsonProcessingException e) {
      logger.warning("Failed convert part for Google GenAI part: " + part.toJson());
      throw new IllegalStateException("Failed convert part for Google GenAI part:", e);
    }
    logger.warning("Cannot convert unsupported part for Google GenAI part: " + part.toJson());
    return null;
  }
}
