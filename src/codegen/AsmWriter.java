package codegen;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AsmWriter {

    private final PrintWriter writer;

    public AsmWriter(String path) throws IOException {
        writer = new PrintWriter(new FileWriter(path));
    }

    public void writeLine(String line) {
        writer.println("        " + line);
    }

    public void writeLabel(String label) {
        writer.println(label + ":");
    }

    public void writeComment(String comment) {
        writer.println("; " + comment);
    }

    public void writeRaw(String line) {
        writer.println(line);
    }

    public void writeBlankLine() {
        writer.println();
    }

    public void close() {
        writer.close();
    }
}
