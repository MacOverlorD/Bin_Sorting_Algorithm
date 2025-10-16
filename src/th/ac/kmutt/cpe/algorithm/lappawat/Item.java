package th.ac.kmutt.cpe.algorithm.lappawat;

public class Item {
    public final double w;
    public final double h;

    public Item(double w, double h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", w, h);
    }
}
