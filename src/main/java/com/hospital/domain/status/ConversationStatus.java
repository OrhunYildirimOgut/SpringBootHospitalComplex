// src/main/java/com/hospital/domain/status/ConversationStatus.java

package com.hospital.domain.status;
// Conversation status active or closed

public enum ConversationStatus {
    ACTIVE {
        @Override
        public boolean canSend(){
            return true;
        }
        @Override
        public boolean canClose(ConversationStatus conversationStatus){
            return conversationStatus == CLOSED;
        }
    },
    CLOSED {
        @Override
        public boolean canSend(){
            return false;
        }
        @Override
        public boolean canClose(ConversationStatus conversationStatus){
            return false;
        }
    };
    public abstract boolean canSend();
    public abstract boolean canClose(ConversationStatus conversationStatus);
}
