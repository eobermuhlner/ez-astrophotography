package ch.obermuhlner.astro.javafx.glow

import ch.obermuhlner.astro.gradient.points.AllPointsFinder
import ch.obermuhlner.astro.gradient.points.NearestPointsFinder
import ch.obermuhlner.astro.gradient.points.PointsFinder
import ch.obermuhlner.astro.gradient.points.VoronoiPointsFinder

enum class PointFinderStrategy(val pointsFinder: PointsFinder) {
    All(AllPointsFinder()),
    Nearest3(NearestPointsFinder(3)),
    Nearest5(NearestPointsFinder(5)),
    Voronoi(VoronoiPointsFinder());

}