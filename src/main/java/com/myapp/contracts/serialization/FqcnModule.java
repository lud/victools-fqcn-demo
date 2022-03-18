package com.myapp.contracts.serialization;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;

public class FqcnModule extends SimpleModule implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder configBuilder) {

    configBuilder
        .forTypesInGeneral()
        .withTypeAttributeOverride((node, scope, context) -> {
          addFQCN(node, scope);
          addMapping(node, scope);
        });

  }

  private void addFQCN(ObjectNode node, TypeScope scope) {
    node.put("$fqcn", scope.getType().getErasedType().getName());
  }

  private void addMapping(ObjectNode node, TypeScope scope) {

    var properties = node.get("properties");

    if (null == properties)
      return;

    var mappedFields = getMappedFields(scope);

    if (properties instanceof ObjectNode props) {
      props.fields().forEachRemaining((propPair) -> {
        var propName = propPair.getKey();
        var propDef = propPair.getValue();
        if (mappedFields.containsKey(propName) && propDef instanceof ObjectNode propSchema) {
          propSchema.set("$mappedType", mappedFields.get(propName));
        }
      });
    }
  }

  private HashMap<String, ObjectNode> getMappedFields(TypeScope scope) {
    var oMapper = new ObjectMapper();
    var mappedFields = new HashMap<String, ObjectNode>();
    Arrays.asList(scope.getType().getErasedType().getFields())
        .forEach(field -> getFieldMapping(oMapper, mappedFields, field));
    return mappedFields;
  }

  private void getFieldMapping(ObjectMapper oMapper, HashMap<String, ObjectNode> mappedFields, Field field) {
    JsonTypeInfo typeInfoAnnotation = field.getAnnotation(JsonTypeInfo.class);
    if (typeInfoAnnotation == null) {
      return;
    }
    JsonSubTypes subTypesAnnotation = field.getAnnotation(JsonSubTypes.class);
    if (subTypesAnnotation == null) {
      return;
    }
    var discr = typeInfoAnnotation.property();
    var mapping = new HashMap<String, String>();
    for (JsonSubTypes.Type subType : subTypesAnnotation.value()) {
      mapping.put(subType.name(), subType.value().getName());
    }
    var spec = new ObjectNode(new JsonNodeFactory(false));
    spec.put("discr", discr);
    spec.set("mapping", oMapper.convertValue(mapping, ObjectNode.class));
    mappedFields.put(field.getName(), spec);
  }

}