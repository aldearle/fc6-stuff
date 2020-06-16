package decaf;

import coreConcepts.Metric;
import dataPoints.floatArray.Euclidean;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GetAngles {

	@SuppressWarnings("boxing")
	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, IOException {

		Map<Integer, float[]> data = DecafUtil.getProfisetDataById();
		List<Pair<Integer, float[]>> queries = DecafUtil.getProfisetQueries();
		List<Integer> dataIds = new ArrayList<>();
		dataIds.addAll(data.keySet());

//		final int queryRank = 42;
		final int queryRank = 500;
		int qId = queries.get(queryRank).getKey();
		System.out.println(qId);
		float[] q = queries.get(queryRank).getValue();
		Map<Integer, Float> nearestNeighbours = DecafUtil.getNearestNeighbours(qId);
		Set<Integer> keyset = data.keySet();
		Iterator<Integer> it = keyset.iterator();
		it.next();
		int randDataId = it.next();
		float[] observerPoint = data.get(randDataId);

		/*
		 * rand is a fixed data point; q is a fixed query; the nns of q sampled give
		 * angle rand-q-sample should be at 90 degrees
		 */
		Metric<float[]> euc = new Euclidean();
		double d1 = euc.distance(observerPoint, q);

		Random random = new Random();
		System.out.println("d1\td2\tnnDist\tangle(d2, nnDist, d1)");
		/*
		 * use 1000 random points, not related to q
		 */
		for (int i = 0; i < 1000; i++) {
			int randId = dataIds.get(random.nextInt(dataIds.size()));
			final float[] y = data.get(randId);
			double d2 = euc.distance(observerPoint, y);
			double nnDist = euc.distance(q, y);
			System.out.println(d1 + "\t" + d2 + "\t" + nnDist + "\t" + getAngle(d2, nnDist, d1));
		}
		/*
		 * use 1000 nearest neigbours to q
		 */
//		for (Integer nnId : nearestNeighbours.keySet()) {
//			float nnDist = nearestNeighbours.get(nnId);
//			double d2 = euc.distance(rand, data.get(nnId));
//			System.out.println(d1 + "\t" + d2 + "\t" + nnDist + "\t" + getAngle(d2, nnDist, d1));
//		}

	}

	static double getAngle(double a, double b, double c) {
		double cosTheta = (b * b + c * c - a * a) / (4 * b * c);
		return Math.acos(cosTheta) * (180 / Math.PI);
	}

}