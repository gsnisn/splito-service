package com.splito.kafka;

import com.splito.event.ExpenseCreatedEvent;
import com.splito.event.GroupCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.expense-created}")
    private String expenseCreatedTopic;

    @Value("${app.kafka.topics.group-created}")
    private String groupCreatedTopic;

    public void publishExpenseCreated(ExpenseCreatedEvent event) {
        String key = String.valueOf(event.getGroupId());
        kafkaTemplate.send(expenseCreatedTopic, key, event);
    }

    public void publishGroupCreated(GroupCreatedEvent event) {
        String key = String.valueOf(event.getGroupId());
        kafkaTemplate.send(groupCreatedTopic, key, event);
    }
}