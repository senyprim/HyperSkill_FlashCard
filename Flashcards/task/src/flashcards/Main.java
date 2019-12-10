package flashcards;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Cards{

    private Map<String,String> cards=null;

    public int size(){return cards.size();}
    public void addCard(String term,String definition){
        if (term==null) return;
        if (isContainsTerm(term)){
            throw new IllegalArgumentException(String.format("The card \"%s\" already exists.", term));
        }
        if (isContainsDefinition(definition)){
            throw new IllegalArgumentException(String.format("The definition \"%s\" already exists.", definition));
        }
        cards.put(term, definition);
    }
    public void removeCard(String term){
        if(term==null) return;
        if(!isContainsTerm(term)){
            throw new IllegalArgumentException(String.format("Can't remove \"%s\": there is no such card.",term));
        }
        cards.remove(term);
    }

    public boolean isContainsTerm(String term){
        return cards.containsKey(term);
    }
    public boolean isContainsDefinition(String definition){
        return cards.containsValue(definition);
    }

    public String getTermByDefinition(String definition) {
        if (definition==null) return null;
        for(Map.Entry<String,String> card : cards.entrySet()){
            if (card.getValue().equals(definition)){
                return card.getKey();
            }
        }
        return null;
    }

    public String getDefinitionByTerm(String term){
        if (term==null) return null;
        return cards.get(term);
    }

    public String getFirstTerm(){
        return String.valueOf(cards.keySet().toArray()[cards.size()-1]);
    }

    public int importCards(String fileName){
        try{
            List<String> lines=Files.readAllLines(Paths.get(fileName));
            int count=lines.size()/2;
            for(int i=0;i<count;i++){
                cards.put(lines.get(i*2), lines.get(i*2+1));
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
            for(Map.Entry<String,String> card :cards.entrySet())
            {
                writer.write(card.getKey()+"\n"+card.getValue()+"\n");
            }
        }
        catch(IOException e){
            throw new IllegalArgumentException("Something wrong save");
        }
        return cards.size();
    }


    public Cards() {
        this.cards=new LinkedHashMap<>(16,0.75f,true);
    }
}

class CardsHelper{

    private final static  String INPUT_COMMAND="Input the action (add, remove, import, export, ask, exit):\n";
    private final static  String INPUT_TERM="The card:\n";
    private final static  String INPUT_DEFINITION="The definition of the card:\n";
    private final static  String INPUT_SUCCESSFULL="The pair (\"%s\":\"%s\") has been added.\n";

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

    public Cards cards =null;
    public Scanner scanner=null;

    public boolean execCommand(String command){
        boolean exit=false;
        switch(command){
            case "add":{inputCard();break;}
            case "remove":{removeCard();break;}
            case "import":{importCard();break;}
            case "export":{exportCard();break;}
            case "ask":{askQuestions();break;}
            case "exit":{exit=true;break;}
            //default:{throw new IllegalArgumentException("Wrong command");}
        }
        return exit;
    }

    public void inputCommand(){
        while(true){
            System.out.printf(INPUT_COMMAND);
            String command=scanner.nextLine();
            if (execCommand(command)) break;
            //System.out.println();
        }
        System.out.println("Bye bye!");
    }

    public String inputTerm(){
        System.out.printf(INPUT_TERM);
        String term=scanner.nextLine();
        if (!cards.isContainsTerm(term)) return term;
        System.out.printf(UN_UNIQUE_TERM,term);
        return null;
    }
    public String inputDefinition(){
        System.out.printf(INPUT_DEFINITION);
        String definition=scanner.nextLine();
        if (!cards.isContainsDefinition(definition)) return definition;
        System.out.printf(UN_UNIQUE_DEFINITION,definition);
        return null;
    }
    public void  inputCard(){
        String term=inputTerm();
        if (term==null) return ;
        String definition=inputDefinition();
        if(definition==null) return;
        cards.addCard(term, definition);
        System.out.printf(INPUT_SUCCESSFULL,term,definition);
    }

    public void removeCard(){
        System.out.printf(INPUT_TERM);
        String term=scanner.nextLine();
        if (!cards.isContainsTerm(term)) {
            System.out.printf(REMOVE_WRONG,term);
            return;
        }
        cards.removeCard(term);
        System.out.printf(REMOVE_SUCCESSFUL);
    }

    public void importCard(){
        System.out.printf(FILE_INPUT);
        String file=scanner.nextLine();
        if (!new File(file).isFile()){
            System.out.printf(FILE_WRONG);
            return;
        }
        int count=cards.importCards(file);
        System.out.printf(FILE_LOAD_SUCCESSFUL,count);
    }

    public void exportCard(){
        System.out.printf(FILE_INPUT);
        String file=scanner.nextLine();
        int count=cards.exportCards(file);
        System.out.printf(FILE_SAVE_SUCCESSFUL,count);
    }

    public void askQuestion(){
        String term=cards.getFirstTerm();
        System.out.printf(QUESTION,term);
        String definition=cards.getDefinitionByTerm(term);

        String answer=scanner.nextLine();
        String otherTerm=cards.getTermByDefinition(answer);
        if (Objects.equals(answer, definition)){
            System.out.printf(ANSWER_CORRECT);
        }
        else if (otherTerm!=null){
            System.out.printf(ANSWER_WRONG2,definition,otherTerm);
        }
        else {
            System.out.printf(ANSWER_WRONG,definition);
        }
    }

    public void askQuestions(){
        System.out.printf(QUESTIONS_COUNT);
        int count=Integer.parseInt(scanner.nextLine());
        for(int i=0;i<count&&cards.size()>0;i++){
            askQuestion();
            //System.out.println();
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
        helper.inputCommand();
    }
}