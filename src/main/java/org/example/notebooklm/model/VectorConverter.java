package org.example.notebooklm.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Locale;

/**
 * Converter between float[] and PostgreSQL pgvector.
 * Ensures Hibernate binds the parameter as PGobject("vector", "[...]").
 */
@Converter
public class VectorConverter implements AttributeConverter<float[], Object> {

    @Override
    public Object convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            return null;
        }
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        try {
            pgObject.setValue(formatVector(attribute));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert embedding to pgvector", e);
        }
        return pgObject;
    }

    @Override
    public float[] convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }

        if (dbData instanceof PGobject pg && "vector".equalsIgnoreCase(pg.getType())) {
            return parseVector(pg.getValue());
        }

        if (dbData instanceof double[] doubleArray) {
            float[] floatArray = new float[doubleArray.length];
            for (int i = 0; i < doubleArray.length; i++) {
                floatArray[i] = (float) doubleArray[i];
            }
            return floatArray;
        }

        if (dbData instanceof float[] floatArray) {
            return floatArray;
        }

        throw new RuntimeException("Unexpected type for vector: " + dbData.getClass());
    }

    private String formatVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(String.format(Locale.US, "%.10f", embedding[i]));
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] parseVector(String value) {
        if (value == null || value.isBlank()) {
            return new float[0];
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            return new float[0];
        }
        String[] parts = trimmed.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i]);
        }
        return result;
    }
}