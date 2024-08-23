package snurkabill.net;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import snurkabill.net.domain.Bin;
import snurkabill.net.domain.BinPacking;
import snurkabill.net.domain.Brick;

public class BinPackingRunner {

	public static void main(String[] args) throws JsonProcessingException {
		SolverFactory factory = SolverFactory
				.createFromXmlInputStream(BinPackingRunner.class.getResourceAsStream("/snurkabill/net/config.xml"));
		Solver solver = factory.buildSolver();
		solver.solve(BinPackingGenerator.simpleSolution());
		BinPacking bestSolution = (BinPacking) solver.getBestSolution();

//		ObjectMapper mapper = new ObjectMapper();
//		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		System.out.println(bestSolution.getScore());

		Map<Bin, List<Brick>> solutionMap = new HashMap<>();
		for (Bin bin : bestSolution.getBinList()) {
			solutionMap.put(bin, new LinkedList<>());
		}

		for (Brick brick : bestSolution.getBrickList()) {
			solutionMap.get(brick.getBin()).add(brick);
		}

		solutionMap = solutionMap.entrySet().stream().filter(e -> !e.getValue().isEmpty())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		Set<Entry<Bin, List<Brick>>> bins = solutionMap.entrySet();

		printBillOfMaterialsSumUp(bins);

		boolean withVisual = true;
		boolean withFillIndicator = true;
		boolean withCost = true;
		printBillOfMaterials(bins, withVisual, withFillIndicator, withCost);
	}

	private static void printBillOfMaterialsSumUp(Set<Entry<Bin, List<Brick>>> bins) {
		System.out.println(bins.size() + " bins");
		// bins list
		System.out.println(
				bins.stream().collect(Collectors.groupingBy(e -> e.getKey().getVolume(), Collectors.counting())) //
						.entrySet().stream() //
						.sorted((a, b) -> b.getKey() - a.getKey()) //
						.map(e -> String.format("%-3d x %-5s", e.getValue(), e.getKey())) //
						.collect(Collectors.joining("\n\t", "\t", "")));
	}

	private static void printBillOfMaterials(Set<Entry<Bin, List<Brick>>> bins, boolean withVisual,
			boolean withFillIndicator, boolean withCost) {
		List<Integer> scrap = new LinkedList<>();

		bins.stream()//
				.sorted((e1, e2) -> e2.getKey().getVolume() - e1.getKey().getVolume())//
				.forEach(entry -> {
					System.out.println(entry.getKey() + " : " + getVisualBinBricks(entry));
					System.out.println("\tbricks : \n" + getBillOfMaterials(entry.getValue()));
					int usedVolume = ScoreCalcul.getUsedVolume(entry.getValue());
					int scrapVolume = ScoreCalcul.getScrapVolume(entry.getKey(), usedVolume, entry.getValue());
					System.out.println("\t\t- 1 x " + scrapVolume + " (scrap)");
					System.out.println("\tfill % : " + ((float) usedVolume / entry.getKey().getVolume()) * 100
							+ "% (scrap " + scrapVolume + ")");
					System.out.printf("\tcost : %.2f€ (scrap %.2f€)%n",
							ScoreCalcul.calculateApproxPrice(entry.getKey()),
							ScoreCalcul.calculateApproxPrice(scrapVolume));
					scrap.add(scrapVolume);
				});

		System.out.println("Total scrap : " + scrap.stream().mapToInt(i -> i).sum());
		System.out.println("Remaining scrap pieces : " + scrap.stream().sorted(Comparator.reverseOrder())
				.map(String::valueOf).collect(Collectors.joining(", ")));

		System.out.printf("Price : %.2f€%n", ScoreCalcul.calculatePrice(bins));
		System.out.printf("Scrap cost : %.2f€%n", ScoreCalcul.calculatePrice(scrap));
	}

	private static String getBillOfMaterials(List<Brick> bricks) {
		return "\t\t- " + bricks.stream()//
				.sorted((a, b) -> b.getVolume() - a.getVolume())//
				.collect(Collectors.groupingBy(Brick::getVolume, Collectors.counting())) //
				.entrySet().stream()//
				.sorted((a, b) -> b.getKey() - a.getKey()) //
				.map(e -> e.getValue() + " x " + e.getKey()) //
				.collect(Collectors.joining("\n\t\t- "));
	}

	private static final int stringRepresentationReductionCoef = 100;

	private static String getVisualBinBricks(Entry<Bin, List<Brick>> binBricks) {
		StringBuilder sb = new StringBuilder();
		int length = binBricks.getKey().getVolume() / stringRepresentationReductionCoef;
		int brickVolume;
		int bricksLength = 0;
		for (Brick b : binBricks.getValue().stream() //
				.sorted((a, b) -> b.getVolume() - a.getVolume()) //
				.collect(Collectors.toList()) //
		) {
			brickVolume = b.getVolume() / stringRepresentationReductionCoef;
			bricksLength += brickVolume;
			sb.append(StringUtils.repeat('-', brickVolume));
			sb.append("|");
		}
		sb.append(StringUtils.repeat("x", length - bricksLength));
		return sb.toString();
	}

}
