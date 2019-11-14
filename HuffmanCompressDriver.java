import java.io.IOException;

/**
 * Driver class for Huffman compression code
 *
 * @author Mien Nguyen, Dartmouth CS 10, driver code as submission for PS-3
 */

public class HuffmanCompressDriver {
    public static void main(String[] args) {
        // Hard code all the file paths we need
        String inputFilePath = "inputs/WarAndPeace.txt";
        String compressedFilePath = inputFilePath.substring(0, inputFilePath.length()-4) + "_compressed.txt";
        String decompressedFilePath = inputFilePath.substring(0, inputFilePath.length()-4) + "_decompressed.txt";

        // Create file compressor object then compress & decompress files
        HuffmanCompress fileCompressor = new HuffmanCompress();
        fileCompressor.compressFile(inputFilePath, compressedFilePath);
        fileCompressor.decompressFile(compressedFilePath, decompressedFilePath);
    }
}
