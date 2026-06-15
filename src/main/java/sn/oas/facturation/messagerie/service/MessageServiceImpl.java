package sn.oas.facturation.messagerie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.messagerie.data.entity.Message;
import sn.oas.facturation.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.messagerie.dto.MessageRequest;
import sn.oas.facturation.messagerie.dto.MessageResponse;
import sn.oas.facturation.messagerie.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Transactional
    @Override
    public MessageResponse clientSendMessage(Client client, MessageRequest request) {
        User destinataire = null;
        if (request.destinataireId() != null) {
            destinataire = userRepository.findById(request.destinataireId())
                    .orElse(null);
        }

        Message message = Message.builder()
                .client(client)
                .expediteur(client)
                .destinataire(destinataire)
                .contenu(request.contenu())
                .lu(false)
                .build();

        messageRepository.save(message);
        return MessageResponse.of(message);
    }

    @Transactional
    @Override
    public MessageResponse agentSendMessage(User agent, Long clientId, MessageRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Message message = Message.builder()
                .client(client)
                .expediteur(agent)
                .destinataire(client)
                .contenu(request.contenu())
                .lu(false)
                .build();

        messageRepository.save(message);
        return MessageResponse.of(message);
    }

    @Transactional
    @Override
    public List<MessageResponse> getConversationMessages(Long clientId, User user) {
        List<Message> messages = messageRepository.findByClientIdOrderByDateEnvoiAsc(clientId);

        boolean updated = false;
        for (Message m : messages) {
            // Mark as read if the current user is NOT the sender of the message
            if (!m.getExpediteur().getId().equals(user.getId()) && !m.isLu()) {
                m.setLu(true);
                updated = true;
            }
        }
        if (updated) {
            messageRepository.saveAll(messages);
        }

        return messages.stream()
                .map(MessageResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientConversationResponse> getActiveConversations() {
        List<Client> clients = messageRepository.findActiveClients();
        List<ClientConversationResponse> conversations = new ArrayList<>();

        for (Client client : clients) {
            List<Message> messages = messageRepository.findByClientIdOrderByDateEnvoiAsc(client.getId());
            MessageResponse lastMsg = null;
            if (!messages.isEmpty()) {
                lastMsg = MessageResponse.of(messages.get(messages.size() - 1));
            }

            long unreadCountForAgents = messages.stream()
                    .filter(m -> m.getExpediteur().getId().equals(client.getId()) && !m.isLu())
                    .count();

            conversations.add(new ClientConversationResponse(
                    client.getId(),
                    client.getFirstName() + " " + client.getLastName(),
                    client.getPhone(),
                    lastMsg,
                    unreadCountForAgents
            ));
        }

        return conversations;
    }
}
