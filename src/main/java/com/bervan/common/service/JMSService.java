package com.bervan.common.service;

public abstract class JMSService {

    public abstract void convertAndSend(String queueName, Object object, String jmsGroupId);
}
