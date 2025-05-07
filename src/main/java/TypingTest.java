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

            // Give exactly 5 seconds for each word
            long timeoutMillis = 5000; // 5 seconds
            long start = System.currentTimeMillis();

            // Wait for input or timeout
            while (System.currentTimeMillis() - start < timeoutMillis &&
                    (lastInput.isEmpty() || !lastInput.equals(wordToTest))) {
                Thread.sleep(50);
            }

            Duration duration = Duration.between(startTime,
                    lastInput.equals(wordToTest) ? endTime : Instant.now());
            durations.add(duration);

            System.out.println("You typed: " + lastInput);
            if (lastInput.equals(wordToTest)) {
                System.out.println("Correct! Time: " + duration.toMillis() + " ms");
                correctCount++;
                return; // Move to next word immediately
            } else {
                System.out.println("Incorrect! The word was: " + wordToTest);
                // Wait remaining time if incorrect
                long remainingTime = timeoutMillis - (System.currentTimeMillis() - start);
                if (remainingTime > 0) {
                    Thread.sleep(remainingTime);
                }
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