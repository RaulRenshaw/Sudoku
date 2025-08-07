package br.com.dio;

import br.com.dio.Model.Board;
import br.com.dio.Model.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class Main {

    // Scanner para leitura da entrada do usuário via console
    private final static Scanner scanner = new Scanner(System.in);

    // Instância do tabuleiro do jogo
    private static Board board;

    // Tamanho limite do tabuleiro (9x9)
    private final static int BOARD_LIMIT = 9;

    public static void main(String[] args) {
        // Mapeia os argumentos passados no formato "posição;valor" para um Map
        // Exemplo do formato esperado: "0,0;4,false"
        final var positions = Stream.of(args)
                .collect(toMap(
                        k -> k.split(";")[0],  // chave: posição no tabuleiro "x,y"
                        v -> v.split(";")[1]   // valor: configuração "valor,fixo"
                ));

        var option = -1;
        // Loop infinito para apresentar o menu até o usuário sair
        while (true){
            System.out.println("Selecione uma das opções a seguir");
            System.out.println("1 - Iniciar um novo Jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Verificar status do jogo");
            System.out.println("6 - limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            option = scanner.nextInt();

            // Dispara a ação escolhida no menu
            switch (option){
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> System.exit(0);
                default -> System.out.println("Opção inválida, selecione uma das opções do menu");
            }
        }
    }

    // Método que inicia o jogo criando o tabuleiro com base nas posições recebidas
    private static void startGame(final Map<String, String> positions) {
        if (nonNull(board)){
            System.out.println("O jogo já foi iniciado");
            return;
        }

        List<List<Space>> spaces = new ArrayList<>();
        // Loop para construir uma matriz 9x9 de espaços (spaces)
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                // Obtém a configuração da posição "i,j" no mapa
                var positionConfig = positions.get("%s,%s".formatted(i, j));
                // Extrai o valor esperado (número) para a posição
                var expected = Integer.parseInt(positionConfig.split(",")[0]);
                // Extrai se o número é fixo ou não naquela posição
                var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                // Cria um objeto Space representando a casa do tabuleiro
                var currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }

        // Cria o tabuleiro com a lista de espaços montada
        board = new Board(spaces);
        System.out.println("O jogo está pronto para começar");
    }

    // Método para inserir um número na posição desejada
    private static void inputNumber() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        System.out.println("Informe a coluna que em que o número será inserido");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha que em que o número será inserido");
        var row = runUntilGetValidNumber(0, 8);
        System.out.printf("Informe o número que vai entrar na posição [%s,%s]\n", col, row);
        var value = runUntilGetValidNumber(1, 9);
        // Tenta alterar o valor na posição; se posição é fixa, não altera e avisa
        if (!board.changeValue(col, row, value)){
            System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
        }
    }

    // Método para remover um número da posição desejada (limpar)
    private static void removeNumber() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        System.out.println("Informe a coluna que em que o número será inserido");
        var col = runUntilGetValidNumber(0, 8);
        System.out.println("Informe a linha que em que o número será inserido");
        var row = runUntilGetValidNumber(0, 8);
        // Tenta limpar a posição; se for fixa, não permite e avisa
        if (!board.clearValue(col, row)){
            System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
        }
    }

    // Mostra o tabuleiro atual no console formatado
    private static void showCurrentGame() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        var args = new Object[81];
        var argPos = 0;
        // Prepara um array de strings com os valores atuais do tabuleiro para formatação
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (var col: board.getSpaces()){
                args[argPos ++] = " " + ((isNull(col.get(i).getActual())) ? " " : col.get(i).getActual());
            }
        }
        System.out.println("Seu jogo se encontra da seguinte forma");
        // Imprime o tabuleiro formatado com base no template definido
        System.out.printf((BOARD_TEMPLATE) + "\n", args);
    }

    // Exibe o status atual do jogo (se está completo, se tem erros, etc)
    private static void showGameStatus() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        System.out.printf("O jogo atualmente se encontra no status %s\n", board.getStatus().getLabel());
        if(board.hasErrors()){
            System.out.println("O jogo contém erros");
        } else {
            System.out.println("O jogo não contém erros");
        }
    }

    // Método para limpar o jogo após confirmação do usuário
    private static void clearGame() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        System.out.println("Tem certeza que deseja limpar seu jogo e perder todo seu progresso?");
        var confirm = scanner.next();
        // Garante que o usuário digite 'sim' ou 'não'
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")){
            System.out.println("Informe 'sim' ou 'não'");
            confirm = scanner.next();
        }

        // Se confirmado, reseta o tabuleiro
        if(confirm.equalsIgnoreCase("sim")){
            board.reset();
        }
    }

    // Finaliza o jogo verificando se está completo e sem erros
    private static void finishGame() {
        if (isNull(board)){
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }

        if (board.gameIsFinished()){
            System.out.println("Parabéns você concluiu o jogo");
            showCurrentGame();
            board = null; // Reseta o jogo para permitir reiniciar
        } else if (board.hasErrors()) {
            System.out.println("Seu jogo contém erros, verifique seu board e ajuste-o");
        } else {
            System.out.println("Você ainda precisa preencher algum espaço");
        }
    }

    // Função auxiliar para garantir que o usuário digite um número válido entre min e max
    private static int runUntilGetValidNumber(final int min, final int max){
        var current = scanner.nextInt();
        while (current < min || current > max){
            System.out.printf("Informe um número entre %s e %s\n", min, max);
            current = scanner.nextInt();
        }
        return current;
    }

}
