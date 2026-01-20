package com.abhi.aws.sns.controller;

import com.abhi.aws.sns.model.GiftCard;
import com.abhi.aws.sns.service.AwsSnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sns")
public class AwsSnsController {

    @Autowired
    private AwsSnsService awsSnsService;

    // Creates a new SNS topic
    @PostMapping("/create-topic")
    public ResponseEntity<?> createTopic(@RequestParam String topicName) {
        try {
            String topicArn = awsSnsService.createTopic(topicName);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Topic created successfully");
            response.put("topicArn", topicArn);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create topic: " + e.getMessage());
        }
    }

    // Lists all SNS topics
    @GetMapping("/list-topics")
    public ResponseEntity<?> listTopics() {
        try {
            List<String> topicArns = awsSnsService.listTopics();
            return ResponseEntity.ok(topicArns);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to list topics: " + e.getMessage());
        }
    }

    // Subscribes an email to a topic
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribeEmail(@RequestParam String topicArn, 
                                           @RequestParam String email) {
        try {
            String subscriptionArn = awsSnsService.subscribeEmail(topicArn, email);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Subscription request sent - Check email to confirm");
            response.put("subscriptionArn", subscriptionArn);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to subscribe: " + e.getMessage());
        }
    }

    // Publishes notification to topic
    @PostMapping("/publish")
    public ResponseEntity<?> publishNotification(@RequestBody Map<String, Object> request) {
        try {
            String topicArn = (String) request.get("topicArn");
            String subject = (String) request.get("subject");
            Map<String, Object> giftCardData = (Map<String, Object>) request.get("giftCard");
            
            GiftCard giftCard = new GiftCard(
                    (String) giftCardData.get("userName"),
                    (String) giftCardData.get("giftCardType"),
                    Double.parseDouble(giftCardData.get("amount").toString()),
                    (String) giftCardData.get("date")
            );
            
            String messageId = awsSnsService.publishNotification(topicArn, subject, giftCard);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification sent successfully");
            response.put("messageId", messageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to publish notification: " + e.getMessage());
        }
    }

    // Deletes a topic
    @DeleteMapping("/delete-topic")
    public ResponseEntity<?> deleteTopic(@RequestParam String topicArn) {
        try {
            awsSnsService.deleteTopic(topicArn);
            return ResponseEntity.ok("Topic deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete topic: " + e.getMessage());
        }
    }
}
