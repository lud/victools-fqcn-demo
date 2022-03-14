package com.myapp.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class CreatePage {
  @JsonProperty(required = true)
  String userId;

  @JsonProperty(required = true)
  PageType createType;

  @JsonProperty(required = true)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "createType", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
  @JsonSubTypes(value = {
      @JsonSubTypes.Type(value = BlogPost.class, name = "BLOG_POST"),
      @JsonSubTypes.Type(value = WikiPage.class, name = "WIKI_PAGE"),
  })
  IPage pagePayload;
}
