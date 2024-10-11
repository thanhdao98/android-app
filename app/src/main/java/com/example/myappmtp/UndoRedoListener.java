package com.example.myappmtp;

public interface UndoRedoListener {
    void onUpdateButtonState(boolean canUndo, boolean canRedo);
}
