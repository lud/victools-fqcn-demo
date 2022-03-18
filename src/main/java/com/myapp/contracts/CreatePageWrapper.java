package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePageWrapper extends CreatePage {
  @JsonProperty(required = true)
  public boolean isWrapper = true;
}