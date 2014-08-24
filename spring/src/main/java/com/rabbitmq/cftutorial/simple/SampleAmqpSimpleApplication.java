/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rabbitmq.cftutorial.simple;

import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleAmqpSimpleApplication {
	public static final String QUEUE_NAME = "myQueue";

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@PostConstruct
	public void setUpQueue() {
	    Queue queue = new Queue(QUEUE_NAME);
	    this.amqpAdmin.declareQueue(queue);
		TopicExchange exchange = new TopicExchange("chatExchange");
		this.amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("*"));
	}
	
	@Bean
	public SimpleMessageListenerContainer container() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				this.connectionFactory);
		Object listener = new Object() {
			@SuppressWarnings("unused")
			public void handleMessage(Object foo) throws JSONException, UnsupportedEncodingException {
				byte[] boo = (byte[]) foo;
				String newString = new String(boo, "UTF-8");
				JSONObject jsonObject = new JSONObject(newString);
				System.out.println("Received Message" + jsonObject.toString());
			}
		};
		MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
		container.setMessageListener(adapter);
		container.setQueueNames(QUEUE_NAME);
		return container;
	}
}
