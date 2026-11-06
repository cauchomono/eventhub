package com.spring.eventhub.config.JacksonConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration 
public class JacksonConfig {
    @Bean 
    public SimpleModule trimStringModule(){
        SimpleModule module = new SimpleModule();

        module.addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
            
       @Override
      
       public String deserialize(JsonParser p, DeserializationContext ctxt){
        String value = p.getValueAsString();
        return(value != null) ? value.trim() : null;
       } 

        });

        return module;
    }

}
