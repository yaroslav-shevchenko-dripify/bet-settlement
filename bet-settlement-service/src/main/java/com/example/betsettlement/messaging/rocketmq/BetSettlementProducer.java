package com.example.betsettlement.messaging.rocketmq;

import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import com.example.betsettlement.config.RocketMqProperties;
import com.example.betsettlement.exception.BetSettlementPublishingException;
import com.example.betsettlement.model.message.BetSettlementMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
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

    try {
      var sendStatus = rocketMqProducer.send(message).getSendStatus();
      if (sendStatus != SendStatus.SEND_OK) {
        throw new BetSettlementPublishingException(
            format(
                "Settlement of bet with id %s was sent with %s",
                betSettlement.betId(), sendStatus));
      }
      log.info("Bet settlement sent: {}", betSettlement);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw publishingException(betSettlement, e);
    } catch (MQClientException | RemotingException | MQBrokerException e) {
      throw publishingException(betSettlement, e);
    }
  }

  private BetSettlementPublishingException publishingException(
      BetSettlementMessage betSettlement, Exception cause) {
    return new BetSettlementPublishingException(
        format("Settlement of bet with id %s was not sent", betSettlement.betId()), cause);
  }
}
