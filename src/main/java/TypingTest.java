import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class TypingTest {

    private static String lastInput = "";
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Duration> durations = new ArrayList<>();
    private static Instant startTime;
    private static Instant endTime;
    private static int correctCount = 0;
    private static int difficultyTime;
    private static boolean testRunning = true;

    public static class InputRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (testRunning) {
                    lastInput = scanner.nextLine();
                    if (!lastInput.isEmpty()) {
                        endTime = Instant.now();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in input thread: " + e.getMessage());
            }
        }
    }

    public static void testWord(String wordToTest) {
        try {
            System.out.println("\nType this word: " + wordToTest);
            lastInput = "";
            startTime = Instant.now();

            // Calculate timeout based on word length and difficulty
            long timeoutMillis = wordToTest.length() * difficultyTime * 100L;
            long start = System.currentTimeMillis();

            // Wait for input or timeout
            while (System.currentTimeMillis() - start < timeoutMillis &&
                    (lastInput.isEmpty() || !lastInput.equals(wordToTest))) {
                Thread.sleep(50);
            }

            Duration duration = Duration.between(startTime, Instant.now());
            durations.add(duration);

            System.out.println("You typed: " + lastInput);
            if (lastInput.equals(wordToTest)) {
                System.out.println("Correct! Time: " + duration.toMillis() + " ms");
                correctCount++;
            } else {
                System.out.println("Incorrect! The word was: " + wordToTest);
            }
        } catch (Exception e) {
            System.out.println("Error during test: " + e.getMessage());
        }
    }

    public static void typingTest(List<String> wordsToTest) throws InterruptedException {
        Thread inputThread = new Thread(new InputRunnable());
        inputThread.start();

        System.out.println("\nTest starting in 2 seconds...");
        Thread.sleep(2000);

        for (String word : wordsToTest) {
            testWord(word);
            Thread.sleep(500); // Pause between words
        }

        testRunning = false;
        inputThread.interrupt();

        // Calculate results
        long totalMillis = durations.stream()
                .mapToLong(Duration::toMillis)
                .sum();

        double totalSeconds = totalMillis / 1000.0;
        double averageTime = totalSeconds / wordsToTest.size();
        int accuracy = (int) Math.round((correctCount * 100.0) / wordsToTest.size());
        double wpm = (correctCount / (totalSeconds / 60.0));

        // Display results
        System.out.println("\n========= Results =========");
        System.out.printf("Total time: %.2f seconds%n", totalSeconds);
        System.out.printf("Average time: %.2f seconds/word%n", averageTime);
        System.out.println("Correct words: " + correctCount + "/" + wordsToTest.size());
        System.out.println("Accuracy: " + accuracy + "%");
        System.out.printf("Words per minute: %.1f%n", wpm);
        System.out.println("=========================");
    }

    public static void main(String[] args) {
        try {
            // Load words from file
            List<String> words = new ArrayList<>();
            File wordsFile = new File("src/main/resources/Words.txt");

            try (Scanner wordScanner = new Scanner(wordsFile)) {
                while (wordScanner.hasNextLine()) {
                    String word = wordScanner.nextLine().trim();
                    if (!word.isEmpty()) {
                        words.add(word);
                    }
                }
            }

            Collections.shuffle(words);

            // Get word count from user
            int wordCount = 0;
            while (wordCount <= 0 || wordCount > 100) {
                try {
                    System.out.print("How many words do you want to type? (1-100): ");
                    wordCount = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number between 1 and 100");
                }
            }

            List<String> selectedWords = words.subList(0, Math.min(wordCount, words.size()));

            // Get difficulty level
            System.out.println("\nSelect difficulty:");
            System.out.println("1. Easy (more time)");
            System.out.println("2. Medium");
            System.out.println("3. Hard (less time)");

            int difficulty = 0;
            while (difficulty < 1 || difficulty > 3) {
                try {
                    System.out.print("Enter choice (1-3): ");
                    difficulty = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter 1, 2, or 3");
                }
            }

            // Set difficulty
            switch (difficulty) {
                case 1 -> difficultyTime = 18;  // Easy
                case 2 -> difficultyTime = 12;  // Medium
                case 3 -> difficultyTime = 6;  // Hard
            }

            // Run the test
            typingTest(selectedWords);

        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find Words.txt file");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("\nPress enter to exit.");
        }
    }
}
