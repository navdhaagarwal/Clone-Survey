package com.nucleus.questionairePdf;

import com.nucleus.letterMaster.LetterType;

import java.util.List;

public interface QuestionairePdfHandler {
    void addOptionalLetterTypesToList(List<LetterType> letterTypeList);
}
