import java.io.*;
import java.util.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class SolveNew {
	FastScanner in;
	PrintWriter out;

	int[][] wallSum;

	boolean wallInside(int x1, int y1, int x2, int y2) {
		int xFr = Math.min(x1, x2), xTo = Math.max(x1, x2);
		int yFr = Math.min(y1, y2), yTo = Math.max(y1, y2);
		return wallSum[xTo][yTo] - wallSum[xFr - 1][yTo]
				- wallSum[xTo][yFr - 1] + wallSum[xFr - 1][yFr - 1] != 0;
	}

	int r;
	char[][] a;

	final static boolean printPicture = false;

	void solve() {
		int n = in.nextInt();
		int m = in.nextInt();
		r = in.nextInt();
		System.err.println(n + " " + m + " " + r);
		int backbone = in.nextInt();
		int router = in.nextInt();
		System.err.println(backbone + " " + router);
		int budget = in.nextInt();
		System.err.println("money " + budget);
		int stX = in.nextInt(), stY = in.nextInt();
		out.println(stX + " " + stY);
		a = new char[n][];
		for (int i = 0; i < n; i++) {
			a[i] = in.next().toCharArray();
			for (int j = 0; j < m; j++) {
				if (a[i][j] == '-') {
					a[i][j] = '#';
				}
			}
		}

		wallSum = new int[n + 1][m + 1];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				wallSum[i + 1][j + 1] = wallSum[i][j + 1] + wallSum[i + 1][j]
						- wallSum[i][j];
				if (a[i][j] == '#') {
					wallSum[i + 1][j + 1]++;
				}
			}
		}
		int totalPoints = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (a[i][j] == '.') {
					totalPoints++;
				}
			}
		}
		System.err.println("total Points = " + totalPoints);
		int routersNumMax = budget / (r * 2 * backbone + router);
		System.err.println("routers num ~" + routersNumMax);
		System.err.println("want to cover " + (totalPoints / routersNumMax)
				+ " by each router (r = " + r + ", max = " + (2 * r + 1)
				* (2 * r + 1) + ")");
		System.err.println();

		routersUsed = 0;
		coveredByRouters = 0;
		coverLeft = new int[n][m][2 * r + 1];
		coverRight = new int[n][m][2 * r + 1];
		routerHere = new boolean[n][m];
		coveredByXRoutes = new int[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				coverLeft[i][j][r] = 1 + (j == 0 ? 0 : coverLeft[i][j - 1][r]);
				if (i != 0) {
					for (int dx = 1; dx <= r; dx++) {
						coverLeft[i][j][r - dx] = Math.min(coverLeft[i][j][r],
								coverLeft[i - 1][j][r - dx + 1]);
					}
				}
				for (int dx = 0; dx <= 2 * r; dx++) {
					if (a[i][j] == '#') {
						coverLeft[i][j][dx] = 0;
					} else {
						coverLeft[i][j][dx] = Math.min(coverLeft[i][j][dx],
								r + 1);
					}
				}

			}
			for (int j = m - 1; j >= 0; j--) {
				coverRight[i][j][r] = 1 + (j == m - 1 ? 0
						: coverRight[i][j + 1][r]);
				if (i != 0) {
					for (int dx = 1; dx <= r; dx++) {
						coverRight[i][j][r - dx] = Math.min(
								coverRight[i][j][r], coverRight[i - 1][j][r
										- dx + 1]);
					}
				}
				for (int dx = 0; dx <= 2 * r; dx++) {
					if (a[i][j] == '#') {
						coverRight[i][j][dx] = 0;
					} else {
						coverRight[i][j][dx] = Math.min(coverRight[i][j][dx],
								r + 1);
					}
				}
			}
		}
		for (int i = n - 1; i >= 0; i--) {
			for (int j = 0; j < m; j++) {
				if (i != n - 1) {
					for (int dx = 1; dx <= r; dx++) {
						coverLeft[i][j][r + dx] = Math.min(coverLeft[i][j][r],
								coverLeft[i + 1][j][r + dx - 1]);
						coverRight[i][j][r + dx] = Math.min(
								coverRight[i][j][r], coverRight[i + 1][j][r
										+ dx - 1]);
					}
				}
				for (int dx = 0; dx <= 2 * r; dx++) {
					if (a[i][j] == '#') {
						coverLeft[i][j][dx] = 0;
						coverRight[i][j][dx] = 0;
					} else {
						coverLeft[i][j][dx] = Math.min(coverLeft[i][j][dx],
								r + 1);
						coverRight[i][j][dx] = Math.min(coverRight[i][j][dx],
								r + 1);
					}
				}
			}
		}
		cntCover = new int[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				for (int dx = -r; dx <= r; dx++) {
					int sz = Math.max(0, coverLeft[i][j][r + dx]
							+ coverRight[i][j][r + dx] - 1);
					cntCover[i][j] += sz;
				}
			}
		}

		routersXY = new Router[n][m];
		int haveMore = Integer.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (a[i][j] == '.' && haveMore > 0) {
					haveMore--;
					// System.err.println("? " + i + " " + j);
					// System.err.println(coverLeft[i][j][r]);
					int maxCover = 0, maxI = -1, maxJ = -1;
					for (int nx = i - r; nx <= i + r; nx++) {
						for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
								+ coverRight[i][j][r + nx - i]; ny++) {
							if (cntCover[nx][ny] >= maxCover) {
								maxCover = cntCover[nx][ny];
								maxI = nx;
								maxJ = ny;
							}
						}
					}
					if (maxCover == 0) {
						throw new AssertionError();
					}
					useXY(maxI, maxJ);
				}
			}
		}

		ArrayList<Router> routers = new ArrayList<SolveNew.Router>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (routerHere[i][j]) {
					Router curRouter = new Router(i, j);
					curRouter.updateCover();
					routersXY[i][j] = curRouter;
					routers.add(curRouter);
				}
			}
		}
		while (routers.size() > routersNumMax) {
			int bestId = 0;
			for (int i = 0; i < routers.size(); i++) {
				if (routers.get(i).cover < routers.get(bestId).cover) {
					bestId = i;
				}
			}
			System.err.println("remove " + routers.get(bestId).cover + " ("
					+ routers.size() + "/ " + routersNumMax + ")");
			routers.get(bestId).removeRouter();
			routers.remove(bestId);
		}

		out.println(budget - routersUsed * router);
		out.println(routersUsed);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				if (routerHere[i][j]) {
					out.println(i + " " + j);
				}
			}
		}

		if (printPicture) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					if (routerHere[i][j]) {
						out.print('*');
					} else {
						out.print(a[i][j]);
					}
				}
				out.println();
			}
		}
		out.close();
		System.err.println("tot = " + totalPoints + ", covered = "
				+ coveredByRouters);
		System.err.println("used routers " + routersUsed);
		System.err.println("used money " + (routersUsed * router)
				+ ", budget = " + budget);

		System.err.println("max score: " + (totalPoints * 1000 + budget));

		if (routersUsed * router > budget) {
			System.err
					.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// throw new AssertionError();
		}

	}

	Router[][] routersXY;

	void updateNearbyRouters(int i, int j) {
		for (int nx = i - r; nx <= i + r; nx++) {
			for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
					+ coverRight[i][j][r + nx - i]; ny++) {
				if (routersXY[nx][ny] != null) {
					routersXY[nx][ny].updateCover();
				}
			}
		}
	}

	class Router implements Comparable<Router> {
		int i, j;
		int cover;

		public Router(int x, int y) {
			super();
			this.i = x;
			this.j = y;
		}

		public int compareTo(Router o) {
			return Integer.compare(cover, o.cover);
		}

		void removeRouter() {
			routerHere[i][j] = false;
			routersUsed--;
			for (int nx = i - r; nx <= i + r; nx++) {
				for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
						+ coverRight[i][j][r + nx - i]; ny++) {
					coveredByXRoutes[nx][ny]--;
					if (coveredByXRoutes[nx][ny] == 1) {
						updateNearbyRouters(nx, ny);
					}
					if (coveredByXRoutes[nx][ny] == 0) {
						coveredByRouters--;
					}
				}
			}
		}

		void updateCover() {
			cover = 0;
			for (int nx = i - r; nx <= i + r; nx++) {
				for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
						+ coverRight[i][j][r + nx - i]; ny++) {
					if (coveredByXRoutes[nx][ny] == 1) {
						cover++;
					}
				}
			}
		}

	}

	int routersUsed = 0;
	int coveredByRouters = 0;

	boolean[][] routerHere;
	int[][] cntCover;
	int[][][] coverLeft;
	int[][][] coverRight;
	int[][] coveredByXRoutes;

	void wasCovered(int i, int j) {
		if (a[i][j] != '.') {
			return;
		}
		// System.err.println("color " + i + " " + j);
		a[i][j] = '-';
		for (int nx = i - r; nx <= i + r; nx++) {
			for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
					+ coverRight[i][j][r + nx - i]; ny++) {
				cntCover[nx][ny]--;
			}
		}
	}

	void useXY(int i, int j) {
		// System.err
		// .println("use " + i + ", " + j + " covered " + cntCover[i][j]);
		coveredByRouters += cntCover[i][j];
		routerHere[i][j] = true;
		routersUsed++;
		for (int nx = i - r; nx <= i + r; nx++) {
			for (int ny = j - coverLeft[i][j][r + nx - i] + 1; ny < j
					+ coverRight[i][j][r + nx - i]; ny++) {
				wasCovered(nx, ny);
				coveredByXRoutes[nx][ny]++;
			}
		}
	}

	void run() {
		// String[] tests = new String[] { "charleston_road", "lets_go_higher",
		// "opera", "rue_de_londres" };
		String[] tests = new String[] { "opera" };
		try {
			for (String t : tests) {
				System.err.println("do " + t);
				in = new FastScanner(new File("tests/" + t + ".in"));
				out = new PrintWriter(new File("tests/answers/" + t + ".out"));

				solve();

				out.close();
				System.err.println("---------------------------------");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	void runIO() {

		in = new FastScanner(System.in);
		out = new PrintWriter(System.out);

		solve();

		out.close();
	}

	class FastScanner {
		BufferedReader br;
		StringTokenizer st;

		public FastScanner(File f) {
			try {
				br = new BufferedReader(new FileReader(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		public FastScanner(InputStream f) {
			br = new BufferedReader(new InputStreamReader(f));
		}

		String next() {
			while (st == null || !st.hasMoreTokens()) {
				String s = null;
				try {
					s = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (s == null)
					return null;
				st = new StringTokenizer(s);
			}
			return st.nextToken();
		}

		boolean hasMoreTokens() {
			while (st == null || !st.hasMoreTokens()) {
				String s = null;
				try {
					s = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (s == null)
					return false;
				st = new StringTokenizer(s);
			}
			return true;
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		long nextLong() {
			return Long.parseLong(next());
		}

		double nextDouble() {
			return Double.parseDouble(next());
		}
	}

	public static void main(String[] args) {
		new SolveNew().run();
	}
}