package com.myapp.contracts.serialization;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        .withCustomDefinitionProvider(new SubTypeContextDefinitionProvider())
        .withTypeAttributeOverride((node, scope, context) -> node
            .put("$fqcn", scope.getType().getErasedType().getName()));

    configBuilder.forFields()
        .withCustomDefinitionProvider(new SubTypeContextDefinitionProvider());

  }
}