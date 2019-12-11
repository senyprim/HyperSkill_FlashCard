package flashcards;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Card{
    private final static Pattern pattern=Pattern.compile("Card\\{term='([^']+)',\\sdefinition='([^']+)',\\serrorsCount=(\\d+)\\}");
    public final String term;
    public final String definition;
    int errorsCount=0;
    public Card(String term,String definition,int errorsCount){
        this.term=term;
        this.definition=definition;
        this.errorsCount=errorsCount;
    }
    public Card(String str){
        Matcher matcher=pattern.matcher(str);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Wrong deserialize string");
        }
        matcher.reset();
        matcher.find();
        this.term=matcher.group(1);
        this.definition=matcher.group(2);
        this.errorsCount=Integer.parseInt(matcher.group(3));
    }
    public void incErrors(){
        errorsCount++;
    }

    public int getErrorsCount(){
        return this.errorsCount;
    }
    public void clearErrorsCount(){
        this.errorsCount=0;
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

    @Override
    public String toString() {
        return "Card{" +
                "term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                ", errorsCount=" + errorsCount +
                '}';
    }
}
class Cards{


    private Map<String,Card> cards=null;
    private Map<String,Card> indexDefinition=null;


    public void resetStat(){
        for (Map.Entry<String,Card> entry:cards.entrySet()) {
            Card card=entry.getValue();
            card.clearErrorsCount();
            entry.setValue(card);
        }
    }
    public int size(){return cards.size();}

    public void addCard(Card card){
        if (card==null) return;
        if (isContainsTerm(card.term)){
            removeCard(card.term);
        }
        cards.put(card.term,card);
        indexDefinition.put(card.definition,card);
    }

    public Card getCardByTerm(String term){
        return cards.get(term);
    }
    public Card getCardByDefinition(String definition){
        return indexDefinition.get(definition);
    }

    public void removeCard(String term){
        if(term==null) return;
        Card card=getCardByTerm(term);
        if(card==null){
            throw new IllegalArgumentException(String.format("Can't remove \"%s\": there is no such card.",term));
        }
        indexDefinition.remove(card.definition);
        cards.remove(term);
    }

    public boolean isContainsTerm(String term){
        return cards.containsKey(term);
    }
    public boolean isContainsDefinition(String definition){
        return indexDefinition.containsKey(definition);
    }

    public String getFirstTerm(){
        return cards.entrySet().iterator().next().getKey();
    }
    public int getMaxErrorsCount(){
        int max=0;
        for (Map.Entry<String,Card> entry:cards.entrySet()) {
            if (entry.getValue().getErrorsCount()>max){
                max=entry.getValue().getErrorsCount();
            }
        }
        return max;
    }
    public List<Card> getHardestCards(){
        List<Card> result=new ArrayList<>();
        int max=0;
        for (Map.Entry<String,Card> entry:cards.entrySet()) {
            if (entry.getValue().getErrorsCount()>max){
                max=entry.getValue().getErrorsCount();
                result.clear();
            }
            if(entry.getValue().getErrorsCount()==max){
                result.add(entry.getValue());
            }
        }
        return max==0?new ArrayList<Card>(): result;
    }

    public int importCards(String fileName){
        try{
            List<String> lines=Files.readAllLines(Paths.get(fileName));
            int count=lines.size();
            for(int i=0;i<count;i++){
                addCard(new Card(lines.get(i)));
            }
            return count;
        }
        catch(IOException e){
            throw new IllegalArgumentException(String.format("File not found"));
        }
    }
    public int exportCards(String fileName){
        try(FileWriter writer=new FileWriter(new File(fileName)))
        {
            int size=cards.size();
            for(Map.Entry<String,Card> card: cards.entrySet())
            {
                writer.write(card.getValue().toString());
                if (--size>0) {
                    writer.write('\n');
                }
            }
        }
        catch(IOException e){
            throw new IllegalArgumentException("Something wrong save");
        }
        return cards.size();
    }

    public Cards() {
        this.cards=new LinkedHashMap<>(16,0.75f,true);
        this.indexDefinition=new HashMap<>();
    }
}

class CardsHelper{

    private final static  String INPUT_COMMAND="Input the action (add, remove, import, export, ask, exit):\n";
    private final static  String INPUT_TERM="The card:\n";
    private final static  String INPUT_DEFINITION="The definition of the card:\n";
    private final static  String INPUT_SUCCESSFUL ="The pair (\"%s\":\"%s\") has been added.\n";

    private final static  String REMOVE_WRONG="Can't remove \"%s\": there is no such card.\n";
    private final static  String REMOVE_SUCCESSFUL="The card has been removed.\n";

    private final static String FILE_INPUT="File name:\n";
    private final static String FILE_WRONG="File not found.\n";
    private final static String FILE_LOAD_SUCCESSFUL="%d cards have been loaded.\n";
    private final static String FILE_SAVE_SUCCESSFUL="%d cards have been saved.\n";

    private final static  String UN_UNIQUE_TERM="The card \"%s\" already exists.\n";
    private final static  String UN_UNIQUE_DEFINITION="The definition \"%s\" already exists.\n";

    private final static  String QUESTIONS_COUNT="How many times to ask?:\n";
    private final static  String QUESTION="Print the definition of \"%s\":\n";
    private final static  String ANSWER_CORRECT="Correct answer.\n";
    private final static  String ANSWER_WRONG="Wrong answer. The correct one is \"%s\".\n";
    private final static  String ANSWER_WRONG2="Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".\n";

    private  final static String HARDEST_NONE="There are no cards with errors.\n";
    private  final static String HARDEST_ONE="The hardest card is %s. You have %d errors answering it.\n";
    private  final static String HARDEST_MANY="The hardest cards are %s. You have %d errors answering them.\n";

    private final static  String RESET_STATISTIC="Card statistics has been reset.\n";
    private final static  String SAVE_LOG="The log has been saved.\n";


    public Cards cards =null;
    public Scanner scanner=null;
    private List<String> log=null;

    private void printAndLog(String format,Object ...args){
        System.out.printf(format,args);
        log.add(String.format(format,args));
    }

    public boolean execCommand(String command){
        boolean exit=false;
        switch(command){
            case "add":{inputCard();break;}
            case "remove":{removeCard();break;}
            case "import":{importCard();break;}
            case "export":{exportCard();break;}
            case "ask":{askQuestions();break;}
            case "exit":{exit=true;break;}
            case "log":{saveLog();break;}
            case "hardest card":{getHardestCards();break;}
            case "reset stats":{resetStat();break;}
        }
        return exit;
    }


    public String inputFromConsole(String inputString){
        printAndLog(inputString);
        String result=scanner.nextLine();
        log.add(result+"\n");
        return result;
    }

    public void inputCommand(){
        while(true){
            String command=inputFromConsole(INPUT_COMMAND);
            if (execCommand(command)) break;
        }
        printAndLog("Bye bye!\n");
    }

    public String inputTerm(){
        String term=inputFromConsole(INPUT_TERM);
        if (!cards.isContainsTerm(term)) return term;
        printAndLog(UN_UNIQUE_TERM,term);
        return null;
    }
    public String inputDefinition(){
        String definition=inputFromConsole(INPUT_DEFINITION);
        if (!cards.isContainsDefinition(definition)) return definition;
        printAndLog(UN_UNIQUE_DEFINITION,definition);
        return null;
    }
    public void  inputCard(){
        String term=inputTerm();
        if (term==null) return ;
        String definition=inputDefinition();
        if(definition==null) return;
        cards.addCard(new Card(term,definition,0));
        printAndLog(INPUT_SUCCESSFUL,term,definition);
    }

    public void removeCard(){
        String term=inputFromConsole(INPUT_TERM);
        if (!cards.isContainsTerm(term)) {
            printAndLog(REMOVE_WRONG,term);
            return;
        }
        cards.removeCard(term);
        printAndLog(REMOVE_SUCCESSFUL);
    }

    public void importCard(){
        String file=inputFromConsole(FILE_INPUT);
        if (!new File(file).isFile()){
            printAndLog(FILE_WRONG);
            return;
        }
        int count=cards.importCards(file);
        printAndLog(FILE_LOAD_SUCCESSFUL,count);
    }

    public void exportCard(){
        String file=inputFromConsole(FILE_INPUT);
        int count=cards.exportCards(file);
        printAndLog(FILE_SAVE_SUCCESSFUL,count);
    }

    public void askQuestion(){
        Card card=cards.getCardByTerm(cards.getFirstTerm());
        String answer=inputFromConsole(String.format(QUESTION,card.term));
        Card otherCard=cards.getCardByDefinition(answer);
        if (Objects.equals(card,otherCard)){
            printAndLog(ANSWER_CORRECT);
        }
        else if (otherCard!=null){
            printAndLog(ANSWER_WRONG2,card.definition,otherCard.term);
            card.incErrors();
        }
        else {
            printAndLog(ANSWER_WRONG,card.definition);
            card.incErrors();
        }
    }

    public void askQuestions(){
        int count=Integer.parseInt(inputFromConsole(QUESTIONS_COUNT));
        for(int i=0;i<count&&cards.size()>0;i++){
            askQuestion();
        }
    }

    public void saveLog(){
        String file=inputFromConsole(FILE_INPUT);
        try(FileWriter writer=new FileWriter(new File(file))){
            for (int i=0;i<log.size();i++){
                writer.write(log.get(i));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        printAndLog(SAVE_LOG);
    }

    public void getHardestCards(){
        List<Card> list=cards.getHardestCards();
        if (list.size()==0){
            printAndLog(HARDEST_NONE);
        }
        else {
            String hardest="";
            for(int i=0;i<list.size();i++){
                hardest+=String.format("\"%s\"",list.get(i).term);
                if (i<list.size()-1){
                    hardest+=", ";
                }
            }
            printAndLog(list.size()==1?HARDEST_ONE:HARDEST_MANY,hardest,list.get(0).errorsCount);
        }
    }

    public void resetStat(){
        cards.resetStat();
        printAndLog(RESET_STATISTIC);
    }

    CardsHelper(Cards cards,Scanner scanner,List<String> log){
        this.cards=cards;
        this.scanner=scanner;
        this.log=log;
    }
}

public class Main {
    public static void main(String[] args) {
        CardsHelper helper=new CardsHelper(new Cards(),new Scanner(System.in),new ArrayList<>());
        helper.inputCommand();
    }
}