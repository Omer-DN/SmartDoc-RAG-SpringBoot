package org.example.notebooklm.service;

import org.springframework.stereotype.Service;

@Service
public class TopKSelector {
    public int selectTopK(String question) {
        // פשוט מחזיר 5 chunks
        return 5;
    }
}
