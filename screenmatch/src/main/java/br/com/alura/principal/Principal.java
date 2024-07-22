
package br.com.alura.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverterDados;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;    

public class Principal {
    //scanner e identificacao do link
    private Scanner leitura = new Scanner(System.in);
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final  String API_KEY = "&apikey=6585022c";
    private final String SEASON = "&season=";
    private ConsumoApi consumo = new ConsumoApi();
    private ConverterDados conversor = new ConverterDados();
    public void exibeMenu(){
        
        //primeira busca pela serie 
        System.out.println("Digite o nome da serie para busca:");
        
        var nomeSerie =  leitura.nextLine();
        var json = consumo.obterDados( ENDERECO+nomeSerie.replace(" ", "+")+API_KEY);
        DadosSerie dados = conversor.obterDados(json,DadosSerie.class);
        System.out.println(dados);
        //https://www.omdbapi.com/?t=supernatural&apikey=6585022c
        
        List<DadosTemporada> temporadas = new ArrayList<>();
        
        //pesquisa pela serie na api e mostra todos os dados sem divisoes alem da temporada
        for(int i = 1; i<=dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO+nomeSerie.replace(" ", "+")+SEASON+ i +API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
            }
       temporadas.forEach(System.out::println);
        //mostra o nome de todos os episodios sem nenhuma divisao 
       temporadas.forEach(t -> t.episodios().forEach(e->System.out.println(e.titulo())));
        //faz um filtro que cria uma tier list dos 10 episodios mais bem avaliados
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());
        System.out.println("\n 10 melhores episodios de "+ nomeSerie);
                
            //apresenta dados dos episodios
//        dadosEpisodios.stream()
//        .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//        .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
//        .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//        .peek(e -> System.out.println("Ordenação " + e))
//        .limit(10)
//        .peek(e -> System.out.println("Limite " + e))
//        .map(e -> e.titulo().toUpperCase())
//        .peek(e -> System.out.println("Mapeamento " + e))
//        .forEach(System.out::println);


        //cria uma lista com todos os episodios de todas as temporadas 
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d-> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());
        
        episodios.forEach(System.out::println);
        
        //busca da lista de episodios pelo titulo e filtra o que achar primeiro 
        System.out.println("titulo episodio desejado: ");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        //se episodio existir ele mostra em que temporada ele esta e os dados,
        //se nao ele fala que nao encontrou
        if(episodioBuscado.isPresent()){
            System.out.println("episodio encontrado na temporada "+episodioBuscado.get());
        }else{
            System.out.println("episodio nao encontrado");
        }
        
        //filtra a partir de que ano voce deseja buscar os episodios, é um filtro que nao leva em consideracao 
        //o filtro anterior
        System.out.println("a partir de que ano voce deseja ver os episodios ");
        var ano = leitura.nextInt();
        
        LocalDate dataBusca = LocalDate.of(ano,1,1);
        
        DateTimeFormatter formatador =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e->e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e-> System.out.println(
                "temporada: "+e.getTemporada()+
                "Episodio: "+e.getTitulo()+
                        "data Lancamento"+e.getDataLancamento().format(formatador)
                ));
        //mostra a avaliacao de cada temporada
        System.out.println("avaliacao de cada temporada: ");
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .collect(Collectors.groupingBy(Episodio::getTemporada, 
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);
        
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e->e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("media: "+est.getAverage());
        System.out.println("melhor episodio: "+ est.getMax());
        System.out.println("pior episodio: "+est.getMin());
        System.out.println("quantidade: "+est.getCount());
    }
}
