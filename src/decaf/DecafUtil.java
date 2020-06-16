package decaf;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class DecafUtil {

	public static String decafRoot = "/Users/al/repos/github/fc6_stuff/resources/";

	/**
	 * @param nn
	 * @return the ids and nnth nearest neighbour distance of all 1000 ground truth
	 *         queries
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	public static Map<Integer, Float> getNthNearestNeigbourDists(int nn) throws IOException {
		Map<Integer, Float> queries = new TreeMap<>();
		String filename = DecafUtil.decafRoot + "groundtruth-profineural-1M-q1000.txt";
		FileReader fr = new FileReader(filename);
		LineNumberReader lnr = new LineNumberReader(fr);

		for (int i = 0; i < 1000; i++) {
			String idLine = lnr.readLine();
			String[] idLineSplit = idLine.split("=");

			String nnLine = lnr.readLine();
			String[] spl = nnLine.split(",");
			String nnBit = spl[nn - 1]; // should look like: " 49.658: 0000927805"
			String[] flBit = nnBit.split(":");

			float f = Float.parseFloat(flBit[0]);

			/*
			 * add the query id and the distance to the nnth nearest-neighbour
			 */
			queries.put(Integer.parseInt(idLineSplit[1]), f);
		}

		lnr.close();
		return queries;
	}

	/**
	 * @param queryId
	 * @return all the nearest neighbour ids and distances for a given query
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	public static Map<Integer, Float> getNearestNeighbours(int queryId) throws IOException {
		Map<Integer, Float> queries = new TreeMap<>();
		String filename = DecafUtil.decafRoot + "groundtruth-profineural-1M-q1000.txt";
		FileReader fr = new FileReader(filename);
		LineNumberReader lnr = new LineNumberReader(fr);

		for (int i = 0; i < 1000; i++) {
			String idLine = lnr.readLine();
			String[] idLineSplit = idLine.split("=");
			final int qId = Integer.parseInt(idLineSplit[1]);

			String nnLine = lnr.readLine();

			if (qId == queryId) {
				String[] spl = nnLine.split(",");

				for (String thing : spl) {
					String[] flBit = thing.split(": ");// should look like: " 49.658: 0000927805"

					float f = Float.parseFloat(flBit[0]);
					int id = Integer.parseInt(flBit[1]);

					queries.put(id, f);
				}
			}
		}

		lnr.close();
		return queries;
	}

	public static List<Pair<Integer, float[]>> getProfisetQueries()
			throws IOException, FileNotFoundException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(decafRoot + "queries_relu/queryData.obj");
		ObjectInputStream ois = new ObjectInputStream(fis);

		@SuppressWarnings("unchecked")
		List<Pair<Integer, float[]>> res = (List<Pair<Integer, float[]>>) ois.readObject();
		ois.close();
		return res;
	}

	public static List<Pair<Integer, float[]>> getProfisetData()
			throws FileNotFoundException, IOException, ClassNotFoundException {
		List<Pair<Integer, float[]>> allData = new ArrayList<>();

		for (int batch = 0; batch < 1000; batch++) {
			FileInputStream fis = new FileInputStream(decafRoot + "data_relu_obj/" + batch + ".obj");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			List<Pair<Integer, float[]>> next = (List<Pair<Integer, float[]>>) ois.readObject();
			allData.addAll(next);

			ois.close();
			if (batch % 100 == 0) {
				System.out.print(".");
			}
		}
		return allData;
	}

	public static Map<Integer, float[]> getProfisetDataById()
			throws FileNotFoundException, ClassNotFoundException, IOException {
		List<Pair<Integer, float[]>> dat = getProfisetData();
		Map<Integer, float[]> res = new HashMap<>();
		for (Pair<Integer, float[]> item : dat) {
			res.put(item.getKey(), item.getValue());
		}
		return res;
	}
}
