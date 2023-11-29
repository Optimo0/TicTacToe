package com.example.tictactoe.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BoardConverter implements AttributeConverter<String[][], String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(String[][] attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting board to JSON", e);
        }
    }

    @Override
    public String[][] convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, String[][].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting board from JSON", e);
        }
    }
}
