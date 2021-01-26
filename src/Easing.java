/**
 * https://easings.net を参照
 */
public final class Easing {
    private Easing() {
    }

    public static double easeOutBounce(double x) {
        final double n1 = 7.5625;
        final double d1 = 2.75;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5 / d1) * x + 0.75;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25 / d1) * x + 0.9375;
        } else {
            return n1 * (x -= 2.625 / d1) * x + 0.984375;
        }
    }

    public static double easeOutElastic(double x) {
        final double c4 = (2 * Math.PI) / 3;

        return x == 0
                ? 0
                : x == 1
                ? 1
                : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    }
}
