package com.mkyong;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

public class TestConnection {
	public static void main(String[] args) {
		  // set up the connection
		  CachingConnectionFactory connectionFactory=new CachingConnectionFactory("barnacle.rmq.cloudamqp.com");
		  connectionFactory.setUsername("hlibvhjp");
		  connectionFactory.setPassword("PG5GP3g042hhd7a9ZvqGluGDuTM4WTML");
		  connectionFactory.setVirtualHost("hlibvhjp");

		  //Recommended settings
		  connectionFactory.setRequestedHeartBeat(30);
		  connectionFactory.setConnectionTimeout(30000);

		  //Set up queue, exchanges and bindings
		  RabbitAdmin admin = new RabbitAdmin(connectionFactory);
		  Queue queue = new Queue("myQueue");
		  admin.declareQueue(queue);
		  TopicExchange exchange = new TopicExchange("fanout");
		  admin.declareExchange(exchange);
		  admin.declareBinding(
		    BindingBuilder.bind(queue).to(exchange).with("foo.*"));

		  //Set up the listener
		  SimpleMessageListenerContainer container =
		    new SimpleMessageListenerContainer(connectionFactory);
		  Object listener = new Object() {
		    public void handleMessage(String foo) {
		      System.out.println(foo);
		    }
		  };

		  //Send a message
		  MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
		  container.setMessageListener(adapter);
		  container.setQueueNames("myQueue");
		  container.start();

		  RabbitTemplate template = new RabbitTemplate(connectionFactory);
		  template.convertAndSend("fanout", "foo.bar", "Hello CloudAMQP!");
		  try{
		    Thread.sleep(1000);
		  } catch(InterruptedException e) {
		     Thread.currentThread().interrupt();
		  }
		  container.stop();
		}
}
