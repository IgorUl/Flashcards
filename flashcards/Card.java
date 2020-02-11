package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Card {
    LinkedHashMap<String, String> cards = new LinkedHashMap<>();
    LinkedHashMap<String, Integer> error = new LinkedHashMap<>();
    ArrayList<String> hardCard = new ArrayList<>();
    ArrayList<String> logs = new ArrayList<>();
    boolean wasCleared = false;
    Scanner sc = new Scanner(System.in);
    protected boolean run = true;
    boolean exportFlag = false;
    String saveFile;

    public <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void saveLog() {
        output("File name:");
        String filename = userInput(sc.nextLine());

        File log = new File(filename);
        try {
            if (!log.exists()) {
                log.createNewFile();
            }

            FileWriter writer = new FileWriter(log);
            for (String s : logs) {
                writer.write(s);
            }
            output("The log has been saved.\n");
            writer.close();
        } catch (IOException e) {
            output("File not found.\n");
        }
    }

    public void output(String str) {
        logs.add(str + "\n");
        System.out.println(str);
    }

    public String userInput(String str) {
        logs.add(str + "\n");
        return str;
    }


    public void add() {

        String term;
        String def;

        output("The card:");
        if (!cards.containsKey(term = userInput(sc.nextLine()))) {
            output("The definition of the card:");
            if (!cards.containsValue(def = userInput(sc.nextLine()))) {
                cards.put(term, def);
                error.put(def, 0);
                output("The pair (\"" + term + "\":\"" + def + "\") has been added.\n");
            } else {
                output("The definition \"" + def + "\" already exists.\n");
            }
        } else {
            output("The card \"" + term + "\" already exists.\n");
        }
    }

    public void remove() {
        String card;
        output("The card:");
        if (cards.containsKey(card = userInput(sc.nextLine().strip()))) {
            cards.remove(card);
            error.remove(cards.get(card));
            hardCard.remove(card);
            output("The card has been removed\n");
        } else {
            output("Can't remove \"" + card + "\": there is no such card.\n");
        }
    }

    public void importing() {
        output("File name:");
        startImport(userInput(sc.nextLine()));
    }

    public void startImport(String str) {
        String[] fileimport;
        File flashcard = new File(str);
        int count = 0;
        try (Scanner scanner = new Scanner(flashcard)) {
            while (scanner.hasNextLine()) {
                fileimport = userInput(scanner.nextLine()).split(":");
                cards.put(fileimport[0], fileimport[1]);
                error.put(fileimport[1], Integer.valueOf(fileimport[2]));
                count += 1;
            }
            output(count + " cards have been loaded.\n");
        } catch (FileNotFoundException e) {
            output("File not found.\n");
        }
    }
    public void exitExport(String filename) {
        int count = 0;
        File flashcard = new File(filename);
        try {
            if (!flashcard.exists()) {
                flashcard.createNewFile();
            }

            FileWriter writer = new FileWriter(flashcard);
            for (var entry : cards.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + ":" + error.get(entry.getValue()) + "\n");
                count += 1;
            }
            output(count + " cards have been saved.\n");
            writer.close();
        } catch (IOException e) {
            output("File not found.\n");
        }
    }


    public void exporting() {
        output("File name:");
        exitExport(userInput(sc.nextLine()));
    }

    public void ask() {
        List<String> keys = new ArrayList<String>(cards.keySet());
        Random random = new Random(keys.size());
        output("How many times to ask?");
        int numAsk = Integer.parseInt(userInput(sc.nextLine()));
        String term;
        String def;
        String answer;
        for (int i = 0; i < numAsk; i++) {
            term = keys.get(random.nextInt(keys.size()));
            def = cards.get(term);
            output("Print the definition of \"" + term + "\":");
            if (!Objects.equals(def, answer = userInput(sc.nextLine())) && cards.containsValue(answer)) {
                error.put(def, error.get(def) + 1);
                output("Wrong answer. (The correct one is \"" + def + "\", you've just written the definition of \"" + getKey(cards, answer) + "\" card.)\n");
            } else if (answer.equals(def)) {
                output("Correct answer.\n");
            } else {
                error.put(def, error.get(def) + 1);
                output("Wrong answer. The correct one is \"" + def + "\".\n");
            }
        }
    }

    public void printError() {
        int maxMistake = 0;
        StringBuilder str = new StringBuilder();
        for (var s : error.entrySet()) {
            if (s.getValue() > maxMistake) {
                maxMistake = s.getValue();
            }
        }
        if (!wasCleared) {
            for (var s : cards.entrySet()) {
                if (error.get(s.getValue()) == maxMistake && !hardCard.contains(s.getKey())) {
                    hardCard.add(s.getKey());
                }
            }
        }
        if (hardCard.size() == 1) {
            output("The hardest card is \"" + hardCard.get(0) + "\". You have " + maxMistake + " errors answering it.\n");
        } else if (hardCard.size() > 1) {
            for (int i = 0; i < hardCard.size() - 1; i++) {
                str.append("\"").append(hardCard.get(i)).append("\", ");
            }
            str.append("\"").append(hardCard.get(hardCard.size() - 1)).append("\".");
            output("The hardest cards are " + str + " You have " + maxMistake + " errors answering them.\n");
        } else {
            output("There are no cards with errors.\n");
        }
        wasCleared = false;
    }

    public void resetStat() {
        for (var s : error.entrySet()) {
            s.setValue(0);
        }
        hardCard.clear();
        wasCleared = true;
        output("Card statistics has been reset.\n");
    }

    public void exit() {
        output("Bye Bye!");
        if (exportFlag) {
            exitExport(saveFile);
        }
        this.run = false;
    }

    public void initStart(String[] str) {
        for (int i = 0; i < str.length - 1; i += 2) {
            if (("-export").equals(str[i])) {
                exportFlag = true;
                saveFile = str[i + 1];
            } else if (("-import").equals(str[i])) {
                startImport(str[i + 1]);
            }
        }
    }

    public void initCard() {

        while (this.run) {

            output("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String answer = userInput(sc.nextLine().strip());
            switch (answer) {
                case "add":
                    add();
                    break;
                case "remove":
                    remove();
                    break;
                case "exit":
                    exit();
                    break;
                case "ask":
                    ask();
                    break;
                case "import":
                    importing();
                    break;
                case "export":
                    exporting();
                    break;
                case "log":
                    saveLog();
                    break;
                case "hardest card":
                    printError();
                    break;
                case "reset stats":
                    resetStat();
                    break;
            }
        }
    }
}

