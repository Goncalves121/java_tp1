package correcao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import correcao.model.RespostaCarrinhos;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class ClienteApi {

    private static final String URL = "https://dummyjson.com/carts?limit=0";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<RespostaCarrinhos> buscarCarrinhos() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Erro: a API respondeu com status HTTP "
                        + response.statusCode() + " (esperado: 200).");
                return Optional.empty();
            }

            RespostaCarrinhos resposta = mapper.readValue(response.body(), RespostaCarrinhos.class);
            if (resposta.getCarts() == null) {
                System.err.println("Erro: o JSON recebido não contém a lista 'carts'.");
                return Optional.empty();
            }
            return Optional.of(resposta);

        } catch (JsonProcessingException e) {
            System.err.println("Erro: JSON malformado ou fora do formato esperado -> "
                    + e.getOriginalMessage());
        } catch (IOException e) {
            System.err.println("Erro de conexão com a API: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro: a requisição foi interrompida.");
        } catch (IllegalArgumentException e) {
            System.err.println("Erro: URL inválida -> " + e.getMessage());
        }
        return Optional.empty();
    }
}
