package com.abhi.aws.sns.service;

import com.abhi.aws.sns.model.GiftCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.List;

@Service
public class AwsSnsService {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AwsSnsService(SnsClient snsClient) {
        this.snsClient = snsClient;
        this.objectMapper = new ObjectMapper();
    }

    // Creates a new SNS topic
    public String createTopic(String topicName) {
        CreateTopicRequest createTopicRequest = CreateTopicRequest.builder()
                .name(topicName)
                .build();
        CreateTopicResponse response = snsClient.createTopic(createTopicRequest);
        return response.topicArn();
    }

    // Lists all SNS topics
    public List<String> listTopics() {
        ListTopicsRequest listTopicsRequest = ListTopicsRequest.builder().build();
        ListTopicsResponse response = snsClient.listTopics(listTopicsRequest);
        return response.topics().stream()
                .map(Topic::topicArn)
                .toList();
    }

    // Subscribes an email to a topic
    public String subscribeEmail(String topicArn, String email) {
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .protocol("email")
                .topicArn(topicArn)
                .endpoint(email)
                .build();
        SubscribeResponse response = snsClient.subscribe(subscribeRequest);
        return response.subscriptionArn();
    }

    // Publishes a notification to a topic
    public String publishNotification(String topicArn, String subject, GiftCard giftCard) throws Exception {
        String message = formatGiftCardMessage(giftCard);
        
        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();
        
        PublishResponse response = snsClient.publish(publishRequest);
        return response.messageId();
    }

    // Deletes a topic
    public void deleteTopic(String topicArn) {
        DeleteTopicRequest deleteTopicRequest = DeleteTopicRequest.builder()
                .topicArn(topicArn)
                .build();
        snsClient.deleteTopic(deleteTopicRequest);
    }

    // Formats gift card message for email
    private String formatGiftCardMessage(GiftCard giftCard) {
        return String.format(
            "Gift Card Notification\n\n" +
            "User: %s\n" +
            "Gift Card Type: %s\n" +
            "Amount: $%.2f\n" +
            "Date: %s\n\n" +
            "This gift card has been processed and will be delivered soon.",
            giftCard.getUserName(),
            giftCard.getGiftCardType(),
            giftCard.getAmount(),
            giftCard.getDate()
        );
    }
}
