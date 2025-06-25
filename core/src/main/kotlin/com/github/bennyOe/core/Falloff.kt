package com.github.bennyOe.core

import com.badlogic.gdx.math.Vector3
import ktx.math.vec3

data class Falloff(val constant: Float, val linear: Float, val quadratic: Float) {
    fun toVector3(): Vector3 = vec3(constant, linear, quadratic)

    companion object {
        /**
         * Berechnet kontinuierliche Falloff-Werte basierend auf einer Distanz.
         * @param distance Die Distanz, bei der das Licht effektiv aufhört.
         * @param profile Ein Parameter (0.0 bis 1.0), der die Form des Falloffs steuert.
         * 0.0 = eher linear, 1.0 = stark quadratisch.
         */
        fun fromDistance(distance: Float, profile: Float = 0.5f): Falloff {
            if (distance <= 0f) return Falloff(1f, 0f, 0f)

            val constant = 1.0f
            // Wir wollen, dass bei 'distance' die Helligkeit ca. 1/256 beträgt.
            // 255 = linear * distance + quadratic * distance^2
            // Dies ist eine Gleichung mit zwei Unbekannten. Wir nutzen den 'profile'-Parameter,
            // um das Verhältnis zwischen linear und quadratic zu steuern.
            val quadratic = (255f * profile) / (distance * distance)
            val linear = (255f * (1f - profile)) / distance

            return Falloff(constant, linear, quadratic)
        }
        val DEFAULT = fromDistance(30f) // Standardwert mit einer mittleren Distanz
    }
}
