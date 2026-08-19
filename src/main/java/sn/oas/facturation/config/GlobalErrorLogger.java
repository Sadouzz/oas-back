package sn.oas.facturation.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;

@ControllerAdvice
public class GlobalErrorLogger {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        try (FileWriter fw = new FileWriter("last-error.log")) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            fw.write(sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(500).body("Erreur: " + ex.getMessage());
    }
}
