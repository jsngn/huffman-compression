import org.w3c.dom.Text;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Class to compress and decompress .txt files using Huffman Compression
 *
 * @author Mien Nguyen, Dartmouth CS 10, text compression class as submission for PS-3
 */

public class HuffmanCompress {
    private BufferedReader input; // The input text file
    private BufferedBitWriter bitOutput; // The compressed file we output
    private BufferedBitReader bitInput; // The compressed file we input for decompression
    private BufferedWriter output; // The decompressed file we output

    private HashMap<Character, Integer> freqTable; // Hash map that is the frequency table of every present char
    private PriorityQueue<BinaryTree<CountedCharacter>> treePQ; // Priority queue containing trees of objects for char
    private HashMap<Character, String> charCodeMap; // Hash map for lookup of every present char's bit code
    private int bitCount; // Total number of meaningful bits in decompressed file

    /**
     * Makes a frequency table of all characters in text file
     */
    private void createFreqTable() throws IOException { // Handle exception in compress method
        freqTable = new HashMap<Character, Integer>();
        int charVal; // Store the integer value returned by read() for each character

        while ((charVal = input.read()) != -1) { // While file is not empty
            Character currChar = (char)charVal; // Cast read integer to character

            if (freqTable.containsKey(currChar)) {
                int newFreq = freqTable.get(currChar) + 1;
                freqTable.put(currChar, newFreq); // +1 frequency if seen before
            }
            else {
                freqTable.put(currChar, 1); // Add character if not seen before, initially seen once
            }
        }
    }

    /**
     * Makes trees whose data is a character & its frequency then puts them into a priority queue
     */
    private void createPriorityQueue() {

        // Implement a tree comparator for priority queue
        class TreeComparator implements Comparator<BinaryTree<CountedCharacter>> {
            public int compare(BinaryTree<CountedCharacter> tree1, BinaryTree<CountedCharacter> tree2) {
                // Return 1 if tree1 has higher frequency
                if (tree1.getData().getThisFreq() > tree2.getData().getThisFreq()) {
                    return 1;
                }
                // Return 0 if two trees have same frequency
                if (tree1.getData().getThisFreq() == tree2.getData().getThisFreq()) {
                    return 0;
                }
                // Return -1 if tree2 has higher frequency
                return -1;
            }
        }

        Comparator<BinaryTree<CountedCharacter>> freqCompare = new TreeComparator();
        treePQ = new PriorityQueue<BinaryTree<CountedCharacter>>(freqCompare);
        Set<Character> allChars = freqTable.keySet(); // Get all the keys into Set so we can go through the map

        // Iterate through Set, creating a new tree for each character (getting frequency from map) then add to priority queue
        for (Character c: allChars) {
            BinaryTree<CountedCharacter> newTree = new BinaryTree<CountedCharacter>(new CountedCharacter(c, freqTable.get(c)));
            treePQ.add(newTree);
        }
    }

    /**
     * Puts everything in the priority queue into a binary tree; put that tree in the priority queue
     */
    private void createTree() {
        if (treePQ.size() == 1) { // Edge case: single character (either occurring once or repeated)
            // Take that 1 tree only, make a parent node for it so we don't immediately hit leaf in compress function
            // Parent node should have 1 child only
            BinaryTree<CountedCharacter> t1 = treePQ.remove();
            BinaryTree<CountedCharacter> r = new BinaryTree<CountedCharacter>(new CountedCharacter('~', t1.getData().getThisFreq()), t1, null);
            treePQ.add(r); // Add it to our priority queue
        }
        else {
            while (treePQ.size() > 1) { // Empty tree (edge case: empty file) won't evaluate to true here
                // Remove 2 first trees
                BinaryTree<CountedCharacter> t1 = treePQ.remove();
                BinaryTree<CountedCharacter> t2 = treePQ.remove();
                // Make a parent node for them with sum of 2 trees' frequencies as its frequency
                BinaryTree<CountedCharacter> r = new BinaryTree<CountedCharacter>(new CountedCharacter('~', t1.getData().getThisFreq() + t2.getData().getThisFreq()), t1, t2);
                treePQ.add(r); // Add it to our priority queue
            }
        }
    }

    /**
     * Creates the map with every character and its corresponding code word
     */
    private void createCharCodeMap() {
        charCodeMap = new HashMap<Character, String>();
        if (treePQ.peek() != null) { // Make sure our priority queue has a tree
            createCharCodeMapRecurse(charCodeMap, treePQ.peek(), ""); // Call recursive helper to build bit codes for all chars
        }
    }

    /**
     * Recursive helper function for createCharCodeMap(); recursively traverses through tree and builds bit code for all chars
     * @param charCodeMap the map that we add to
     * @param startNode the current node we look at; at first it's the root
     * @param bitCode the string of code we build up for each character
     */
    private void createCharCodeMapRecurse(HashMap<Character, String> charCodeMap, BinaryTree<CountedCharacter> startNode, String bitCode) {
        // Base case
        if (startNode.isLeaf()) {
            charCodeMap.put(startNode.getData().getThisChar(), bitCode); // Add char and code string to map
            return; // Don't check for children unnecessarily
        }

        // Recursive cases for both children
        if (startNode.hasLeft()) {
            createCharCodeMapRecurse(charCodeMap, startNode.getLeft(), bitCode + "0"); // Add on 0 if go left
        }
        if (startNode.hasRight()) {
            createCharCodeMapRecurse(charCodeMap, startNode.getRight(), bitCode + "1"); // Add on 1 if go right
        }
    }

    /**
     * Compresses the file
     * @param inputFilePath path of the .txt file we want to compress
     * @param compressedFilePath path we want to put the compressed file in
     */
    public void compressFile(String inputFilePath, String compressedFilePath) {
        try {
            input = new BufferedReader(new FileReader(inputFilePath));
            bitOutput = new BufferedBitWriter(compressedFilePath);

            // Call all helper methods
            createFreqTable();
//            System.out.println(freqTable);
            createPriorityQueue();
//            System.out.println(treePQ);
            createTree();
//            System.out.println(treePQ);
            createCharCodeMap();
//            System.out.println(charCodeMap);

            // Instantiate this again because read() has gone through all chars in createFreqTable()
            input = new BufferedReader(new FileReader(inputFilePath));

            bitCount = 0; // Start total meaningful bits at 0
            int charVal; // Store the integer value returned by read() for each character
            while ((charVal = input.read()) != -1) { // While file isn't empty
                Character currChar = (char)charVal; // Cast read integer to character
                String currCode = charCodeMap.get(currChar); // Get the bit code of current character
                bitCount += currCode.length(); // Update bit count to add length of curr char's bit code because it's meaningful

                // Write the whole bit code of current character into compressed file
                for (int i = 0; i < currCode.length(); i++) {
                    if (currCode.charAt(i) == '0') {
                        bitOutput.writeBit(false);
                    } else {
                        bitOutput.writeBit(true);
                    }
                }
            }

            // Fill it up with dummy 0's/false's if necessary because we can only write in bytes to files
            if (bitCount % 8 != 0) {
                for (int i = 0; i < 8 - (bitCount % 8); i++) {
                    bitOutput.writeBit(false);
                }
            }
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            if (input != null) { // Close the input file if we've opened it
                try {
                    input.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
            if (bitOutput != null) { // Close the output compressed file if we've opened it
                try {
                    bitOutput.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Decompresses the file
     * @param compressedFilePath path to the file we want to decompress
     * @param decompressedFilePath path we want to put the decompressed file in
     */
    public void decompressFile(String compressedFilePath, String decompressedFilePath) {
        try {
            bitInput = new BufferedBitReader(compressedFilePath);
            output = new BufferedWriter(new FileWriter(decompressedFilePath));

            int currBitCount = 0; // Start a count of how many bits we've read to check we only read meaningful bits
            BinaryTree<CountedCharacter> currNode = treePQ.peek();


            while (bitInput.hasNext()) { // Make sure we don't try to read bits that aren't there!
                if (currNode != null) { // Make sure we have a tree
                    boolean bit = bitInput.readBit(); // Read that bit
                    currBitCount++; // Increment our count by 1

                    if (currBitCount <= bitCount) { // Check that we aren't counting the dummy values, if any
                        if (!bit) {
                            if (currNode.hasLeft()) { // Safety check that left child exists
                                currNode = currNode.getLeft(); // Go left down our tree if bit is false/0
                            }
                        }
                        else {
                            if (currNode.hasRight()) { // Safety check that right child exists
                                currNode = currNode.getRight(); // Go right down our tree if bit is true/1
                            }
                        }
                        if (currNode.isLeaf()) { // If we've hit a leaf node
                            output.write((int)currNode.getData().getThisChar()); // Write corresponding char to decompressed file
                            currNode = treePQ.peek(); // Reset current node to root so we go down tree again for next bit, if any
                        }
                    }
                    else {
                        break; // No point in reading more dummy values
                    }
                }
            }
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
        finally {
            if (output != null) { // Close the output decompressed file if we've opened it
                try {
                    output.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
            if (bitInput != null) { // Close the input compressed file if we've opened it
                try {
                    bitInput.close();
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
