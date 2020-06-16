package decaf;

import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.floatArray.Euclidean;
import javafx.util.Pair;
import searchStructures.SearchIndex;
import searchStructures.VPTree;

import java.io.*;
import java.util.*;

/**
 ** This is the code from Brno to measure powers with Profiset.
 ** 
 **/
public class GetDecaf {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

//		FileReader mainFile = new FileReader(decafRoot + queryFileName);
//		LineNumberReader lnr = new LineNumberReader(mainFile);
//		writeTextBatches(lnr);
//		writeObjectBatches(lnr);
//		writeQueryData(lnr, qs);
//		lnr.close();

		final int noOfNeigbours = 100;
		Map<Integer, Float> queryToThresholdMap = DecafUtil.getNthNearestNeigbourDists(noOfNeigbours);
//		for (int i : qs.keySet()) {
//			System.out.println(i + "\t" + qs.get(i));
//		}

//		double x = queryToThresholdMap.get(39912);
//		x += 0.0005;
//		x = (float) Math.pow(x, 1.0);
//		System.out.println(x);

		queryAllData(queryToThresholdMap);
//		testProfisetMissingResult();
//		printNNdists();
	}

	@SuppressWarnings("boxing")
	private static void queryAllData(Map<Integer, Float> queryToThresholdMap)
			throws IOException, ClassNotFoundException {

//		final double POWER = 3.4;
		final boolean fourPoint = true;
		final boolean fft = true;

		List<Pair<Integer, float[]>> queries = DecafUtil.getProfisetQueries();
		List<Pair<Integer, float[]>> allData = DecafUtil.getProfisetData();

		int[] numResults = new int[queries.size()];
		for (int power = 10; power <= 34; power += 2) {
			float POWER = (float) power / 10;

			vptQuery(queryToThresholdMap, POWER, queries, allData, numResults);
		}
	}

	@SuppressWarnings({ "boxing" })
	private static void vptQuery(Map<Integer, Float> queryToThresholdMap, final double POWER,
			List<Pair<Integer, float[]>> queries, List<Pair<Integer, float[]>> allData, int[] numResults)
			throws FileNotFoundException {
		String testName = "VPT_random";
		System.out.print("doing " + testName + ": " + POWER);
		PrintWriter pw = new PrintWriter("//" +
				"/Users/al/Desktop/profiset/profiset_" + POWER + ".csv");
		pw.println("Pivots,Exp,Recall,Distance Computations");

		Metric<Pair<Integer, float[]>> euc = new Metric<Pair<Integer, float[]>>() {
			Metric<float[]> euc = new Euclidean();

			@Override
			public double distance(Pair<Integer, float[]> x, Pair<Integer, float[]> y) {
				final double distance = this.euc.distance(x.getValue(), y.getValue());
				return Math.pow(distance, POWER);
			}

			@Override
			public String getMetricName() {
				return this.euc.getMetricName();
			}
		};
		CountedMetric<Pair<Integer, float[]>> cm = new CountedMetric<>(euc);

		SearchIndex<Pair<Integer, float[]>> si = new VPTree<>(allData, cm);
		cm.reset();
		System.out.println("built tree");

		int ptr = 0;
		for (Pair<Integer, float[]> q : queries) {
			int qid = q.getKey();

			double thresh = queryToThresholdMap.get(qid) + 0.0005;
			thresh = Math.pow(thresh, POWER);

			List<Pair<Integer, float[]>> res = si.thresholdSearch(q, thresh);
//			if (POWER == 1.0) {
				System.out.println("done query " + ptr);
//			}

			pw.println("-," + POWER + "," + res.size() + "," + cm.reset());
			pw.flush();
			ptr++;
		}
		pw.close();
	}

	private static void writeTextBatches(LineNumberReader lnr) throws IOException, FileNotFoundException {
		int batch = 0;
		int id = 0;
		PrintWriter pw = null;
		for (String line = lnr.readLine(); line != null; line = lnr.readLine()) {
			if (id % 1000 == 0) {
				if (batch != 0) {
					pw.close();
					System.out.println("done batch " + (batch - 1));
				}
				pw = new PrintWriter(DecafUtil.decafRoot + "data_relu/" + batch + ".txt");
				batch++;
			}
			Pair<Integer, float[]> pair = getPair(line);
			id++;

			pw.print(pair.getKey());
			for (float f : pair.getValue()) {
				pw.print("\t" + f);
			}
			pw.println();
		}
		pw.close(); // only for the last ever printwriter
		System.out.println("done");
	}

	private static void writeObjectBatches(LineNumberReader lnr) throws IOException, FileNotFoundException {
		int batch = 0;
		int id = 0;
		ObjectOutputStream oos = null;
		List<Pair<Integer, float[]>> vals = null;
		for (String line = lnr.readLine(); line != null; line = lnr.readLine()) {
			if (id % 1000 == 0) {
				if (batch != 0) {
					oos.writeObject(vals);
					oos.close();
					System.out.println("done batch " + (batch - 1));
				}
				FileOutputStream fos = new FileOutputStream(DecafUtil.decafRoot + "data_relu_obj/" + batch + ".obj");
				oos = new ObjectOutputStream(fos);
				vals = new ArrayList<>();
				batch++;
			}
			Pair<Integer, float[]> pair = getPair(line);
			id++;
			vals.add(pair);
		}
		oos.writeObject(vals);
		oos.close(); // only for the last ever printwriter
		System.out.println("done");
	}

	private static void writeQueryData(LineNumberReader lnr, Set<Integer> queries)
			throws IOException, FileNotFoundException {

		FileOutputStream fos = new FileOutputStream(DecafUtil.decafRoot + "queryData.obj");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		List<Pair<Integer, float[]>> vals = new ArrayList<>();
		int lineNo = 0;
		for (String line = lnr.readLine(); line != null; line = lnr.readLine()) {
			Pair<Integer, float[]> pair = getPair(line);
			if (queries.contains(pair.getKey())) {
				vals.add(pair);
			}
			if (lineNo++ % 1000 == 0) {
				System.out.println(lineNo);
			}
		}
		oos.writeObject(vals);
		oos.close();
		System.out.println("done");
	}

	@SuppressWarnings("boxing")
	private static Pair<Integer, float[]> getPair(String a) {
		float[] vals = new float[4096];
		String[] b = a.split("decaf\\_float\\\"\\:\\[");
		String[] c = b[1].split("\\\"_id\\\"\\:\\\"");
		String[] d = c[1].split("\\\"");

		Scanner s = new Scanner(b[1]);
		s.useDelimiter(",");
		int dim = 0;
		while (s.hasNextFloat()) {
			vals[dim] = s.nextFloat();
			dim++;
		}
		String last = s.next();
		vals[dim] = Float.parseFloat(last.substring(0, last.length() - 1));

		Pair<Integer, float[]> pair = new Pair<>(Integer.parseInt(d[0]), vals);
		s.close();
		return pair;
	}

	static void printNNdists() throws IOException {
		Map<Integer, Float> qi = DecafUtil.getNthNearestNeigbourDists(2);
		for (float qdist : qi.values()) {
			System.out.println(qdist);
		}
	}

	static void testProfisetMissingResult() throws FileNotFoundException, ClassNotFoundException, IOException {
		int dataId = 1772361;
		int dataFile = 918;
		int queryId = 183919;

		List<Pair<Integer, float[]>> queries = DecafUtil.getProfisetQueries();

		FileInputStream fis = new FileInputStream(DecafUtil.decafRoot + "data_relu_obj/" + dataFile + ".obj");
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")

		List<Pair<Integer, float[]>> data = (List<Pair<Integer, float[]>>) ois.readObject();

		for (Pair<Integer, float[]> q : queries) {
			if (q.getKey() == queryId) {
				for (Pair<Integer, float[]> d : data) {
					if (d.getKey() == dataId) {
						Euclidean euc = new Euclidean();
						System.out.println(euc.distance(q.getValue(), d.getValue()));
					}
				}
			}
		}

	}
}
