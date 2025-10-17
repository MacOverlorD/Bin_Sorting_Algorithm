package th.ac.kmutt.cpe.algorithm.lappawat;

import java.util.ArrayList;
import java.util.List;

public class FFDHPacker {

    /** ชั้น (shelf) หนึ่งแถวใน bin */
    private static class Shelf {
        double y;        
        double height;   
        double usedW;   
        Shelf(double y) { this.y = y; this.height = 0.0; this.usedW = 0.0; }
    }

    /** ผลลัพธ์การแพ็ก */
    public static class Result {
        public final List<Placement> placed = new ArrayList<>();
        public final List<Item> unplaced = new ArrayList<>();
        public final double usedArea;
        public final double binArea;
        public final double leftoverArea;
        public final double utilization; // %

        public Result(double usedArea, double binArea) {
            this.usedArea = usedArea;
            this.binArea = binArea;
            this.leftoverArea = Math.max(0.0, binArea - usedArea);
            this.utilization = binArea > 0 ? (usedArea / binArea) * 100.0 : 0.0;
        }
    }

    /** clear state (ใช้ก่อนเรียก pack ใหม่) */
    public static void clearState() {
        TL_PLACED.get().clear();
        TL_UNPLACED.get().clear();
    }

    /** pack items ลง bin */
    public static Result pack(List<Item> items, double binW, double binH) {
        if (items == null) items = List.of();

        // เตรียม node สำหรับ sort
        class Node {
            int id;
            Item it;
            double keyHeight;
            Node(int id, Item it) {
                this.id = id;
                this.it = it;
                this.keyHeight = Math.max(it.w, it.h); // sort ตามด้านที่ยาวกว่า
            }
        }

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) nodes.add(new Node(i+1, items.get(i)));
        nodes.sort((a,b) -> Double.compare(b.keyHeight, a.keyHeight)); // มาก → น้อย

        List<Shelf> shelves = new ArrayList<>();
        shelves.add(new Shelf(0.0));
        double totalAreaPlaced = 0.0;

        // วางทีละชิ้น (BFDH: Best-Fit Decreasing Height)
        for (Node nd : nodes) {
            Item r = nd.it;

            final double EPS = 1e-9;
            // Search best existing shelf (min leftover width) across both orientations
            int bestShelfIdx = -1;
            double bestW = 0, bestH = 0;
            boolean bestRot = false;
            double bestLeftover = Double.POSITIVE_INFINITY;
            double bestShelfHeight = Double.POSITIVE_INFINITY;

            double[][] cand = new double[][] {
                {r.w, r.h, 0},
                {r.h, r.w, 1}
            };

            for (double[] c : cand) {
                double w = c[0], h = c[1];
                for (int si = 0; si < shelves.size(); si++) {
                    Shelf s = shelves.get(si);
                    double newShelfHeight = Math.max(s.height, h);
                    boolean fitW = s.usedW + w <= binW + EPS;
                    boolean fitH = s.y + newShelfHeight <= binH + EPS;
                    if (fitW && fitH) {
                        double leftover = binW - (s.usedW + w);
                        // prefer smaller leftover; tie-breaker: smaller resulting shelf height
                        if (leftover + 1e-12 < bestLeftover || (Math.abs(leftover - bestLeftover) < 1e-12 && newShelfHeight + 1e-12 < bestShelfHeight)) {
                            bestLeftover = leftover;
                            bestShelfIdx = si;
                            bestW = w; bestH = h; bestRot = (c[2] == 1);
                            bestShelfHeight = newShelfHeight;
                        }
                    }
                }
            }

            if (bestShelfIdx >= 0) {
                // place on best existing shelf
                Shelf s = shelves.get(bestShelfIdx);
                double x = s.usedW, y = s.y;
                s.usedW += bestW;
                s.height = Math.max(s.height, bestH);
                placementsAdd(nd.id, x, y, bestW, bestH, bestRot);
                totalAreaPlaced += bestW * bestH;
                continue;
            }

            // No existing shelf fits. Try to create a new shelf at the bottom (after last shelf)
            Shelf last = shelves.get(shelves.size()-1);
            double newY = last.y + last.height;

            int bestNewOri = -1;
            double bestNewW = 0, bestNewH = 0;
            double bestNewLeftover = Double.POSITIVE_INFINITY;
            boolean bestNewRot = false;

            for (double[] c : cand) {
                double w = c[0], h = c[1];
                if (w <= binW + EPS && newY + h <= binH + EPS) {
                    double leftover = binW - w;
                    if (leftover + 1e-12 < bestNewLeftover) {
                        bestNewLeftover = leftover;
                        bestNewOri = 1;
                        bestNewW = w; bestNewH = h; bestNewRot = (c[2] == 1);
                    }
                }
            }

            if (bestNewOri >= 0) {
                Shelf s2 = new Shelf(newY);
                s2.usedW = bestNewW;
                s2.height = bestNewH;
                shelves.add(s2);
                placementsAdd(nd.id, 0.0, newY, bestNewW, bestNewH, bestNewRot);
                totalAreaPlaced += bestNewW * bestNewH;
            } else {
                unplacedAdd(r);
            }
        }

        double binArea = binW * binH;
        Result result = new Result(totalAreaPlaced, binArea);
        result.placed.addAll(TL_PLACED.get());
        result.unplaced.addAll(TL_UNPLACED.get());
        return result;
    }

    // ---------- เก็บผลลัพธ์ ----------
    private static final ThreadLocal<List<Placement>> TL_PLACED = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<Item>> TL_UNPLACED = ThreadLocal.withInitial(ArrayList::new);

    private static void placementsAdd(int id, double x, double y, double w, double h, boolean rot) {
        TL_PLACED.get().add(new Placement(id, x, y, w, h, rot));
    }
    private static void unplacedAdd(Item it) {
        TL_UNPLACED.get().add(it);
    }
}
