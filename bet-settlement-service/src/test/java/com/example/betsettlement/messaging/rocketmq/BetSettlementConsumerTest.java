package com.example.betsettlement.messaging.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.betsettlement.model.BetStatus;
import com.example.betsettlement.model.message.BetSettlementMessage;
import com.example.betsettlement.service.BetSettlementService;
import java.util.List;
import java.util.UUID;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@ContextConfiguration(classes = {BetSettlementConsumer.class, JacksonAutoConfiguration.class})
class BetSettlementConsumerTest {
  @MockitoBean BetSettlementService betSettlementService;
  @Autowired BetSettlementConsumer betSettlementConsumer;
  @Autowired JsonMapper jsonMapper;

  @Test
  void shouldSettleBet() {
    // given
    var betSettlement = generateTestBetSettlement();

    // when
    var status = betSettlementConsumer.consumeMessage(generateTestMessages(betSettlement), null);

    // then
    assertThat(status).isEqualTo(ConsumeConcurrentlyStatus.CONSUME_SUCCESS);
    verify(betSettlementService, times(1)).settleBet(betSettlement);
  }

  @Test
  void shouldRetryOnFailure() {
    // given
    var betSettlement = generateTestBetSettlement();
    doThrow(new QueryTimeoutException("Not available"))
        .when(betSettlementService)
        .settleBet(betSettlement);

    // when
    var status = betSettlementConsumer.consumeMessage(generateTestMessages(betSettlement), null);

    // then
    assertThat(status).isEqualTo(ConsumeConcurrentlyStatus.RECONSUME_LATER);
  }

  private List<MessageExt> generateTestMessages(BetSettlementMessage betSettlement) {
    var message = new MessageExt();
    message.setBody(jsonMapper.writeValueAsBytes(betSettlement));
    return List.of(message);
  }

  private BetSettlementMessage generateTestBetSettlement() {
    return new BetSettlementMessage(UUID.randomUUID(), BetStatus.WON);
  }
}
