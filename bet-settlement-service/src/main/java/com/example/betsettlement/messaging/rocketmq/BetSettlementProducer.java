package com.example.betsettlement.messaging.rocketmq;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.config.RocketMqProperties;
import com.example.betsettlement.exception.BetSettlementPublishingException;
import com.example.betsettlement.model.message.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class BetSettlementProducer {
  DefaultMQProducer rocketMqProducer;
  RocketMqProperties properties;
  JsonMapper jsonMapper;

  public void send(BetSettlementMessage betSettlement) {
    var message = new Message(properties.topic(), jsonMapper.writeValueAsBytes(betSettlement));
    message.setKeys(betSettlement.betId().toString());

    var sendStatus = sendMessage(betSettlement, message);
    if (sendStatus != SendStatus.SEND_OK) {
      throw new BetSettlementPublishingException(
          format(
              "Settlement of bet with id %s was sent with %s", betSettlement.betId(), sendStatus));
    }
    log.info("Bet settlement sent: {}", betSettlement);
  }

  private SendStatus sendMessage(BetSettlementMessage betSettlement, Message message) {
    try {
      return rocketMqProducer.send(message).getSendStatus();
    } catch (Exception e) {
      throw new BetSettlementPublishingException(
          format("Settlement of bet with id %s was not sent", betSettlement.betId()), e);
    }
  }
}
