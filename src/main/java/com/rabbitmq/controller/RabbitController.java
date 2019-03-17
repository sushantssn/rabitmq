package com.rabbitmq.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.rabbitmq.controller.*;

@Controller
public class RabbitController {

	final static String queueName = "myQueue";

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RabbitAdmin admin;
	
	@Autowired
	Queue queue;
	
	@Autowired
	TopicExchange exchange;

	
	@Bean
	Queue queue() {
		return new Queue(queueName, true);
	}

	@Bean
	TopicExchange exchange() {
		return new TopicExchange("fanout");
	}

	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("foo.*");
	}

	@Bean
	RabbitAdmin admin(CachingConnectionFactory connectionFactory, Queue queue, TopicExchange exchange) {

		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	CachingConnectionFactory connectionFactory() {

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("barnacle.rmq.cloudamqp.com");
		connectionFactory.setUsername("hlibvhjp");
		connectionFactory.setPassword("PG5GP3g042hhd7a9ZvqGluGDuTM4WTML");
		connectionFactory.setVirtualHost("hlibvhjp");

		return connectionFactory;
	}

	@Bean
	SimpleMessageListenerContainer container(CachingConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	Receiver receiver() {
		return new Receiver();
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@RequestMapping("/hello/{name}")
	String hello(@PathVariable String name) throws Exception {

		admin.declareQueue(queue);
		admin.declareExchange(exchange);
		admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("foo.*"));

		rabbitTemplate.convertAndSend("fanout", "foo.bar", "Hello CloudAMQP!");
		receiver().getLatch().await(10000, TimeUnit.MILLISECONDS);
		// running the jar crashes if we don't deactivate this...
		// context.close();
		return "Hello, " + name + "!";
	}

}
