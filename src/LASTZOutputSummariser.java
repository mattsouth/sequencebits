import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bespoke code for Andrew South.
 * 
 * Reads a tab delimited file of values and outputs the frequency matrix for the incidence of values across two columns.
 * 
 * @author matt
 */
public class LASTZOutputSummariser {

	private String filepath = null;
	private Matrix matrix = new Matrix();
	private int lines=0;
	private int processed=0;
	// change these to change the columns of interest - note that java arrays are zero indexed
	private int col1Index=0; 
	private int col2Index=2;

	/**
	 * This method is run automatically by Java if it exists.
	 * @param args see printHelp()
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		LASTZOutputSummariser summariser = new LASTZOutputSummariser();
		if (args.length>0) {
			if (args[0].equals("--help")) {
				printHelp();
			} else {
				summariser.setFilepath(args[args.length-1]);
				summariser.summarise();
				if (args[0].equals("--overview")) {
					summariser.printOverview();
				} else {
					summariser.printMatrix();
				}
			}
		} else {
			printHelp();
		}
	}

	private static void printHelp() {
		System.out.println("LASTZ Output summariser");
		System.out.println("Requires java v6 or later (running \"java -version\" tells you the version).");
		System.out.println("Usage: java LASTZOutputSummariser [--overview] filename");		
	}
	
	/**
	 * Process the file
	 * @throws FileNotFoundException if the filepath provided in setFilepath() doesn't properly point to a file
	 * @throws IOException if something goes wrong while reading the file
	 */
	public void summarise() throws FileNotFoundException, IOException {
		File file = new File(filepath);
		FileReader reader = new FileReader(file);
		BufferedReader buff = new BufferedReader(reader);
		String line;
		while((line=buff.readLine())!=null) {
			if (line.length()>0) {
				lines++;
				String[] cols = line.split("\t");
				if (cols.length>3) {
					matrix.incrementCell(cols[col1Index], cols[col2Index]);
					processed++;
				}
			}
		}
		buff.close();
		reader.close();
		// sense checking
		if (lines!=processed) {
			throw new RuntimeException("Error: Are you sure this is the right file? found " + lines + " lines but could only process " + processed);
		}
	}

	/**
	 * Outputs frequency matrix. Run this after processing input file with summarise()!
	 */
	public void printMatrix() {
		String[] col2Vals = matrix.getCol2Vals().toArray(new String[] {});
		int total=0;
		// write list of col 2 values (headings)
		for (String val : col2Vals) {
			System.out.print("\t" + val);
		}
		System.out.println();
		// write cell values
		for (String col1 : matrix.getCol1Vals()) {
			System.out.print(col1);
			for (String col2 : col2Vals ) {
				int count = matrix.getCellCount(col1, col2);
				System.out.print("\t"+count);
				total = total + count;
			}
			System.out.println();
		}
		// sense checking
		if (lines!=total) {
			throw new RuntimeException("Warning: Possible problem found! - found " + lines + " lines but a total count of " + total);
		}
	}

	/**
	 * Swap this for printMatrix in main() to get an overview after processing the input file.
	 */
	public void printOverview() {
		System.out.println(matrix.getCol1Vals().size() + " distinct column " + (col1Index+1) + " vals");
		System.out.println(matrix.getCol2Vals().size() + " distinct column " + (col2Index+1) + " vals");
		System.out.println(processed + " lines processed");
	}

	public void setFilepath(String filepath) {
		this.filepath=filepath;
	}

	public String getFilepath() {
		return filepath;
	}

	/**
	 * A useful class for counting frequency of col1 / col2 combination incidence and returning 0 if there are none counted.
	 */	
	class Matrix {
		Set<String> col2Vals = new HashSet<String>();
		Map<String, Map<String, Integer>> cells = new HashMap<String, Map<String, Integer>>();

		void incrementCell(String col1, String col2) {
			Map<String, Integer> value;
			if (cells.containsKey(col1)) {
				value = cells.get(col1);
				if (value.containsKey(col2)) {
					Integer count = value.get(col2);
					value.put(col2, count+1);
				} else {
					value.put(col2, 1);
				}
			} else {
				value = new HashMap<String, Integer>();
				value.put(col2, 1);
				cells.put(col1, value);
			}
			col2Vals.add(col2); // Don't need to check for existence first as add() does this already
		}

		Integer getCellCount(String col1, String col2) {
			if (cells.containsKey(col1) && cells.get(col1).containsKey(col2)) {
				return cells.get(col1).get(col2);
			} else {
				return 0;
			}
		}

		Set<String> getCol1Vals() {
			return cells.keySet();
		}

		Set<String> getCol2Vals() {
			return col2Vals;
		}
	}
}
