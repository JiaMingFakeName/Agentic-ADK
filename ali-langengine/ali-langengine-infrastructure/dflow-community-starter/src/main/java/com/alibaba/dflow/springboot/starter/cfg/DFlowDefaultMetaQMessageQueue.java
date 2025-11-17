package com.alibaba.dflow.springboot.starter.cfg;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.dflow.DFlow.SimpleMessage;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 基于阿里云 RocketMQ SDK 的 DFlow 消息队列实现
 */
@Configuration
@ConditionalOnMissingBean(SubscribableChannel.class)
public class DFlowDefaultMetaQMessageQueue implements SubscribableChannel {

    private static final Logger logger = LoggerFactory.getLogger(DFlowDefaultMetaQMessageQueue.class);

    private final String cid;
    private final String topic;
    private final List<MessageHandler> messageHandlers = new ArrayList<>();

    private DefaultMQProducer producer;

    public DFlowDefaultMetaQMessageQueue(String cid, String topic) {
        this.cid = cid;
        this.topic = topic;
    }

    /**
     * 初始化消费者和生产者
     */
    public void init() throws MQClientException {

        /* ------------ 消费者 ------------ */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(cid);
        consumer.subscribe(topic, "*");
        consumer.setPullInterval(0);
        consumer.setConsumeThreadMin(32);
        consumer.setConsumeThreadMax(64);
        consumer.setConsumeMessageBatchMaxSize(1);

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    SimpleMessage toChannel = new SimpleMessage("", new String(msg.getBody()));
                    messageHandlers.forEach(h -> h.handleMessage(toChannel));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();

        /* ------------ 生产者 ------------ */
        producer = new DefaultMQProducer(cid + "_PRODUCER");
        // 如果使用了阿里云 RocketMQ 实例，可在这里配置 NameServer 地址：
        // producer.setNamesrvAddr("xxx.mq-internal.aliyuncs.com:9876");
        producer.start();
    }

    /* ============ SubscribableChannel 实现 ============ */

    @Override
    public boolean subscribe(MessageHandler handler) {
        return messageHandlers.add(handler);
    }

    @Override
    public boolean unsubscribe(MessageHandler handler) {
        return messageHandlers.remove(handler);
    }

    /* ============ MessageChannel 实现 ============ */

    @Override
    public boolean send(Message<?> message) {
        org.apache.rocketmq.common.message.Message toSend;
        byte[] body;
        if (message.getPayload() instanceof byte[]) {
            body = (byte[]) message.getPayload();
        } else if (message.getPayload() instanceof String) {
            body = ((String) message.getPayload()).getBytes();
        } else {
            throw new UnsupportedOperationException("Unsupported payload type: " + message.getPayload().getClass());
        }
        toSend = new org.apache.rocketmq.common.message.Message(topic, body);

        try {
            SendResult sendResult = producer.send(toSend);
            return sendResult != null && sendResult.getSendStatus() != null
                && "SEND_OK".equals(sendResult.getSendStatus().name());
        } catch (Exception e) {
            logger.error("DFlow adapter send RocketMQ failed", e);
            return false;
        }
    }

    @Override
    public boolean send(Message<?> message, long timeout) {
        // 如需支持超时发送，可在此调用 producer.send(msg, timeout)
        return false;
    }
}