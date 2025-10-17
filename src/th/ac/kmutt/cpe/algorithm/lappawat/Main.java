package th.ac.kmutt.cpe.algorithm.lappawat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static Path resolveCsvPath(String fileName) {
        return Paths.get("src/th/ac/kmutt/cpe/algorithm/lappawat/Data/" + fileName);
    }

    private static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().replace(',', '.');
            try {
                double v = Double.parseDouble(s);
                if (v > 0) return v;
                System.out.println("inputs more than 0");
            } catch (NumberFormatException e) {
                System.out.println("invalid numbers formats - try agains");
            }
        }
    }

    private static boolean fitsInBinRotatable(Item it, double binW, double binH) {
        final double EPS = 1e-9;
        boolean ori1 = it.w <= binW + EPS && it.h <= binH + EPS;
        boolean ori2 = it.h <= binW + EPS && it.w <= binH + EPS;
        return ori1 || ori2;
    }

    public static void main(String[] args) throws Exception {
        try (Scanner sc = new Scanner(System.in)) {

        System.out.println("=== FFDH Packing (rotation ALWAYS allowed) ===");
        System.out.println("Working dir: " + System.getProperty("user.dir"));

        System.out.println("Choose items source:");
        System.out.println("  1) boxSize1.csv");
        System.out.println("  2) boxSize2.csv");
        System.out.println("  3) boxSize3.csv");
        System.out.print("Enter 1-3: ");
        String choice = sc.nextLine().trim();

        Path csvPath = switch (choice) {
            case "2" -> resolveCsvPath("boxSize2.csv");
            case "3" -> resolveCsvPath("boxSize3.csv");
            default -> resolveCsvPath("boxSize1.csv");
        };
        List<Item> allItems = ReadData.readItems(csvPath);
        System.out.println("Loaded boxes = " + allItems.size());

        System.out.println("\nSet Bin size:");
        double binW = readPositiveDouble(sc, "Bin width  : ");
        double binH = readPositiveDouble(sc, "Bin height : ");

        List<Item> items = new ArrayList<>();
        List<Item> impossible = new ArrayList<>();
        for (Item it : allItems) {
            if (fitsInBinRotatable(it, binW, binH)) items.add(it);
            else impossible.add(it);
        }

        System.out.printf("%nPrefilter: %d kept, %d removed%n", items.size(), impossible.size());
        if (!impossible.isEmpty()) {
            System.out.println("Removed (up to 10):");
            for (int i = 0; i < Math.min(10, impossible.size()); i++) {
                System.out.println("  " + impossible.get(i));
            }
        }

        System.out.println("\nItems to pack (up to 10):");
        for (int i = 0; i < Math.min(10, items.size()); i++) {
            System.out.printf("#%d %s%n", i + 1, items.get(i));
        }
        if (items.size() > 10) System.out.println("...");

        FFDHPacker.clearState();
        FFDHPacker.Result res = FFDHPacker.pack(items, binW, binH);

        System.out.println("\n=== Result ===");
        System.out.printf("Bin size     : %.3f x %.3f%n", binW, binH);
        System.out.printf("Placed       : %d%n", res.placed.size());
        System.out.printf("Unplaced     : %d%n", res.unplaced.size());
        System.out.printf("Used area    : %.2f%n", res.usedArea);
        System.out.printf("Leftover     : %.2f%n", res.leftoverArea);
        System.out.printf("Bin area     : %.2f%n", res.binArea);
        System.out.printf("Utilization  : %.2f%%%n", res.utilization);

        int show = Math.min(res.placed.size(), 15);
        for (int i = 0; i < show; i++) {
            System.out.println(res.placed.get(i));
        }
        if (res.placed.size() > show) System.out.println("...");

        if (!res.unplaced.isEmpty()) {
            System.out.println("\nUnplaced items (up to 10):");
            for (int i = 0; i < Math.min(10, res.unplaced.size()); i++) {
                System.out.println(res.unplaced.get(i));
            }
        }

        System.out.println("\n=== Validation ===");
        System.out.println("Overlap check   : " +
            (Validator.validateNoOverlap(res.placed) ? "PASS " : "FAIL "));
        System.out.println("In-bounds check : " +
            (Validator.validateInBounds(res.placed, binW, binH) ? "PASS " : "FAIL "));
        System.out.println("Area consistency: " +
            (Validator.validateAreaConsistency(res) ? "PASS " : "FAIL "));  
        }
    }
}
