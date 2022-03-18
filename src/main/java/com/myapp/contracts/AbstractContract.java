package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractContract {
  @JsonProperty(required = true)
  public Metadata metadata;

}