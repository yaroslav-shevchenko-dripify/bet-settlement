package com.example.betsettlement.messaging.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.example.betsettlement.config.RocketMqProperties;
import com.example.betsettlement.exception.BetSettlementPublishingException;
import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.model.message.BetSettlementMessage;
import java.util.UUID;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@ContextConfiguration(classes = {BetSettlementProducer.class, JacksonAutoConfiguration.class})
@EnableConfigurationProperties(RocketMqProperties.class)
class BetSettlementProducerTest {
  @MockitoBean DefaultMQProducer rocketMqProducer;
  @Autowired BetSettlementProducer betSettlementProducer;
  @Autowired JsonMapper jsonMapper;

  @Test
  void shouldSendMessage() throws Exception {
    // given
    var betSettlement = generateTestBetSettlement();
    doReturn(generateSendResult(SendStatus.SEND_OK))
        .when(rocketMqProducer)
        .send(any(Message.class));

    // when
    betSettlementProducer.send(betSettlement);

    // then
    var captor = ArgumentCaptor.<Message>captor();
    verify(rocketMqProducer).send(captor.capture());
    var message = captor.getValue();
    assertThat(message.getTopic()).isEqualTo("bet-settlements");
    assertThat(message.getKeys()).isEqualTo(betSettlement.betId().toString());
    assertThat(jsonMapper.readValue(message.getBody(), BetSettlementMessage.class))
        .isEqualTo(betSettlement);
  }

  @Test
  void shouldFailWhenMessageIsNotAccepted() throws Exception {
    // given
    var betSettlement = generateTestBetSettlement();
    doReturn(generateSendResult(SendStatus.FLUSH_DISK_TIMEOUT))
        .when(rocketMqProducer)
        .send(any(Message.class));

    // then
    assertThatThrownBy(() -> betSettlementProducer.send(betSettlement))
        .isInstanceOf(BetSettlementPublishingException.class);
  }

  private SendResult generateSendResult(SendStatus sendStatus) {
    var sendResult = new SendResult();
    sendResult.setSendStatus(sendStatus);
    return sendResult;
  }

  private BetSettlementMessage generateTestBetSettlement() {
    return new BetSettlementMessage(UUID.randomUUID(), BetStatus.WON);
  }
}
