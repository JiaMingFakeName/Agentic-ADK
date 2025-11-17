package com.google.adk.a2a.executor;

import io.a2a.spec.Event;
import io.a2a.spec.Message;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatusUpdateEvent;

/** Aggregates the task status updates and provides the final task state. */
public class TaskResultAggregator {

  private TaskState taskState;
  private Message taskStatusMessage;

  /** Constructor initializes the task state to working and message to null. */
  public TaskResultAggregator() {
    this.taskState = TaskState.WORKING;
    this.taskStatusMessage = null;
  }

  /**
   * Processes an event from the agent run and detects signals about the task status. Priority of
   * task state: - FAILED - AUTH_REQUIRED - INPUT_REQUIRED - WORKING
   *
   * @param event the incoming event to be processed
   */
  public void processEvent(Event event) {
    if (event instanceof TaskStatusUpdateEvent) {
      TaskStatusUpdateEvent statusEvent = (TaskStatusUpdateEvent) event;
      TaskState newState = statusEvent.getStatus().state();
      Message newMessage = statusEvent.getStatus().message();

      if (newState == TaskState.FAILED) {
        this.taskState = TaskState.FAILED;
        this.taskStatusMessage = newMessage;
      } else if (newState == TaskState.AUTH_REQUIRED && this.taskState != TaskState.FAILED) {
        this.taskState = TaskState.AUTH_REQUIRED;
        this.taskStatusMessage = newMessage;
      } else if (newState == TaskState.INPUT_REQUIRED
          && this.taskState != TaskState.FAILED
          && this.taskState != TaskState.AUTH_REQUIRED) {
        this.taskState = TaskState.INPUT_REQUIRED;
        this.taskStatusMessage = newMessage;
      } else if (this.taskState == TaskState.WORKING) {
        this.taskStatusMessage = newMessage;
      }
      // After processing, reset the event's state to WORKING
      //            statusEvent.getStatus()(TaskState.WORKING);
    }
  }

  /**
   * Returns the current task state.
   *
   * @return the current TaskState
   */
  public TaskState getTaskState() {
    return taskState;
  }

  /**
   * Returns the current task status message.
   *
   * @return the task status message or null
   */
  public Message getTaskStatusMessage() {
    return taskStatusMessage;
  }
}
