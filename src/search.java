// Author Harmanjot Singh
// 02/20/2017

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class search {

	ArrayList<String> all_words = new ArrayList<String>();
	ArrayList<String> topwords = new ArrayList<String>();

	public static void main(String args[]) throws IOException {

		search w = new search();
		String stopWordFile = "stopwords.txt";

		search ws = new search();
		ws.make_stopwords(stopWordFile);
		ws.readFile(2);
		ws.relavantDocuments();
		ws.getQueries();
		ws.top10();

	}

	HashMap<String, Integer> word_list = new HashMap<String, Integer>();
	HashMap<String, Double> document_len = new HashMap<String, Double>();
	HashMap<String, Integer> max_frequency = new HashMap<String, Integer>();
	HashMap<Integer, Set<Integer>> rel_map = new HashMap<Integer, Set<Integer>>();

	int t_words = 0;
	Integer t_Docs = 0;
	Set<String> stopWords = new HashSet<String>();
	Set<helperClass> queries = new HashSet<helperClass>();

	HashMap<String, helperClass> invIndex = new HashMap<String, helperClass>();

	public void reset() {
		all_words.clear();
		topwords.clear();
		word_list.clear();
		stopWords.clear();
		t_words = 0;
	}

	public void make_stopwords(String fname) {
		try {
			Scanner input = new Scanner(new File(fname));
			while (input.hasNext()) {
				stopWords.add((String) input.next());
			}
		} catch (Exception e) {
			System.out.println(" " + e);
		}

	}

	public void tokenizer(String str) {
		String delim = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";
		int i = 0;
		String str1;
		StringTokenizer tok = new StringTokenizer(str, delim);
		int count = tok.countTokens();
		t_words = t_words + count;
		for (int j = 0; j < count; j++) {
			str1 = tok.nextToken().toLowerCase();
			all_words.add(str1);
			if (word_list.containsKey(str1)) {
				int freq = word_list.get(str1);
				word_list.remove(str1);
				word_list.put(str1, freq + 1);

			} else {
				word_list.put(str1, 1);
			}

		}

	}

	public HashMap<String, Integer> pTokenizer(String str, String fname, HashMap<String, Integer> fileMap,
			String textType) {

		String delim = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";
		int i = 0;
		String str1;

		StringTokenizer tok = new StringTokenizer(str, delim);
		int count = tok.countTokens();
		t_words = t_words + count;
		int validWords = 0;
		Porter stemmer = new Porter();

		for (int j = 0; j < count; j++) {
			str1 = tok.nextToken().toLowerCase();
			if (stopWords.contains(str1) == false) {
				validWords++;
				String stem = stemmer.stripAffixes(str1);
				
				all_words.add(stem);
				Integer weight = 0;
				if (textType.equals("title"))
					weight = 4;
				else
					weight = 1;

				if (fileMap.containsKey(stem)) {

					int freq = fileMap.get(stem);
					fileMap.remove(stem);

					fileMap.put(stem, freq + weight);

				} else {
					fileMap.put(stem, weight);
				}
				
			}
		}
		t_words = t_words + validWords;
		return fileMap;

	}

	public void readFile(int mode) throws IOException {

		String str1;

		File f = new File("./cranfield");
		try {

			File[] fList = f.listFiles();
			for (File file : fList) {
				t_Docs++;
				HashMap<String, Integer> fileMap = new HashMap<String, Integer>();

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				while (true) {
					String line = br.readLine();
					if (line == null) {
						break;
					}
					if (line.equals("<TITLE>")) {
						while (true) {
							line = br.readLine();
							if (line.equals("</TITLE>")) {

								break;
							}
							if (mode == 1) {
								tokenizer(line);
							} else {
								fileMap = pTokenizer(line, file.getName(), fileMap, "title");
							}

						}
					}
					if (line.equals("<TEXT>")) {
						while (true) {
							line = br.readLine();
							if (line.equals("</TEXT>")) {
								break;
							}
							if (mode == 1) {
								tokenizer(line);
							} else {
								fileMap = pTokenizer(line, file.getName(), fileMap, "text");
							}
						}
					}
				}

				Iterator it = fileMap.entrySet().iterator();
				Integer maxFrequency = 0;
				Integer len = 0;
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					String word = (String) pair.getKey();
					Integer freq = (Integer) pair.getValue();
					len = len + freq * freq;
					if (maxFrequency < freq)
						maxFrequency = freq;
					if (invIndex.containsKey(word)) {
						helperClass tfVal = invIndex.get(word);
						tfVal.setNumof_doc(tfVal.getNumof_doc() + 1);
						tfVal.getHmap().put(file.getName(), freq);
					} else {
						helperClass tfVal = new helperClass();
						HashMap<String, Integer> hmap = new HashMap<String, Integer>();
						hmap.put(file.getName(), freq);
						tfVal.setNumof_doc(1);
						tfVal.setHmap(hmap);
						invIndex.put(word, tfVal);
					}

				}
				max_frequency.put(file.getName(), maxFrequency);

			}

		} catch (Exception e) {
			System.out.println(" " + e);
		}
	}

	public void getQueries() {
		String qFile = "queries.txt";
		String delim = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";
		Integer lineNum = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(qFile)));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				helperClass query = new helperClass();
				HashMap<String, Integer> hmap = new HashMap<String, Integer>();
				lineNum++;
				query.setNumof_doc(lineNum);
				StringTokenizer tok = new StringTokenizer(line, delim);
				int count = tok.countTokens();
				String str1;
				Porter stemmer = new Porter();

				for (int j = 0; j < count; j++) {
					str1 = tok.nextToken().toLowerCase();
					if (stopWords.contains(str1) == false) {
						String stem = stemmer.stripAffixes(str1);
						if (hmap.containsKey(stem)) {

							int freq = hmap.get(stem);
							hmap.remove(stem);
							hmap.put(stem, freq + 1);

						} else {
							hmap.put(stem, 1);
						}
					}
				}
				query.setHmap(hmap);
				queries.add(query);
			}

		}

		catch (Exception e) {
			System.out.println(" " + e);
		}
	}

	public void top10() {
		Iterator<helperClass> it = queries.iterator();
		getDocLengths();
		while (it.hasNext()) {
			HashMap<String, Double> docScore = new HashMap<String, Double>();
			helperClass query = it.next();
			Integer index = query.getNumof_doc();
			Iterator<Entry<String, Integer>> jt = query.getHmap().entrySet().iterator();
			while (jt.hasNext()) {
				Map.Entry pair = (Map.Entry) jt.next();
				String word = (String) pair.getKey();
				Integer qFreq = (Integer) pair.getValue();
				if (invIndex.containsKey(word) == false) {
					continue;
				}
				helperClass tfVal = invIndex.get(word);
				HashMap<String, Integer> hmap = tfVal.getHmap();
				Integer numDocs = tfVal.getNumof_doc();
				Iterator<Entry<String, Integer>> ht = hmap.entrySet().iterator();
				Double idf = Math.log10(t_Docs.doubleValue() / numDocs);
				idf = idf / Math.log10(2.0);

				while (ht.hasNext()) {
					Map.Entry docWord = (Map.Entry) ht.next();
					String docId = (String) docWord.getKey();
					Integer wordFreq = (Integer) docWord.getValue();
					Double tfDoc = wordFreq.doubleValue() / max_frequency.get(docId);
					Double weight = (qFreq * idf * idf * tfDoc) / Math.sqrt(document_len.get(docId));
					Double docLength = Math.sqrt(document_len.get(docId));
					Integer mFreq = max_frequency.get(docId);

					if (docScore.containsKey(docId)) {

						weight = docScore.get(docId) + weight;
						docScore.remove(docId);
						docScore.put(docId, weight);
					} else
						docScore.put(docId, weight);

				}
			}

			Map<String, Double> sortedMap = sortByValue(docScore);
			Iterator<Entry<String, Double>> st = sortedMap.entrySet().iterator();

			Set<Integer> topN = new HashSet<Integer>();
			Integer prevCount = 0;
			int[] topNum = { 10, 50, 100, 500 };
			for (int t = 0; t < topNum.length; t++) {

				for (int i = 0; i < topNum[t] - prevCount; i++) {
					Map.Entry pair = (Map.Entry) st.next();
					String docId = pair.getKey().toString();
					Double docScore1 = (Double) pair.getValue();

					Scanner in = new Scanner(docId).useDelimiter("[^0-9]+");
					Integer id = in.nextInt();
					topN.add(id);
				}
				computeAccuracy(index, topN);
				prevCount = topNum[t];
			}

		}

	}

	public void getDocLengths() {

		Iterator<Entry<String, helperClass>> it = invIndex.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String word = pair.getKey().toString();
			helperClass tfVal = (helperClass) pair.getValue();

			Double idf = Math.log10(t_Docs.doubleValue() / tfVal.getNumof_doc());

			idf = idf / Math.log10(2.0);

			HashMap<String, Integer> hmap = tfVal.getHmap();
			Iterator<Entry<String, Integer>> ht = hmap.entrySet().iterator();

			while (ht.hasNext()) {
				Map.Entry docWord = (Map.Entry) ht.next();
				String docId = (String) docWord.getKey();
				Integer wordFreq = (Integer) docWord.getValue();
				Double tfDoc = wordFreq.doubleValue() / max_frequency.get(docId);
				Double weight = tfDoc * tfDoc * idf * idf;

				if (document_len.containsKey(docId)) {

					weight = document_len.get(docId) + weight;
					document_len.remove(docId);
					document_len.put(docId, weight);
				} else
					document_len.put(docId, weight);
			}

		}

	}

	void getInvIndx(String word, String fname) {
		if (invIndex.containsKey(word)) {
			helperClass tfVal = invIndex.get(word);
			System.out.println("Num of docs " + tfVal.getNumof_doc());

			HashMap wordFreq = tfVal.getHmap();
			Iterator it = wordFreq.keySet().iterator();
			while (it.hasNext()) {
				System.out.println("File is " + it.next());
			}

			if (wordFreq.containsKey(fname)) {
				System.out.println("Frequency " + wordFreq.get(fname));

			}
		}
	}

	public void relavantDocuments() throws IOException {
		String relavanceFile = "relevance.txt";
		String delim = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relavanceFile)));
		System.out.println(" Ranked list of documents, in descending order of their similarity with the query");
		
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			StringTokenizer tok = new StringTokenizer(line, delim);
			int count = tok.countTokens();
			String str1;
			Integer queryNUm = Integer.parseInt((String) tok.nextElement());
			Integer docNUm = Integer.parseInt((String) tok.nextElement());

			System.out.println("Query Number  " + queryNUm + "  Document Number  " + docNUm);
			if (!rel_map.containsKey(queryNUm)) {
				Set<Integer> st = new HashSet<Integer>();
				st.add(docNUm);

				rel_map.put(queryNUm, st);
			} else {

				rel_map.get(queryNUm).add(docNUm);

			}

		}
	}

	public void computeAccuracy(Integer queryNum, Set<Integer> topN) {
		double precision = 0, recall = 0;
		System.out.println("Avg Precision and Reall of " + queryNum + " with top " + topN.size() + " Documents");
		Set<Integer> realTop = rel_map.get(queryNum);
		Integer totalRelavant = realTop.size();
		Set<Integer> commonSet = new HashSet<Integer>(realTop);
		commonSet.retainAll(topN);
		recall = (double) commonSet.size() / totalRelavant;
		precision = (double) commonSet.size() / topN.size();
		System.out.println("Recall is " + recall);
		System.out.println("Precison is " + precision);

	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
