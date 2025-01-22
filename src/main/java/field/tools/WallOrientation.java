package field.tools;

public record WallOrientation(boolean top, boolean right, boolean bottom, boolean left) {

    /**
     * Gibt einen Vektor der Mauerrichtungen zurück
     *
     * @return Vektor der Mauerrichtungen (top, right, bottom, left)
     * @author DavidKulbe
     */
    public boolean[] getOrientationVector() {
        return new boolean[]{top, right, bottom, left};
    }
}
