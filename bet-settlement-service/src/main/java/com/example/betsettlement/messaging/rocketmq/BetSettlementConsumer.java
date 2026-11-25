package com.example.betsettlement.messaging.rocketmq;

import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.model.message.BetSettlementMessage;
import com.example.betsettlement.service.BetSettlementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class BetSettlementConsumer implements MessageListenerConcurrently {
  BetSettlementService betSettlementService;
  JsonMapper jsonMapper;

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(
      List<MessageExt> messages, ConsumeConcurrentlyContext context) {
    try {
      messages.forEach(this::settleBet);
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception e) {
      log.warn("Bet settlement failed, message will be redelivered", e);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

  private void settleBet(MessageExt message) {
    var betSettlement = jsonMapper.readValue(message.getBody(), BetSettlementMessage.class);
    log.info("Bet settlement received: {}", betSettlement);
    betSettlementService.settleBet(betSettlement);
  }
}
