package ru.etu.astamir.common.test;

import ru.etu.astamir.common.Pair;
import ru.etu.astamir.math.MathUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Astamir
 * Date: 03.02.14
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
class Test {
    static int hits = 0;

    public static void main(String... args) throws Exception{
//        System.out.println(solution(new int[]{0, 1,1,1, 2, 2, 3, 5},
//                new int[]{500000, 500000,600000,700000, 0,0,0,20000}));
//        System.out.println(calc(0, 500000));
        System.out.println(pass("aDd0fda0dsdD"));
    }

    public static int sol(int[] A, int[] B) {
        int n = A.length;
        int m = B.length;;
        Arrays.sort(A);
        Arrays.sort(B);
        int i = 0;
        for (int k = 0;k < n;k++) {
            while (i < m - 1 && B[i] < A[k])
                i += 1;
            if (A[k] == B[i])
                return A[k];
        }
        return -1;
    }

    public static int pass(String S) {
        char[] chars = S.toCharArray();
        int max = 0;
        int current = 0;
        boolean upper = false;
        for (char c : chars) {
            if (Character.getType(c) == Character.DECIMAL_DIGIT_NUMBER) {
                if (upper) {
                    max = Math.max(max, current);
                }
                upper = false;
                current = 0;
                continue;
            }
            if (Character.getType(c) == Character.UPPERCASE_LETTER) {
                upper = true;
            }
            current++;
        }
        max = Math.max(max, current);

        return max;
    }

    public static int solution(int[] A) {
        int max_depth = -1;
        int n = A.length;

        int p = 0;
        int q = -1;
        int r = -1;
        for (int i = 1; i < n; i++) {
            if (q == -1 && A[i] >= A[i - 1]) {
                q = i - 1;
            }

            if (q >= 0 && (A[i] <= A[i-1] || i + 1 == n)) {
                if (A[i] <= A[i-1]) {
                    r = i - 1;
                } else {
                    r = i;
                }
                int depth = depth(A, p, q, r);
                if (depth > max_depth) {
                    max_depth = depth;
                }
                p = i - 1;
                q = -1;
                r = -1;
            }
        }
        return max_depth;
    }

    static int solution1(int[] A) {
        int depth = 0;

        int P = 0, Q = -1, R = -1;

        for (int i = 1; i < A.length; i++)
        {
            if (Q < 0 && A[i] >= A[i-1])
                Q = i-1;

            if ((Q >= 0 && R < 0) &&
                    (A[i] <= A[i-1] || i + 1 == A.length))
            {
                if (A[i] <= A[i-1])
                    R = i - 1;
                else
                    R = i;
                System.out.println(P+"  "+Q+"  "+R);
                depth = Math.max(depth, Math.min(A[P]-A[Q], A[R]-A[Q]));
                P = i - 1;
                Q = R = -1;
            }
        }
        if (depth == 0) depth = -1;
        return depth;
    }

    public static int depth(int[] a, int p, int q, int r) {
        return Math.min(a[p] - a[q], a[r] - a[q]);
    }

    public static double calc(int a, int b) {
        return a + b / 1000000D;
    }

    public static int pairs(int n) {
        if (n < 0) return 0;
        if (n <= 2) return n;
        int sum = n;
        for (int i = 1; i < n; i++) {
            sum += n - i;
        }
        return sum;
    }

    public static boolean isEven(int number) {
        return (number & 1) == 0;
    }
}

