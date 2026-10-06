package correcao.view;

import correcao.model.Carrinho;
import correcao.model.ProdutoCarrinho;
import correcao.service.ClienteApi;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Function<Carrinho, String> FORMATADOR = c -> String.format(Locale.US,
            "Carrinho #%d | Usuário: %d | Itens: %d | Total: US$ %.2f",
            c.getId(), c.getUserId(), c.getTotalQuantity(), c.getTotal());

    public static void main(String[] args) {
        new ClienteApi().buscarCarrinhos().ifPresentOrElse(
                resposta -> gerarRelatorio(resposta.getCarts()),
                () -> System.out.println("Não foi possível gerar o relatório."));
    }

    private static void gerarRelatorio(List<Carrinho> carrinhos) {
        System.out.println("Carrinhos carregados: " + carrinhos.size());

        System.out.println("\n=== 1) Carrinhos com total acima de US$ 1.000 ===");
        carrinhos.stream()
                .filter(c -> c.getTotal() > 1000)
                .map(FORMATADOR)
                .forEach(System.out::println);

        System.out.println("\n=== 2) Produtos com desconto acima de 15% ===");
        carrinhos.stream()
                .flatMap(c -> c.getProducts() == null ? Stream.<ProdutoCarrinho>empty() : c.getProducts().stream())
                .filter(p -> p.getDiscountPercentage() > 15)
                .map(ProdutoCarrinho::getTitle)
                .distinct()
                .sorted()
                .forEach(System.out::println);

        System.out.println("\n=== 3) Carrinhos por economia (maior -> menor) ===");
        carrinhos.stream()
                .sorted(Comparator.comparingDouble(Carrinho::getEconomia).reversed())
                .forEach(c -> System.out.printf(Locale.US,
                        "Carrinho #%d | Economia: US$ %.2f%n", c.getId(), c.getEconomia()));

        System.out.println("\n=== 4) Soma do discountedTotal ===");
        carrinhos.stream()
                .map(Carrinho::getDiscountedTotal)
                .reduce(Double::sum)
                .ifPresent(soma -> System.out.printf(Locale.US, "Total com desconto: US$ %.2f%n", soma));

        System.out.println("\n=== 5) Carrinhos por número de produtos ===");
        Map<Integer, Long> porQtdProdutos = carrinhos.stream()
                .collect(Collectors.groupingBy(Carrinho::getTotalProducts, TreeMap::new, Collectors.counting()));
        porQtdProdutos.forEach((qtd, n) ->
                System.out.println(qtd + " produto(s): " + n + " carrinho(s)"));

        System.out.println("\n=== Desafio 1) Carrinho de maior valor ===");
        carrinhos.stream()
                .max(Comparator.comparingDouble(Carrinho::getTotal))
                .map(FORMATADOR)
                .ifPresentOrElse(System.out::println,
                        () -> System.out.println("Nenhum carrinho encontrado."));
    }
}
