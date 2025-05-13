package com.RecipeManagement.Recipe.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.index.TextIndexDefinition.TextIndexDefinitionBuilder;
import jakarta.annotation.PostConstruct;

@Configuration
public class MongoDBConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        // Create text index on title and instructions fields for full-text search
        TextIndexDefinition textIndex = new TextIndexDefinitionBuilder()
                .onField("title", 1.0f)
                .onField("ingredients", 1.0f)
                .onField("instructions", 1.0f)
                .onField("category", 1.0f)
                .build();

        mongoTemplate.indexOps("Recipe").ensureIndex(textIndex);
    }
}