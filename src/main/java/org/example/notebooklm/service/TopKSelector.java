package org.example.notebooklm.service;

import org.springframework.stereotype.Component;

@Component
public class TopKSelector {

    public int selectTopK(String question) {
        int length = question.length();

        if (length < 50) return 3;
        if (length < 150) return 5;
        if (length < 300) return 8;

        return 10;
    }
}
