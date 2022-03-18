package com.myapp.contracts.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.members.RawField;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.CustomDefinitionProviderV2;
import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.CustomPropertyDefinitionProvider;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.SubtypeResolver;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.generator.CustomDefinition.AttributeInclusion;
import com.github.victools.jsonschema.generator.CustomDefinition.DefinitionType;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JsonSubTypesResolver;

public class FqcnModule extends SimpleModule implements Module {

  private static class SubTypeContextDefinitionProvider
      implements CustomDefinitionProviderV2,
      CustomPropertyDefinitionProvider

  {

    @Override
    public CustomDefinition provideCustomSchemaDefinition(ResolvedType javaType, SchemaGenerationContext context) {

      ObjectNode standardDefinition = context.createStandardDefinition(javaType, this);
      standardDefinition.put("$passedToCustomDef", true);
      return new CustomDefinition(standardDefinition, CustomDefinition.DefinitionType.STANDARD,
          CustomDefinition.AttributeInclusion.NO);
    }

    @Override
    public CustomPropertyDefinition provideCustomSchemaDefinition(MemberScope scope, SchemaGenerationContext context) {
      var javaType = scope.getType();
      ObjectNode standardDefinition = context.createStandardDefinition(javaType, this);
      standardDefinition.put("$passedToCustomPropDef", true);
      return new CustomPropertyDefinition(standardDefinition, CustomDefinition.AttributeInclusion.NO);
    }
  }

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder configBuilder) {

    configBuilder.forTypesInGeneral()
        // .withCustomDefinitionProvider(new SubTypeContextDefinitionProvider())
        .withTypeAttributeOverride((node, scope, context) -> {
          addFQCN(node, scope);
          addMapping(node, scope);

        });

    // configBuilder.forFields()
    // .withCustomDefinitionProvider(new SubTypeContextDefinitionProvider());

  }

  private ObjectNode addFQCN(ObjectNode node, TypeScope scope) {
    return node
        .put("$fqcn", scope.getType().getErasedType().getName());
  }

  private void addMapping(ObjectNode node, TypeScope scope) {

    System.out.println(String.format("scope: %s", scope));

    var mappedFields = new HashMap<String, ObjectNode>();
    var oMapper = new ObjectMapper();

    var properties = node.get("properties");

    if (null == properties)
      return;

    Arrays.asList(scope.getType().getErasedType().getFields())
        .forEach(
            field -> {
              JsonTypeInfo typeInfoAnnotation = field.getAnnotation(JsonTypeInfo.class);
              if (typeInfoAnnotation == null) {
                return;
              }
              System.out.println(String.format("%s has JsonTypeInfo", field));
              JsonSubTypes subTypesAnnotation = field.getAnnotation(JsonSubTypes.class);
              if (subTypesAnnotation == null) {
                return;
              }
              System.out.println(String.format("%s has JsonSubTypes", field));
              var discr = typeInfoAnnotation.property();
              var mapping = new HashMap<String, String>();
              for (JsonSubTypes.Type subType : subTypesAnnotation.value()) {
                mapping.put(subType.name(), subType.value().getName());
              }
              var spec = new ObjectNode(new JsonNodeFactory(false));
              spec.put("discr", discr);
              spec.set("mapping", oMapper.convertValue(mapping, ObjectNode.class));
              mappedFields.put(field.getName(), spec);
            });

    if (properties instanceof ObjectNode props) {
      props.fields().forEachRemaining((arg0) -> {
        System.out.println(String.format("arg0: %s", arg0));
        System.out.println(String.format("arg0.getKey(): %s", arg0.getKey()));
        System.out.println(
            String.format("mappedFields.containsKey(arg0.getKey()): %s", mappedFields.containsKey(arg0.getKey())));
        if (mappedFields.containsKey(arg0.getKey()) && arg0.getValue() instanceof ObjectNode propSchema) {
          var hasAnyOf = propSchema.has("anyOf");
          System.out.println(String.format("hasAnyOf: %s", hasAnyOf));
          propSchema.set("$mappedType", mappedFields.get(arg0.getKey()));
        }
      });
    }
    // System.out.println(String.format("node: %s", node));
  }

  // private List<RawField> getAllFields(TypeScope scope, ResolvedType resolvedType, ArrayList<RawField> acc) {
  //   resolvedType.getErasedType().getSuperclass();

  //   scope.getType().getMemberFields().forEach((field) -> acc.add(field));
  //   scope.getType().get

  // }
}