package sn.oas.facturation.messagerie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;
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
    private final GarageRepository garageRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Transactional
    @Override
    public MessageResponse clientSendMessage(Client client, MessageRequest request) {
        User destinataire = null;
        if (request.destinataireId() != null) {
            destinataire = userRepository.findById(request.destinataireId())
                    .orElse(null);
        }

        if (request.garageId() == null) {
            throw new IllegalArgumentException("Veuillez sélectionner le garage à qui envoyer votre message");
        }
        Garage garage = garageRepository.findById(request.garageId())
                .orElseThrow(() -> new RuntimeException("Garage non trouvé"));

        Message message = Message.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MSG, garage))
                .client(client)
                .expediteur(client)
                .destinataire(destinataire)
                .garage(garage)
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

        // La réponse de l'agent reste rattachée au garage de la conversation (celui choisi par le
        // client dans son dernier message), pour que les agents scopés à ce garage la voient.
        List<Message> existing = messageRepository.findByClientIdOrderByDateEnvoiAsc(clientId);
        Garage garage = existing.isEmpty() ? null : existing.get(existing.size() - 1).getGarage();

        Message message = Message.builder()
                .numero(garage != null
                        ? documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MSG, garage)
                        : documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.MSG))
                .client(client)
                .expediteur(agent)
                .destinataire(client)
                .garage(garage)
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
