package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlogPost implements IPage {
  @JsonProperty(required = true)
  String content;
}