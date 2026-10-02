package sn.oas.facturation;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import sn.oas.facturation.features.bonDeCommande.dto.ReceptionBonDeCommandeRequest;
import sn.oas.facturation.features.bonDeCommande.service.BonDeCommandeService;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;

import java.util.List;
import java.util.Map;

@SpringBootTest
class FacturationApplicationTests {

	static {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		System.setProperty("spring.profiles.active", "dev");
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void inspectOrderAndStock() {
		System.out.println("====== INPSECTING DATABASE STATE ======");
		
		// 1. Piece info
		List<Map<String, Object>> pieces = jdbcTemplate.queryForList(
				"SELECT id, reference, qte_reelle, stock_magasin, stock_atelier FROM pieces_detachees");
		for (Map<String, Object> p : pieces) {
			System.out.println("PIECE: id=" + p.get("id") + ", ref=" + p.get("reference") + 
					", qte_reelle=" + p.get("qte_reelle") + ", magasin=" + p.get("stock_magasin") + 
					", atelier=" + p.get("stock_atelier"));
		}

		// 2. Bon de commande 13 info
		List<Map<String, Object>> bcs = jdbcTemplate.queryForList(
				"SELECT id, numero, statut FROM bons_de_commande WHERE id = 13");
		for (Map<String, Object> bc : bcs) {
			System.out.println("BC 13: numero=" + bc.get("numero") + ", statut=" + bc.get("statut"));
		}

		// 3. Lignes de BC 13
		List<Map<String, Object>> lignesBc = jdbcTemplate.queryForList(
				"SELECT id, quantite, prix_unitaire, piece_detachee_id FROM ligne_bon_de_commande WHERE bon_commande_id = 13");
		for (Map<String, Object> l : lignesBc) {
			System.out.println("LIGNE BC: id=" + l.get("id") + ", qte=" + l.get("quantite") + 
					", pu=" + l.get("prix_unitaire") + ", piece_id=" + l.get("piece_detachee_id"));
		}

		// 4. Bon de livraison linked to BC 13
		List<Map<String, Object>> bls = jdbcTemplate.queryForList(
				"SELECT id, numero, montant_total, statut FROM bons_de_reception WHERE bon_de_commande_id = 13");
		for (Map<String, Object> bl : bls) {
			System.out.println("BL: id=" + bl.get("id") + ", numero=" + bl.get("numero") + 
					", total=" + bl.get("montant_total") + ", statut=" + bl.get("statut"));
			
			// Lignes de BL
			List<Map<String, Object>> lignesBl = jdbcTemplate.queryForList(
					"SELECT id, piece_id, quantite, prix FROM lignes_facturation_piece WHERE facturation_id = ?", bl.get("id"));
			for (Map<String, Object> lbl : lignesBl) {
				System.out.println("  LIGNE BL: piece_id=" + lbl.get("piece_id") + ", qte=" + lbl.get("quantite") + 
						", prix=" + lbl.get("prix"));
			}
		}

		// 5. Stock movements for the piece
		if (!pieces.isEmpty()) {
			Long pieceId = ((Number) pieces.getFirst().get("id")).longValue();
			List<Map<String, Object>> mvs = jdbcTemplate.queryForList(
					"SELECT type, quantite, stock_magasin_avant, stock_magasin_apres, stock_atelier_avant, stock_atelier_apres, motif, date_operation " +
					"FROM stock_mouvements WHERE piece_id = ? ORDER BY date_operation DESC LIMIT 5", pieceId);
			for (Map<String, Object> mv : mvs) {
				System.out.println("MVT: type=" + mv.get("type") + ", qte=" + mv.get("quantite") + 
						", magasin_avant=" + mv.get("stock_magasin_avant") + ", magasin_apres=" + mv.get("stock_magasin_apres") + 
						", motif=" + mv.get("motif") + ", date=" + mv.get("date_operation"));
			}
		}
	}

	@Autowired
	private BonDeCommandeService bonDeCommandeService;

	@Autowired
	private PieceDetacheRepository pieceDetacheRepository;

	@Test
	@org.springframework.transaction.annotation.Transactional
	void testReceptionAndUpdateStock() {
		// Insert a mock BonDeCommande and Piece
		jdbcTemplate.execute("INSERT INTO bons_de_commande (id, numero, date_commande, statut) VALUES (13, 'BC-001', CURRENT_DATE, 'ENVOYE') ON CONFLICT DO NOTHING");
		jdbcTemplate.execute("UPDATE bons_de_commande SET statut = 'ENVOYE' WHERE id = 13");
		
		jdbcTemplate.execute("INSERT INTO pieces_detachees (id, type_piece, designation, reference, numero_serie, statut, stock_magasin, stock_atelier, qte_reelle, created_at, deleted) VALUES (1, 'PDP', 'Piece 1', 'REF-001', 'NS-001', 'ACTIF', 0, 30, 30, CURRENT_TIMESTAMP, false) ON CONFLICT DO NOTHING");
		jdbcTemplate.execute("UPDATE pieces_detachees SET stock_magasin = 0, stock_atelier = 30, qte_reelle = 30 WHERE id = 1");
		
		jdbcTemplate.execute("INSERT INTO Ligne_Bon_De_Commande (id, bon_commande_id, piece_detachee_id, quantite, prix_unitaire, quantite_recue) VALUES (16, 13, 1, 210, 100, 0) ON CONFLICT DO NOTHING");
		
		Long bcId = 13L;
		Long pieceId = 1L;
		Long ligneId = 16L;

		// Find an agent in the database to authenticate
		String agentUsername = jdbcTemplate.queryForObject(
				"SELECT username FROM users WHERE type = 'AGENT' LIMIT 1", String.class);

		// Authenticate as agent (receptionnerAvecQuantites requires a logged-in user/agent)
		org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
				new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
						agentUsername, "password", java.util.List.of()
				)
		);

		// 2. Execute reception
		ReceptionBonDeCommandeRequest request = new ReceptionBonDeCommandeRequest();
		ReceptionBonDeCommandeRequest.LigneReception ligne = new ReceptionBonDeCommandeRequest.LigneReception();
		ligne.setLigneId(ligneId); // ligne_bon_de_commande id for piece_id=1
		ligne.setQuantiteRecue(210); // user requested: "j'avais commandé 210 articles... stock devait devenir 240"
		request.setLignes(java.util.List.of(ligne));

		System.out.println("====== EXECUTING TEST RECEPTION ======");
		bonDeCommandeService.receptionnerAvecQuantites(bcId, request);

		// 3. Assert and verify
		PieceDetache piece = pieceDetacheRepository.findById(pieceId).orElseThrow();
		piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
		org.junit.jupiter.api.Assertions.assertTrue(piece instanceof PDP);
		PDP pdp = (PDP) piece;

		System.out.println("VERIFICATION STOCK: qte_reelle=" + pdp.getQteReelle() + ", magasin=" + pdp.getStockMagasin() + ", atelier=" + pdp.getStockAtelier());
		org.junit.jupiter.api.Assertions.assertEquals(240, pdp.getQteReelle());
		org.junit.jupiter.api.Assertions.assertEquals(210, pdp.getStockMagasin());
		org.junit.jupiter.api.Assertions.assertEquals(30, pdp.getStockAtelier());
	}

}

