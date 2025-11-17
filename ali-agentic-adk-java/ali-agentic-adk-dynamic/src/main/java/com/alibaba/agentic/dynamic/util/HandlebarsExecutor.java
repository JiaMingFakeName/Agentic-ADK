package com.alibaba.agentic.dynamic.util;

import com.alibaba.fastjson.JSON;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;

public class HandlebarsExecutor {
    public static void main(String[] args) {
        System.out.println(
                new HandlebarsExecutor().execute("{{#each this}}{{this}}{{/each}}", new String[]{"1","2","3"})
        );
    }
    public String execute(String code, Object input) {
        // Create a new Handlebars instance
        Handlebars handlebars = new Handlebars();

        // Define a template as a string
        String source = code;

        try {
            // Compile the template
            Template template = handlebars.compileInline(source);
            if(input instanceof String){
                input = JSON.parse((String) input);
            }

            // Apply the template to the data model
            String output = template.apply(input);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
