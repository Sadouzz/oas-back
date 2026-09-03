package sn.oas.facturation.features.messagerie.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.messagerie.data.entity.Message;
import sn.oas.facturation.features.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.features.messagerie.dto.MessageRequest;
import sn.oas.facturation.features.user.data.entity.User;

import java.util.List;

public interface MessageService {
    Message clientSendMessage(Client client, MessageRequest request);
    Message agentSendMessage(User agent, Long clientId, MessageRequest request);
    List<Message> getConversationMessages(Long clientId, User user);
    List<ClientConversationResponse> getActiveConversations();
}
