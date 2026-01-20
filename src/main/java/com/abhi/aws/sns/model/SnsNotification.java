package com.abhi.aws.sns.model;

public class SnsNotification {
    private String messageId;
    private String topicArn;
    private String subject;

    public SnsNotification() {
    }

    public SnsNotification(String messageId, String topicArn, String subject) {
        this.messageId = messageId;
        this.topicArn = topicArn;
        this.subject = subject;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
