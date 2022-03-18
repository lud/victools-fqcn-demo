package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata {
  @JsonProperty(required = true)
  public String requestId;
}