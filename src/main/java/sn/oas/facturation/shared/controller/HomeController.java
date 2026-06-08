
package sn.oas.facturation.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Tag(name = "Accueil", description = "Redirection et statut de l'API")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Rediriger vers Swagger UI")
    public RedirectView redirectToSwagger() {
        return new RedirectView("/swagger-ui/index.html");
    }

    @GetMapping("/status")
    @Operation(summary = "Vérifier le statut de l'API")
    public String status() {
        return "API Facturation is running on port 9090";
    }
}