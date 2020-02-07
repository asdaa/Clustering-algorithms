import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * main()-function and repeated clustering utilities 
 * for running clustering on text-based datasets
 * 
 * @author Juho Puumalainen
 */
public class Clustering {

	/**
	 * Program's main function. Parses the command line arguments, runs the clustering, dumps result to file.
	 *
	 */
	public static void main(String[] args)  {
		if (args.length < 2) {
			System.err.println("Not enough arguments");
			printUsage();
			return;
		}

		String inputFilename = args[0];
		if(!(new File(inputFilename).exists())){
			System.err.println("Input file does not exist");
			return;
		}

		int numberOfClusters = -1;
		try {
			numberOfClusters = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
		}
		if(numberOfClusters <= 0){
			System.err.println("Invalid number of clusters: " + args[1]);
			return;
		}

		// additional options
		if (args.length % 2 != 0) {
			System.out.println("Invalid options");
			printUsage();
			return;
		}
		String realCentroidsFilename = null;
		String outputFilename;
		if(inputFilename.contains("."))
			outputFilename = inputFilename.substring(0, inputFilename.lastIndexOf("."));
		else
			outputFilename = inputFilename;
		outputFilename = outputFilename + "-centroids.txt";
		outputFilename = new File(outputFilename).getName();
		String algorithmName = "fkm";
		int repeats = 1;
		for (int i = 2; i < args.length; i += 2) {
			if (args[i].toLowerCase().equals("-c")) {
				// real centroids
				realCentroidsFilename = args[i + 1];
				if(!(new File(realCentroidsFilename).exists())){
					System.err.println("Real centroids file does not exist");
					return;
				}
			} else if (args[i].toLowerCase().equals("-r")) {
				// repeats
				repeats = -1;
				try {
					repeats = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
				}
				if(repeats <= 0){
					System.err.println("Invalid number of repeats: " + args[i + 1]);
					return;
				}
			} else if (args[i].toLowerCase().equals("-o")) {
				// output filename
				outputFilename = args[i + 1];
				try {
					new FileWriter(new File(outputFilename)).close();
				} catch(IOException e){
					System.err.println("Unable to access output file: " + args[i + 1]);
					return;
				}
			} else if (args[i].toLowerCase().equals("-a")) {
				// algorithm name
				algorithmName = args[i + 1].toLowerCase();
			} else {
				System.err.println("Unknown option: " + args[i]);
				printUsage();
				return;
			}
		}

		Clustering clustering;
		if(algorithmName.equals("km")) {
			System.out.println("Normal k-means algorithm selected");
			clustering = new Clustering(inputFilename, numberOfClusters, realCentroidsFilename, new KMeans());
		} else if(algorithmName.equals("rs")){
			System.out.println("Random swap algorithm selected");
			clustering = new Clustering(inputFilename, numberOfClusters, realCentroidsFilename, new RandomSwap());
		} else if (algorithmName.equals("sr")){
			System.out.println("Stochastic relaxation algorithm selected");
			clustering = new Clustering(inputFilename, numberOfClusters, realCentroidsFilename, new StochasticRelaxation());
		} else if (algorithmName.equals("fkm")){
			// default algorithm fast k-means
			System.out.println("Fast k-means algorithm selected");
			clustering = new Clustering(inputFilename, numberOfClusters, realCentroidsFilename, new FastKMeans());
		} else {
			System.err.println("Invalid algorithm option: " + algorithmName);
			printUsage();
			return;
		}
		
		clustering.runMultiple(repeats);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFilename)));
			for(double[] c : clustering.dataset.centroids){
				for(double d : c)
					bw.write(d + " ");
				bw.newLine();
			}
			bw.close();
		} catch(IOException e){
			System.err.println("Encountered error while writing output file");
		}
	}

	/**
	 * Prints usage info
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("Usage: java Clustering <input file> <number of clusters>");
		System.out.println("Additional options:");
		System.out.println("\t\t-c <real centroids file name>");
		System.out.println("\t\t-r <number of repeats>");
		System.out.println("\t\t-o <output filename>");
		System.out.println("\t\t\t default: <input file>-centroids.txt");
		System.out.println("\t\t-a <algorithm name>");
		System.out.println("\t\t\t fkm\t- fast k-means (default)");
		System.out.println("\t\t\t rs\t- random swap");
		System.out.println("\t\t\t sr\t- stochastic relaxation");
		System.out.println("\t\t\t km\t- normal k-means");
	}

	/**
	 * Dataset (and best clustering result found)
	 */
	public Dataset dataset;

	/**
	 * Algorithm used for clustering
	 */
	public ClusteringAlgorithm algorithm;
	
	/* STATISTICS (unused) */
	
	public int timesRepeated = 0;
	/** How many times CI 0 was reached across all repeats */
	public int timesCI0Reached = 0;
	public double cumulativeTSE = 0;
	public double cumulativeNMSE = 0;
	public double cumulativeCI = 0;
	// (cumulative) runtime
	public double runtimeMs = 0;
	
	/**
	 * 
	 * @param filename
	 *            dataset file name
	 * @param numberOfClusters
	 *            expected number of clusters
	 * @param realCentroidFilename
	 *            file name for real centroid file; can be null
	 */
	public Clustering(String filename, int numberOfClusters, String realCentroidFilename, ClusteringAlgorithm algorithm) {
		try {
			this.dataset = new Dataset(filename, numberOfClusters);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to read input file");
			System.exit(1);
		} catch (IllegalArgumentException e) {
			if(e instanceof NumberFormatException){
				e.printStackTrace();
				System.err.println("Input file is not properly formatted.");
				System.exit(1);
			} else {
				e.printStackTrace();
				System.err.println(e.getMessage());
				System.exit(1);
			}

		}
		
		if (realCentroidFilename != null && realCentroidFilename.length() > 0) {
			try {
				dataset.loadRealCentroids(realCentroidFilename);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Unable to read real centroid file");
				System.exit(1);
			} catch(IllegalArgumentException e){
				if(e instanceof NumberFormatException){
					e.printStackTrace();
					System.err.println("Real centroid file is not properly formatted.");
					System.exit(1);
				} else if(e.getMessage().contains("Number of clusters specified does not match")) {
					e.printStackTrace();
					System.err.println("The number of clusters specified does not match with the real centroids file.");
					System.exit(1);
				}
			}
		}
		this.algorithm = algorithm;
	}

	/**
	 * Repeats the algorithm once
	 */
	public void run() {
		runMultiple(1);
	}

	/**
	 * Repeats the selected algorithm multiple times, best result saved in {@link #dataset}
	 * 
	 * @param repeats
	 *            number of repeated runs
	 */
	public void runMultiple(int repeats) {
		long startTime = System.nanoTime();
		System.out.println("Repeat\tMSE\t\tCI\ttime (seconds)");
		dataset.initializeRandomCentroids();
		double bestMSE = dataset.MSE();
		for (int repeat = 1; repeat <= repeats; repeat++) {
			Dataset newDataset = dataset.copy();
			newDataset.initializeRandomCentroids();
			algorithm.cluster(newDataset);
			double MSE = updateStatistics(newDataset);
			if (MSE < bestMSE) {
				int CI = -1;
				if (newDataset.realCentroids != null) {
					CI = newDataset.centroidIndex();
				}
				System.out.printf("%-6d  %-14.2f  %-6d  %-6.2f", repeat, MSE, CI,
						(((double) (System.nanoTime() - startTime) / 1000000000)));
				System.out.println();
				dataset = newDataset;
				bestMSE = MSE;
			}
		}
		runtimeMs = (System.nanoTime() - startTime) / 1000000.0;
		System.out.println("Total time: " + (runtimeMs/1000.0) + " seconds");
	}
	
	/**
	 * Updates statistics after each repeated run. 
	 * 
	 * @return MSE value of the dataset to save recalculation
	 */
	private double updateStatistics(Dataset newDataset){
		timesRepeated++;
		if (newDataset.realCentroids != null) {
			int CI = newDataset.centroidIndex();
			cumulativeCI += CI;
			if (CI == 0)
				timesCI0Reached++;
		}
		double TSE = newDataset.TSE();
		cumulativeTSE += TSE;
		double MSE = TSE / dataset.data.length;
		cumulativeNMSE += MSE / dataset.data[0].length;
		return MSE;
	}
}
