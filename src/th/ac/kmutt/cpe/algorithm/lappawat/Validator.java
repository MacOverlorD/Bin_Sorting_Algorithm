package th.ac.kmutt.cpe.algorithm.lappawat;

import java.util.List;

public class Validator {

    /** ตรวจว่าไม่มี overlap กัน */
    public static boolean validateNoOverlap(List<Placement> placed) {
        for (int i = 0; i < placed.size(); i++) {
            Placement a = placed.get(i);
            for (int j = i + 1; j < placed.size(); j++) {
                Placement b = placed.get(j);
                boolean overlap = !(a.x + a.w <= b.x || b.x + b.w <= a.x ||
                                    a.y + a.h <= b.y || b.y + b.h <= a.y);
                if (overlap) return false;
            }
        }
        return true;
    }
    public static boolean validateInBounds(List<Placement> placed, double binW, double binH) {
        final double EPS = 1e-9;
        for (Placement a : placed) {
            if (a.x < -EPS || a.y < -EPS ||
                a.x + a.w > binW + EPS ||
                a.y + a.h > binH + EPS) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateAreaConsistency(FFDHPacker.Result r) {
        double check = r.usedArea + r.leftoverArea;
        return Math.abs(check - r.binArea) < 1e-6;
    }
}
