package main.simulation.analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import main.simulation.config.Config;

public class CsvAccess {

    /**
     * Creates a CSV-File an fills the headers.
     * @param folderPath = "C:/path/to/folder/"; // Specify the folder path where the CSV file will be created
     * @param fileName "example.csv"; // Specify the name of the CSV file
     * @param headers String[] headers = {"Name", "Age", "City"};
     */
    public static void createCSV(String folderPath, String fileName, List<String> headers) {
        fileName += Util.prittyFormatDate(System.currentTimeMillis()) + ".csv";
        if (!Files.exists(Paths.get(folderPath + fileName))) {//checks if file exists
            try {
                FileWriter fileWriter = new FileWriter(folderPath + fileName);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                printWriter.print("time");//Always: Time
                printWriter.print(",");
                // Write column headers
                writeData(headers, printWriter);
                printWriter.println();

                printWriter.close();
                System.out.println("CSV file created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("(CSV file already exists)");
        }
    }

    /**
     *         // Data rows
     *         String[][] data = {
     *                 {"John Doe", "30", "New York"},
     *                 {"Jane Smith", "25", "London"},
     *                 {"Mike Johnson", "35", "Paris"}
     *       };
     * @param folderPath = path to csv
     * @param fileName = file name (name + "_date.csv")
     * @param data = a new row/new rows
     */
    public static void appendRowInCSV(String folderPath, String fileName, List<String> data) {
        fileName += Util.prittyFormatDate(System.currentTimeMillis()) + ".csv";
        try {
            FileWriter fileWriter = new FileWriter(folderPath + fileName, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            writeData(data, printWriter);

            printWriter.println();
            printWriter.close();
            System.out.println("CSV file created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeData(List<String> data, PrintWriter printWriter) {
        // Write data rows
        for (int i = 0; i < data.size(); i++) {
            printWriter.print(data.get(i));
            if (i != data.size() - 1) {
                printWriter.print(",");
            }
        }
    }
}
