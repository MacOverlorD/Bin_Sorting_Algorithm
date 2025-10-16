package th.ac.kmutt.cpe.algorithm.lappawat;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ReadData {
    // ทุกแถวใน CSV คือกล่อง 2 ค่าทศนิยม: w,h (คั่นด้วย , หรือ ;)
    public static List<Item> readItems(Path csvPath) throws IOException {
        List<Item> items = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line; int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("[,;]");
                if (p.length < 2) continue;
                try {
                    double w = Double.parseDouble(p[0].trim().replace(',', '.'));
                    double h = Double.parseDouble(p[1].trim().replace(',', '.'));
                    items.add(new Item(w, h));
                } catch (NumberFormatException e) {
                    System.err.println("Skip line " + lineNo + ": " + line);
                }
            }
        }
        return items;
    }
}
