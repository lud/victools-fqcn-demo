## Run

To run the schema generation call.

```bash
mvn clean compile
mvn com.github.victools:jsonschema-maven-plugin:generate
```

If you have `just`, then `just regen-schemas`.


## What we want to do

There are two classes using a mapped subtype:

```java
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

public class UpdatePage {
  @JsonProperty(required = true)
  String userId;

  @JsonProperty(required = true)
  PageType updateType;

  @JsonProperty(required = true)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "updateType", include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
  @JsonSubTypes(value = {
      @JsonSubTypes.Type(value = BlogPost.class, name = "BLOG_POST"),
      @JsonSubTypes.Type(value = WikiPage.class, name = "WIKI_PAGE"),
  })
  IPage pagePayload;
}
```

We have the `JsonSubTypes` and `JsonTypeInfo` annotation over the properties of
the using classes, as the property name or the allowed subtypes can change.

Here the property is either `createType` or `updateType`. Of course this is a contrived example.

The module adding custom attributes adds two attributes, using custom definitions:

- `$passedToCustomDef: true`
- `$passedToCustomPropDef: true`


Unfortunately the schema object containing the `anyOf` attributes does not get
passed to our custom module:

```json
    "pagePayload" : {
      "anyOf" : [ {
        "type" : "object",
        "properties" : {
          "content" : {
            "type" : "string",
            "$fqcn" : "java.lang.String",
            "$passedToCustomDef" : true,
            "$passedToCustomPropDef" : true
          }
        },
        "required" : [ "content" ],
        "$fqcn" : "com.myapp.contracts.BlogPost",
        "$passedToCustomDef" : true,
        "$passedToCustomPropDef" : true
      }, {
        "type" : "object",
        "properties" : {
          "bbcode" : {
            "type" : "string",
            "$fqcn" : "java.lang.String",
            "$passedToCustomDef" : true,
            "$passedToCustomPropDef" : true
          }
        },
        "required" : [ "bbcode" ],
        "$fqcn" : "com.myapp.contracts.WikiPage",
        "$passedToCustomDef" : true,
        "$passedToCustomPropDef" : true
      } ]
    }
```