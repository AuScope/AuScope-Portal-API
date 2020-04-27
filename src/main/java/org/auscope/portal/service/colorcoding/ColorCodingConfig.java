package org.auscope.portal.service.colorcoding;

import java.util.HashMap;
import java.util.Random;

/**
 * Configuration object used in calculating and storing color coding and calculations.
 *
 * @author tey006
 *
 */
public class ColorCodingConfig {

    double max, min;
    int intervals;
    COLOR color;

    final String UPPERBOUND = "upperBound";
    final String LOWERBOUND = "lowerBound";

    public enum COLOR {
        RED(new String[] {"#FFFFFF", "#FFE6E6", "#FFCCCC", "#FFB2B2", "#FF9999", "#FF8080", "#FF6666", "#FF4D4D",
                "#FF3333", "#FF1919", "#FF0000", "#E60000", "#CC0000", "#B20000", "#990000", "#800000", "#660000",
                "#4C0000", "#330000", "#1A0000"}),
                BLUE(new String[] {"#E8F2FF", "#E6F0FF", "#CCE0FF", "#B2D1FF", "#99C2FF", "#80B2FF", "#66A3FF", "#4D94FF",
                        "#3385FF", "#1975FF", "#0066FF", "#005CE6", "#0052CC", "#0047B2", "#003D99", "#003380", "#002966",
                        "#001F4C", "#001433", "#000A1A"}),
                        GREEN(new String[] {"#E8FAE8", "#E6FAE6", "#CCF5CC", "#B2F0B2", "#99EB99", "#80E680", "#66E066", "#4DDB4D",
                                "#33D633", "#19D119", "#00CC00", "#00B800", "#00A300", "#008F00", "#007A00", "#006600", "#005200",
                                "#003D00", "#002900", "#001400"}),
                                PURPLE(new String[] {"#FFFFFF", "#F5F0FF", "#EBE0FF", "#E0D1FF", "#D6C2FF", "#CCB2FF", "#C2A3FF", "#B894FF",
                                        "#AD85FF", "#A375FF", "#9966FF", "#8A5CE6", "#7A52CC", "#6B47B2", "#5C3D99", "#4C3380", "#3D2966",
                                        "#2E1F4C", "#1F1433", "#0F0A1A"});

        private final String[] color;

        COLOR(String[] color) {
            this.color = color;
        }

        String[] getColor() {
            return color;
        }

        public static COLOR getRandomColor() {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }
    }

    public static String getRandomColor(){
        String [] colors = {"#ff0000","#cc9900","#669900","#00cc99","#0066cc","#6600ff","#ac3973","#00cc00","#cc6600"};
        Double random=(Math.random() * colors.length);
        return colors[random.intValue()];
    }

    public ColorCodingConfig(double min, double max, int intervals, COLOR color) {
        this.max = max;
        this.min = min;
        this.intervals = intervals;
        this.color = color;
    }

    public ColorCodingConfig(double min, double max, int intervals) {
        this.max = max;
        this.min = min;
        this.intervals = intervals;
        this.color = COLOR.getRandomColor();
    }

    public int getIntervals() {
        return this.intervals + 2;
    }

    /**
     * Based on the given iteration cycle, return upper and lower bound.Use this in conjunction with getIterationUpperBound and getIterationLowerBound.
     *
     * @param iteration
     * @return
     */
    public HashMap<String, Double> getIteration(int iteration) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        double avg = Math.abs((this.max - this.min) / this.intervals);

        int minIteration = iteration - 1;
        if (minIteration < 0) {
            result.put(LOWERBOUND, null);
        } else {
            result.put(LOWERBOUND, (minIteration * avg) + this.min);
        }

        double upperBound = (iteration * avg) + this.min;

        if (iteration > this.intervals) {
            result.put(UPPERBOUND, null);
        } else {
            result.put(UPPERBOUND, upperBound);
        }

        return result;
    }

    public Double getIterationUpperBound(HashMap<String, Double> iteration) {
        return iteration.get(UPPERBOUND);
    }

    public Double getIterationLowerBound(HashMap<String, Double> iteration) {
        return iteration.get(LOWERBOUND);
    }

    /**
     * Based on the given iteration, retrieve the color accordingly in the scale.
     *
     * @param iteration
     * @return - color in hex
     */
    public String getColor(int iteration) {
        return (this.color.getColor())[iteration];
    }

}
