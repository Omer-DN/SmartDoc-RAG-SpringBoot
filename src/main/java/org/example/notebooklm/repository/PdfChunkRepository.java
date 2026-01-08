package org.example.notebooklm.repository;

import org.example.notebooklm.model.PdfChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PdfChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public PdfChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // זו המתודה ששומרת - קראנו לה saveChunk
    public void saveChunk(PdfChunk chunk, float[] vector) {
        String sql = "INSERT INTO pdf_chunks (text, chunk_index, pdf_id, embedding) VALUES (?, ?, ?, ?::vector)";
        jdbcTemplate.update(sql,
                chunk.getText(),
                chunk.getChunkIndex(),
                chunk.getPdfDocument().getId(),
                vector);
    }

    // המתודה הזו מקבלת float[] queryVector ולא String
    public List<PdfChunk> findSimilarChunks(long pdfId, float[] queryVector, int limit) {
        String sql = "SELECT id, text, chunk_index FROM pdf_chunks WHERE pdf_id = ? " +
                "ORDER BY embedding <=> ?::vector LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PdfChunk chunk = new PdfChunk();
            chunk.setId(rs.getLong("id"));
            chunk.setText(rs.getString("text"));
            chunk.setChunkIndex(rs.getInt("chunk_index"));
            return chunk;
        }, pdfId, queryVector, limit);
    }
}