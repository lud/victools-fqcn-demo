package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WikiPage implements IPage {
  @JsonProperty(required = true)
  String bbcode;
}