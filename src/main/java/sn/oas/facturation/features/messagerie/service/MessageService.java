package sn.oas.facturation.features.messagerie.service;

import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.features.messagerie.dto.MessageRequest;
import sn.oas.facturation.features.messagerie.dto.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse clientSendMessage(Client client, MessageRequest request);
    MessageResponse agentSendMessage(User agent, Long clientId, MessageRequest request);
    List<MessageResponse> getConversationMessages(Long clientId, User user);
    List<ClientConversationResponse> getActiveConversations();
}
