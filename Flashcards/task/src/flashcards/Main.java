package flashcards;

import javax.print.DocFlavor;
import java.util.*;

class Card{

    public final String term;
    public final String definition;

    public Card(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(term, card.term) &&
                Objects.equals(definition, card.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, definition);
    }
}

class Cards{

    private ArrayList<Card> cards=null;
    private Map<String,Integer> indexTerm =null;
    private Map<String,Integer> indexDefinition=null;

    public int getNextNumber(){
        return cards.size()+1;
    }

    public void addCard(Card card){
        if (card==null) return;
        cards.add(card);
        int index=cards.size()-1;
        indexTerm.put(card.term,index);
        indexDefinition.put(card.definition,index);
    }

    public boolean isContainsTerm(String term){
        return indexTerm.containsKey(term);
    }
    public boolean isContainsDefinition(String definition){
        return indexDefinition.containsKey(definition);
    }

    public Card getCardByTerm(String term) {
        return isContainsTerm(term)?getCard(indexTerm.get(term)):null;
    }
    public Card getCardByDefinition(String definition){
        return  isContainsDefinition(definition)? getCard(indexDefinition.get(definition)):null;
    }
    public Card getCard(int index){
        return cards.get(index);
    }

    public Cards() {
        this.cards=new ArrayList<>();
        this.indexTerm =new HashMap<>();
        this.indexDefinition=new HashMap<>();
    }
}

class CardsHelper{
    private final static  String INPUT_TERM="The card #%d:\n";
    private final static  String INPUT_DEFINITION="The definition of the card #%d:\n";
    private final static  String UN_UNIQUE_TERM="The card \"%s\" already exists. Try again:\n";
    private final static  String UN_UNIQUE_DEFINITION="The definition \"%s\" already exists. Try again:\n";
    private final static  String QUESTION="Print the definition of \"%s\":\n";
    private final static  String ANSWER_CORRECT="Correct answer.\n";
    private final static  String ANSWER_WRONG="Wrong answer. The correct one is \"%s\".\n";
    private final static  String ANSWER_WRONG2="Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".\n";
    private final static  String INPUT_NUMBER_OF_CARDS="Input the number of cards:\n";


    public Cards cards =null;
    public Scanner scanner=null;

    public String inputTerm(int number){
        System.out.printf(INPUT_TERM,number);
        while (true){
            String term=scanner.nextLine();
            if (!cards.isContainsTerm(term)) return term;
            System.out.printf(UN_UNIQUE_TERM,term);
        }
    }
    public String inputDefinition(int number){
        System.out.printf(INPUT_DEFINITION,number);
        while (true){
            String definition=scanner.nextLine();
            if (!cards.isContainsDefinition(definition)) return definition;
            System.out.printf(UN_UNIQUE_DEFINITION,definition);
        }
    }
    public int inputNumberOfCards(){
        System.out.printf(INPUT_NUMBER_OF_CARDS);
        String str=scanner.nextLine();
        return Integer.parseInt(str);
    }
    public void  inputCard(){
        int number=cards.getNextNumber();
        String term=inputTerm(number);
        String definition=inputDefinition(number);
        cards.addCard(new Card(term,definition));
    }
    public void askQuestion(int number){
        Card card=cards.getCard(number);
        System.out.printf(QUESTION,card.term);
        String definition=scanner.nextLine();
        Card askCard=cards.getCardByDefinition(definition);
        if (askCard==null) {
            System.out.printf(ANSWER_WRONG,card.definition);
        }
        else if (Objects.equals(card,askCard)){
            System.out.printf(ANSWER_CORRECT);
        }
        else {
            System.out.printf(ANSWER_WRONG2,card.definition,askCard.term);
        }
    }
    CardsHelper(Cards cards,Scanner scanner){
        this.cards=cards;
        this.scanner=scanner;
    }
}

public class Main {
    public static void main(String[] args) {
        CardsHelper helper=new CardsHelper(new Cards(),new Scanner(System.in));
        int number=helper.inputNumberOfCards();
        for (int i=0;i<number;i++){
            helper.inputCard();
        }
        for (int i=0;i<number;i++){
            helper.askQuestion(i);
        }
    }
}
