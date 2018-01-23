package ru.itmo.nds.reference.treap2015;

@SuppressWarnings("RedundantIfStatement")
public class Treap {
    public static class Treaps {
        Treap l, r;
        Treaps() {}

        public String toString() {
            return "l=" + String.valueOf(l) + ", r=" + String.valueOf(r);
        }
    }

    final Double2DIndividual x;
    private final int y;
    final Treap left;
    final Treap right;

    Treap(Double2DIndividual x, int y, Treap left, Treap right) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
    }

    static Treap merge(Treap l, Treap r) {
        if (l == null) return r;
        if (r == null) return l;

        if (l.y > r.y) {
            Treap newR = merge(l.right, r);
            return new Treap(l.x, l.y, l.left, newR);
        } else {
            Treap newL = merge(l, r.left);
            return new Treap(r.x, r.y, newL, r.right);
        }
    }

    Treaps splitX(Double2DIndividual x) {
        Treaps res = new Treaps();
        Treaps t = new Treaps();
        if (this.x.compareX1(x) < 0) {
            if (right == null) {
                res.r = null;
            } else {
                t = right.splitX(x);
                res.r = t.r;
            }
            res.l = new Treap(this.x, y, left, t.l);
        } else {
            if (left == null) {
                res.l = null;
            } else {
                t = left.splitX(x);
                res.l = t.l;
            }
            res.r = new Treap(this.x, y, t.r, right);
        }
        //System.err.println("SPL: " + String.valueOf(this.x) + "_" + String.valueOf(left) + "_" + String.valueOf(right) + " : " + res.toString());
        return res;
    }

    /**
     * same as split X - members of r have greater x1 than members of l
     */
    Treaps splitY(Double2DIndividual x) {
        Treaps res = new Treaps();
        Treaps t = new Treaps();
        if (this.x.compareX2(x) >= 0) {
            if (right == null) {
                res.r = null;
            } else {
                t = right.splitY(x);
                res.r = t.r;
            }
            res.l = new Treap(this.x, y, left, t.l);
        } else {
            if (left == null) {
                res.l = null;
            } else {
                t = left.splitY(x);
                res.l = t.l;
            }
            res.r = new Treap(this.x, y, t.r, right);
        }
        return res;
    }

    boolean dominatesSomebody(Double2DIndividual nInd) {
        final Treaps split = splitX(nInd);
        if (split.l != null && nInd.compareDom(split.l.getMax()) < 0)
            return true;
        if (split.r != null && nInd.compareDom(split.r.getMin()) < 0)
            return true;
        return false;
    }

    boolean dominatedBySomebody(Double2DIndividual nInd) {
        final Treaps split = splitX(nInd);
        //System.err.println("DBS: " + split);
        //System.err.println("_DBS: " + this);

        if (split.l != null && nInd.compareDom(split.l.getMax()) > 0)
            return true;
        if (split.r != null && nInd.compareDom(split.r.getMin()) > 0)
            return true;
        return false;
    }

    Double2DIndividual getMinP() {
        Treap l = this;
        while (l.left != null)
            l = l.left;
        Treap r = this;
        while (r.right != null)
            r = r.right;
        return new Double2DIndividual(l.x.getX1(), r.x.getX2());
    }

    private Double2DIndividual getMin() {
        Treap l = this;
        while (l.left != null)
            l = l.left;
        return l.x;
    }

    public Double2DIndividual getMax() {
        Treap r = this;
        while (r.right != null)
            r = r.right;
        return r.x;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    private void toString(StringBuilder sb) {
        sb.append("[")
                .append(x.toString())
                .append("]; ");
        if (left != null)
            left.toString(sb);
        if (right != null)
            right.toString(sb);
    }
}
