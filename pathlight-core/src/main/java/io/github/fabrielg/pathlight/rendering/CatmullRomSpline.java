package io.github.fabrielg.pathlight.rendering;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates smooth curves through a series of points using
 * the Catmull-Rom spline algorithm.
 *
 * Catmull-Rom splines have the nice property of passing THROUGH
 * every control point, making them ideal for navigation trails
 * where the path must follow the waypoints exactly.
 *
 * The algorithm computes each segment using 4 points:
 *   P0 (previous), P1 (start), P2 (end), P3 (next)
 * For edge segments, P0 or P3 are extrapolated by duplicating
 * the nearest endpoint.
 */
public class CatmullRomSpline {

	private final double tension;
	private final int samplesPerSegment;

	public CatmullRomSpline(double tension, int samplesPerSegment) {
		this.tension           = tension;
		this.samplesPerSegment = samplesPerSegment;
	}

	/**
	 * Generates a smooth list of interpolated points along the given control points.
	 *
	 * @param points the control points (waypoint positions) the curve must pass through
	 * @return an ordered list of interpolated positions forming the smooth curve
	 */
	public List<Vector> generate(List<Vector> points) {
		List<Vector> result = new ArrayList<>();

		if (points.size() < 2) return points;

		for (int i = 0; i < points.size() - 1; i++) {

			Vector p0 = getPoint(points, i - 1);
			Vector p1 = getPoint(points, i);
			Vector p2 = getPoint(points, i + 1);
			Vector p3 = getPoint(points, i + 2);

			for (int s = 0; s < samplesPerSegment; s++) {
				double t = (double) s / samplesPerSegment;
				result.add(interpolate(p0, p1, p2, p3, t));
			}
		}

		result.add(points.get(points.size() - 1));

		return result;
	}

	// ─────────────────────────────────────────
	//  INTERPOLATION CATMULL-ROM
	// ─────────────────────────────────────────

	/**
	 * Computes a single point on the Catmull-Rom spline at parameter t.
	 *
	 * The formula is:
	 *   q(t) = 0.5 * [ (2*P1)
	 *                + (-P0 + P2) * t
	 *                + (2*P0 - 5*P1 + 4*P2 - P3) * t²
	 *                + (-P0 + 3*P1 - 3*P2 + P3) * t³ ]
	 *
	 * @param p0 control point before the segment start
	 * @param p1 segment start (curve passes through this point at t=0)
	 * @param p2 segment end   (curve passes through this point at t=1)
	 * @param p3 control point after the segment end
	 * @param t  parameter in [0, 1]
	 */
	private Vector interpolate(Vector p0, Vector p1, Vector p2, Vector p3, double t) {
		double t2 = t * t;
		double t3 = t2 * t;

		double x = catmullRom(p0.getX(), p1.getX(), p2.getX(), p3.getX(), t, t2, t3);
		double y = catmullRom(p0.getY(), p1.getY(), p2.getY(), p3.getY(), t, t2, t3);
		double z = catmullRom(p0.getZ(), p1.getZ(), p2.getZ(), p3.getZ(), t, t2, t3);

		return new Vector(x, y, z);
	}

	/**
	 * Applies the Catmull-Rom formula to a single coordinate axis.
	 */
	private double catmullRom(double p0, double p1, double p2, double p3,
							  double t, double t2, double t3) {
		return tension * (
				(2.0 * p1)
						+ (-p0 + p2) * t
						+ (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2
						+ (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
		);
	}

	// ─────────────────────────────────────────
	//  HELPER
	// ─────────────────────────────────────────

	/**
	 * Returns the point at the given index, clamping to the list bounds.
	 * This handles edge segments by duplicating the nearest endpoint.
	 *
	 * Example for a list [A, B, C, D]:
	 *   index -1 → returns A  (duplicate of index 0)
	 *   index  4 → returns D  (duplicate of index 3)
	 */
	private Vector getPoint(List<Vector> points, int index) {
		if (index < 0)                 return points.get(0);
		if (index >= points.size())    return points.get(points.size() - 1);
		return points.get(index);
	}
}