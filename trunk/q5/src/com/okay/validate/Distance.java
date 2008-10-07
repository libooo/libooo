package com.okay.validate;
/*
 * 相似度计算
 * **/
public class Distance {

	private int Minimum(int a, int b, int c) {
		int mi;
		mi = a;
		if (b < mi) {
			mi = b;
		}
		if (c < mi) {
			mi = c;
		}
		return mi;
	}

	// *****************************
	// Compute Levenshtein distance
	// *****************************
	public int LD(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		for (i = 1; i <= n; i++) {
			s_i = s.charAt(i - 1);

			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);
				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,	d[i - 1][j - 1] + cost);
			}
		}
		// Step 7
		return d[n][m];
	}

}
