package com.google.adk.a2a.converters;

import io.a2a.server.agentexecution.RequestContext;

/**
 * @author dlancerc
 * @date 2025-07-28-21:35
 */
public class RequestConverter {

  /**
   * Get user from call context if available (auth is enabled on a2a server)
   *
   * @param request
   * @return
   */
  public static String getUserId(RequestContext request) {
    if (request.getParams() != null
        && request.getParams().metadata() != null
        && request.getParams().metadata().containsKey("user_id")) {
      return request.getParams().metadata().get("user_id").toString();
    } else {
      return "A2A_USER_" + request.getContextId();
    }
  }
}
