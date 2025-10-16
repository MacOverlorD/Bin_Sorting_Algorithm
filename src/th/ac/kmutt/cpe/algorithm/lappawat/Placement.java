package th.ac.kmutt.cpe.algorithm.lappawat;

public class Placement {
    public final int index;    // ดัชนีรายการ (เริ่มที่ 1 ตามลำดับหลัง sort ภายใน packer)
    public final double x, y;  // มุมซ้ายล่างใน bin
    public final double w, h;  // ขนาดที่วาง (อาจสลับ w/h ถ้าหมุน)
    public final boolean rotated;

    public Placement(int index, double x, double y, double w, double h, boolean rotated) {
        this.index = index;
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.rotated = rotated;
    }

    @Override
    public String toString() {
        return String.format("#%d @ (%.2f, %.2f) %.2f x %.2f %s",
                index, x, y, w, h, rotated ? "[rot]" : "");
    }
}
