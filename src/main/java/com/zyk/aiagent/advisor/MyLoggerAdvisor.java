//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zyk.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public int getOrder() {
		return 100;
	}

	private AdvisedRequest before(AdvisedRequest request) {
		log.info("AI Request: {}", request.userText());
		return request;
	}

	private void observeAfter(AdvisedResponse advisedResponse) {
		log.info("AI Request:{}",advisedResponse.response().getResult().getOutput().getText());
	}

	public String toString() {
		return SimpleLoggerAdvisor.class.getSimpleName();
	}

	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		advisedRequest = this.before(advisedRequest);
		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
		this.observeAfter(advisedResponse);
		return advisedResponse;
	}

	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		advisedRequest = this.before(advisedRequest);
		Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
		return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
	}
}
