package sn.oas.facturation.features.blog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sn.oas.facturation.features.blog.data.entity.BlogPost;
import sn.oas.facturation.features.blog.repository.BlogPostRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlogDataSeeder implements CommandLineRunner {

    private final BlogPostRepository blogPostRepository;

    @Override
    public void run(String... args) throws Exception {
        if (blogPostRepository.count() == 0) {
            log.info("--- Initialisation des articles de blog par défaut dans la base de données ---");

            List<BlogPost> posts = Arrays.asList(
                BlogPost.builder()
                    .title("Les 6 vérifications à faire avant un long trajet")
                    .metaDescription("Les contrôles simples qui préparent votre véhicule aux longues distances.")
                    .datePublication(LocalDateTime.now().minusDays(2))
                    .description("Avant de prendre la route pour un long trajet, quelques contrôles s'imposent pour éviter les pannes. Vérifiez la pression et l'état des pneus (y compris la roue de secours), les niveaux des liquides (huile moteur, liquide de refroidissement, lave-glace, liquide de frein), le fonctionnement de tous les feux de signalisation, l'état des balais d'essuie-glace, la présence du kit de sécurité obligatoire, et le fonctionnement de la climatisation.")
                    .images("road")
                    .category("Conseils automobiles")
                    .readTime("6 min de lecture")
                    .featured(true)
                    .build(),

                BlogPost.builder()
                    .title("Quand faut-il remplacer ses plaquettes de frein ?")
                    .metaDescription("Les signes à reconnaître pour garder un freinage fiable.")
                    .datePublication(LocalDateTime.now().minusDays(8))
                    .description("Le système de freinage est l'organe de sécurité le plus important de votre véhicule. Les plaquettes de frein doivent être contrôlées régulièrement. Les signes d'usure incluent un sifflement ou un grincement lors du freinage, une baisse du niveau de liquide de frein, des vibrations dans la pédale ou un allongement anormal des distances d'arrêt. N'attendez pas que le témoin s'allume pour agir.")
                    .images("brakes")
                    .category("Sécurité")
                    .readTime("4 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("Bien entretenir sa climatisation automobile")
                    .metaDescription("Confort, qualité de l’air et bons réflexes au quotidien.")
                    .datePublication(LocalDateTime.now().minusDays(15))
                    .description("Une climatisation bien entretenue assure un air sain dans l'habitacle et évite les surconsommations de carburant. Pensez à faire fonctionner votre climatisation au moins 10 minutes chaque mois, hiver comme été, pour lubrifier les joints. Le filtre d'habitacle doit être remplacé chaque année pour stopper les pollens et poussières, et une recharge en gaz est conseillée tous les 2 ans.")
                    .images("air")
                    .category("Entretien")
                    .readTime("5 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("Ce qui change à l’atelier cet été")
                    .metaDescription("Nouveaux équipements et services pour mieux vous accompagner.")
                    .datePublication(LocalDateTime.now().minusDays(22))
                    .description("Pour toujours mieux vous servir, notre atelier OAS fait peau neuve cet été avec de nouveaux équipements de diagnostic électronique de pointe et une station de géométrie 3D pour un réglage de parallélisme parfait. Nos mécaniciens ont également suivi une formation spécifique sur les véhicules hybrides pour élargir nos champs d'intervention.")
                    .images("garage")
                    .category("Actualités")
                    .readTime("3 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("Comprendre les voyants du tableau de bord")
                    .metaDescription("Les alertes à ne jamais ignorer et celles qui demandent simplement une vérification.")
                    .datePublication(LocalDateTime.now().minusDays(30))
                    .description("Les voyants lumineux du tableau de bord sont de trois couleurs : rouge (danger immédiat, arrêtez-vous), orange (anomalie à contrôler rapidement sans urgence absolue), et vert/bleu (fonctionnement d'un équipement). Apprenez à décoder le symbole de pression d'huile, le témoin moteur, le système de freinage ou le voyant de batterie pour agir avec sérénité.")
                    .images("dashboard")
                    .category("Sécurité")
                    .readTime("7 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("Vidange moteur : pourquoi respecter les échéances ?")
                    .metaDescription("Une huile adaptée et changée à temps protège durablement votre moteur.")
                    .datePublication(LocalDateTime.now().minusDays(36))
                    .description("L'huile moteur lubrifie, nettoie et refroidit les pièces internes du moteur. Avec le temps et les kilomètres, elle se charge en impuretés et perd ses propriétés protectrices. Respecter l'intervalle de vidange préconisé par le constructeur évite l'usure prématurée des cylindres et prolonge la durée de vie globale du moteur.")
                    .images("oil")
                    .category("Entretien")
                    .readTime("5 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("Préserver ses pneus pendant la saison des pluies")
                    .metaDescription("Pression, usure et adhérence : les essentiels pour rouler sereinement.")
                    .datePublication(LocalDateTime.now().minusDays(45))
                    .description("Sur route mouillée, le risque d'aquaplaning augmente considérablement. L'état et la pression de vos pneus sont déterminants. Vérifiez que la profondeur des sculptures est bien supérieure à la limite légale de 1,6 mm, ajustez la pression (des pneus sous-gonflés évacuent moins bien l'eau) et réduisez votre vitesse par temps de pluie.")
                    .images("tyres")
                    .category("Conseils automobiles")
                    .readTime("4 min de lecture")
                    .featured(false)
                    .build(),

                BlogPost.builder()
                    .title("OAS élargit ses prestations de diagnostic")
                    .metaDescription("Un accompagnement plus précis pour identifier l’origine d’une panne.")
                    .datePublication(LocalDateTime.now().minusDays(52))
                    .description("Grâce à l'acquisition de nos nouveaux outils de numérisation de calculateurs, nous pouvons désormais identifier avec une précision absolue les pannes intermittentes ou complexes sur l'ensemble des marques de voitures européennes et asiatiques. Prenez rendez-vous pour un check-up complet.")
                    .images("tools")
                    .category("Actualités")
                    .readTime("3 min de lecture")
                    .featured(false)
                    .build()
            );

            blogPostRepository.saveAll(posts);
            log.info("--- Initialisation terminée avec succès : {} articles de blog insérés ---", posts.size());
        }
    }
}
