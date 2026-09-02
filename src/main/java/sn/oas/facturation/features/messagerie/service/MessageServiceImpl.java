package sn.oas.facturation.features.messagerie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.messagerie.data.entity.Message;
import sn.oas.facturation.features.messagerie.dto.ClientConversationResponse;
import sn.oas.facturation.features.messagerie.dto.MessageRequest;
import sn.oas.facturation.features.messagerie.dto.MessageResponse;
import sn.oas.facturation.features.messagerie.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional
    @Override
    public Message clientSendMessage(Client client, MessageRequest request) {
        User destinataire = null;
        if (request.destinataireId() != null) {
            destinataire = userRepository.findById(request.destinataireId())
                    .orElse(null);
        }

        Message message = Message.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MSG))
                .client(client)
                .expediteur(client)
                .destinataire(destinataire)
                .contenu(request.contenu())
                .lu(false)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    @Override
    public Message agentSendMessage(User agent, Long clientId, MessageRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Client non trouvé avec l'id : " + clientId));

        Message message = Message.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MSG))
                .client(client)
                .expediteur(agent)
                .destinataire(client)
                .contenu(request.contenu())
                .lu(false)
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    @Override
    public List<Message> getConversationMessages(Long clientId, User user) {
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

        return messages;
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
