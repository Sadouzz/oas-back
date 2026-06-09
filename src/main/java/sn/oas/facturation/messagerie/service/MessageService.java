package sn.oas.facturation.messagerie.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.messagerie.dto.MessageRequest;
import sn.oas.facturation.messagerie.dto.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse clientSendMessage(Client client, MessageRequest request);
    MessageResponse agentSendMessage(User agent, Long clientId, MessageRequest request);
    List<MessageResponse> getConversationMessages(Long clientId, User user);
    List<ClientConversationResponse> getActiveConversations();
}
