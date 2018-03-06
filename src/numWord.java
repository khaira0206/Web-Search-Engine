import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class numWord {

	int numwords = 0;

	ArrayList<String> words = new ArrayList<String>();
	ArrayList<String> topwords = new ArrayList<String>();
	HashMap<String, Integer> wlist = new HashMap<String, Integer>();
	Set<String> stopWords = new HashSet<String>();

	public void tokenizer(String str) {
		String delimiter = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";

		String str1;
		StringTokenizer token = new StringTokenizer(str, delimiter);
		int count = token.countTokens();
		numwords = numwords + count;
		for (int i = 0; i < count; i++) {
			str1 = token.nextToken().toLowerCase();
			words.add(str1);
			if (wlist.containsKey(str1)) {
				int freq = wlist.get(str1);
				wlist.remove(str1);
				wlist.put(str1, freq + 1);

			} else {

				wlist.put(str1, 1);
			}

		}

	}

	public void stopWords(String fname) {
		try {
			Scanner input = new Scanner(new File(fname));
			while (input.hasNext()) {
				stopWords.add((String) input.next());
			}
		} catch (Exception e) {
			System.out.println(" " + e);
		}

	}

	public HashMap<String, Integer> pTokenizer(String str, HashMap<String, Integer> fmap) {
		String delimiter = "~`^=_{}?,.&!'[ ]:;-\"( )%&@#$/";

		String str1;
		StringTokenizer token = new StringTokenizer(str, delimiter);
		int count = token.countTokens();
		int vocWords = 0;
		Porter stemmer = new Porter();

		for (int i = 0; i < count; i++) {
			str1 = token.nextToken().toLowerCase();
			if (stopWords.contains(str1) == false) {
				vocWords++;

				String stem = stemmer.stripAffixes(str1);

				words.add(stem);

				if (fmap.containsKey(stem)) {

					int freq = fmap.get(stem);
					fmap.remove(stem);

					fmap.put(stem, freq + 1);

				} else {
					fmap.put(stem, 1);
				}

			}
		}
		return fmap;

	}

	public void readFile(int val) throws IOException {
		String str;

		File f = new File("./citeseer");
		HashMap<String, Integer> fileMap = new HashMap<String, Integer>();
		try {
			File[] files = f.listFiles();
			for (File file : files) {

				BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

				while (true) {
					String line = buffer.readLine();
					if (line == null) {
						break;

					}

					if (val == 1) {
						tokenizer(line);
					} else {
						fileMap = pTokenizer(line, fileMap);
					}

				}
			}
			if (val == 1) {
				int vocSize = wlist.size();
				int c = 0;

				Map<String, Integer> sMap = sortByValue(wlist);

				Iterator j = sMap.entrySet().iterator();
				
				ArrayList<String> stopw = new ArrayList<String>();

				System.out.println("Top 20 words in the ranking");
				while (j.hasNext()) { // String stopWord = input.next();

					Map.Entry pair = (Map.Entry) j.next();
					System.out.println(pair.getKey() + " = " + pair.getValue());
					if (!stopWords.contains(pair.getKey())) {
						stopw.add(pair.getKey().toString());

					}

					c++;
					if (c == 20)
						break;

				}

				System.out.println("Total No of Words:=" + words.size());
				System.out.println("Vocab Size:=" + vocSize);
                System.out.println("Stopwords in top 20:=" + stopw.size());
				
				if (stopw.size() != 0) {

					for (int i = 0; i < stopw.size(); i++) {
						System.out.println("Stopwords are:=" + stopw.get(i));
					}
				}

				int n = 1, total = 0;
				int acc15 = 0;
				acc15 = (int) ((words.size() * 15) / 100);

				Map.Entry pair1 = (Map.Entry) j.next();
				while (j.hasNext()) {

					total += Integer.parseInt(pair1.getValue().toString());

					if (total >= acc15) {
						break;
					}
					n++;
				}
				System.out.println("Words acoounting for 15%=" + n);
				words.clear();
			}

			else {
				int vocSize = fileMap.size();
				int c = 0;

				Map<String, Integer> sMap1 = sortByValue(fileMap);

				Iterator j = sMap1.entrySet().iterator();
				
				ArrayList<String> stopw = new ArrayList<String>();

				System.out.println("Top 20 words in the ranking");
				while (j.hasNext()) {

					Map.Entry pair1 = (Map.Entry) j.next();
					System.out.println(pair1.getKey() + " = " + pair1.getValue());
					if (stopWords.contains(pair1.getKey())) {
						stopw.add(pair1.getKey().toString());

					}

					c++;
					if (c == 20)
						break;

				}

				int n1 = 1, total = 0;
				int acc15 = 0;
				acc15 = (int) ((words.size() * 15) / 100);

				while (j.hasNext()) {

					Map.Entry pair1 = (Map.Entry) j.next();

					total += Integer.parseInt(pair1.getValue().toString());

					if (total >= acc15) {
						break;
					}
					n1++;
				}
				System.out.println("Total No of Words:=" + words.size());
				System.out.println("Vocab Size:=" + vocSize);
				System.out.println("Words acoounting for 15%:=" + n1);
              
				System.out.println("Stopwords in top 20:=" + stopw.size());
				
				if (stopw.size() != 0) {

					for (int i = 0; i < stopw.size(); i++) {
						System.out.println("Stopwords are:=" + stopw.get(i));
					}
				}

			}
		} catch (Exception e) {
			System.out.println("Exception is " + e);
		}

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

	private void top20Words() throws IOException {
		try {

			Scanner input = new Scanner(new File("stopwords.txt"));
			Set<String> topSet = new HashSet<String>(topwords);
			System.out.println("Stopwords: in top 20  \t");
			while (input.hasNext()) {
				String stopWord = input.next();
				if (topSet.contains(stopWord))
					System.out.println("Stopwords: " + stopWord + "\t");
			}
		} catch (Exception e) {
			System.out.println(" " + e);
		}
	}

	public static void main(String args[]) {

		numWord w = new numWord();
		try {

			String file2 = "stopwords.txt";

			w.stopWords(file2);

			System.out.println("==========Before Stemming===========");
			w.readFile(1);

			System.out.println("==========After Stemming============");
			w.readFile(2);

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
