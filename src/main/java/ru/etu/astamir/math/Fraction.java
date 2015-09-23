package ru.etu.astamir.math;


/**
 * Объект класса реализуют обыкновенную дробь. По сути, объект представляет собой пару целых чисел
 * числителя {@link #numerator} и знаменателя {@link #denumerator}.
 *
 * @author Astamir
 */
public class Fraction implements java.io.Serializable, Cloneable, Comparable<Fraction> {
    private static final long serialVersionUID = 1L;

    /**
     * Числитель дроби
     */
    private int numerator;

    /**
     * Знаменатель дроби
     */
    private int denumerator;

    public Fraction(int numerator, int denumerator) {
        this.numerator = numerator;
        this.denumerator = denumerator;
    }

    public Fraction(int numerator) {
        this.numerator = numerator;
        this.denumerator = 1;
    }

    /**
     * Получение наибольшего общего делителя числителя {@link #numerator} и знаменателя
     * {@link #denumerator} дроби.
     *
     * @see #nod(int, int) Алгоритм нахождения НОД двух целых чисел
     * @return наибольший общий делитель числителя и знаменателя дроби.
     */
    public int nod() {
        return Fraction.nod(Math.abs(numerator), Math.abs(denumerator));
    }

    /**
     * Алгоритм Эвклида для поиска наибольшего обшего делителя двух целых чисел.
     *
     * @param a первое число
     * @param b второе число
     * @return наибольший общий делитель чисел a и b
     *
     */
    public static int nod(int a, int b) {
        while (a != 0 && b != 0) {
            if (a >= b)
                a = a % b;
            else
                b = b % a;
        }
        return a + b;
    }

    /**
     * Алгоритм расчета наименьшего общего кратного двух целых чисел.
     *
     * @see #nod(int, int) Алгоритм нахождения наибольшего общего делителя
     * @param a
     * @param b
     * @return наименьшее общее кратное числе a и b
     */
    public static int nok(int a, int b) {
        return (a * b) / nod(a, b);
    }

    /**
     * Алгоритм расчета наименьшего общего кратного числителя {@link #numerator} и знаменателя
     * {@link #denumerator} дроби.
     *
     * @return наименьшее общее кратное числителя и знаменателя дроби
     */
    public int nok() {
        return Fraction.nok(Math.abs(numerator), Math.abs(denumerator));
    }

    /**
     * Упрощение данной дроби - деление числителя и знаменателя на их наибольший общий делитель.
     *
     * @see #nod(int, int) Алгоритм нахождения наибольшего общего делителя
     *
     */
    public Fraction simplify() {
        int nod = nod();
        if (nod > 1) {
            numerator /= nod;
            denumerator /= nod;
        }

        return this;
    }

    /**
     * Упрощение заданной дроби - деление числителя и знаменателя на их наибольший общий делитель.
     *
     * @param fr упрощаемая дробь
     * @return упрощенная дробь
     */
    public static Fraction simplify(Fraction fr) {
        Fraction result = (Fraction) fr.clone();
        result.simplify();
        return result;
    }

    /**
     * Обращение дроби
     *
     * @param fr дробь которую нужно обратить(перевернуть)
     * @return Дробь, обратная данной.
     */
    public static Fraction reverse(Fraction fr) {
        return new Fraction(fr.denumerator, fr.numerator);
    }

    /**
     * Сложение двух дробей.
     *
     * @param fr1 Первая дробь.
     * @param fr2 Вторая дробь
     * @return Результат сложения тоже дробь (К.О.)
     */
    public static Fraction plus(Fraction fr1, Fraction fr2) {
        return new Fraction(fr1.numerator * fr2.denumerator + fr2.numerator * fr1.denumerator,
                fr1.denumerator * fr2.denumerator);
    }

    /**
     * Сложlение дроби с числом.
     *
     * @param fr Дробь
     * @param number Число которое нужно прибавить к дробе.
     * @return Дробь, полученная сложением данной и числа.
     */
    public static Fraction plus(Fraction fr, int number) {
        return Fraction.plus(fr, new Fraction(number, 1));
    }

    /**
     *
     * @param fr1
     * @param fr2
     * @return
     */
    public static Fraction minus(Fraction fr1, Fraction fr2) {
        return new Fraction(fr1.numerator * fr2.denumerator - fr2.numerator * fr1.denumerator,
                fr1.denumerator * fr2.denumerator);
    }

    /**
     *
     * @param fr
     * @param number
     * @return
     */
    public static Fraction minus(Fraction fr, int number) {
        return Fraction.minus(fr, new Fraction(number, 1));
    }

    /**
     *
     * @param fr
     */
    public void plus(Fraction fr) {
        this.numerator = this.numerator * fr.denumerator + this.numerator * fr.denumerator;
        this.denumerator = this.denumerator * fr.denumerator;
    }

    /**
     *
     * @param fr1
     * @param fr2
     * @return
     */
    public static Fraction multiply(Fraction fr1, Fraction fr2) {
        return new Fraction(fr1.numerator * fr2.numerator, fr1.denumerator * fr2.denumerator);
    }

    /**
     *
     * @param fr
     * @param number
     * @return
     */
    public static Fraction multiply(Fraction fr, int number) {
        return new Fraction(fr.numerator * number, fr.denumerator);
    }

    /**
     *
     * @param fr1
     * @param fr2
     * @return
     */
    public static Fraction devide(Fraction fr1, Fraction fr2) {
        return Fraction.multiply(fr1, Fraction.reverse(fr2));
    }

    /**
     *
     * @param fr
     * @param number
     * @return
     */
    public static Fraction devide(Fraction fr, int number) {
        return Fraction.devide(fr, new Fraction(number, 1));
    }

    /**
     *
     * @return
     */
    public double signum() {
        return Math.signum(numerator) * Math.signum(denumerator);
    }


    /**
     *
     * @param fr1
     * @param fr2
     * @return
     */
    public static int compare(Fraction fr1, Fraction fr2) {
        return fr1.compareTo(fr2);
    }

    public int getDenumerator() {
        return denumerator;
    }

    public void setDenumerator(int denumerator) {
        this.denumerator = denumerator;
    }

    public int getNumerator() {
        return numerator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    /**
     *
     * @param numerator
     * @param denumerator
     */
    public void setFraction(int numerator, int denumerator) {
        this.numerator = numerator;
        this.denumerator = denumerator;
    }

    /**
     *
     * @param fraction
     */
    public void setFraction(Fraction fraction) {
        this.numerator = fraction.numerator;
        this.denumerator = fraction.denumerator;
    }

    @Override
    public String toString() {
        return denumerator != 1 ? numerator + "/" + denumerator : String.valueOf(numerator);
    }

    /**
     *
     * @param fractionString
     * @return
     */
    public static Fraction createFromString(String fractionString) throws NumberFormatException {
        int numerator = 0;
        int denumerator = 0;
        String fracString = fractionString.replaceAll("\\s", "");
        if (fracString.contains("/")) {
            numerator = Integer.parseInt(fracString.substring(0,
                    fracString.indexOf("/")));
            if (!fracString.substring(fracString.indexOf("/") + 1).isEmpty())
                denumerator = Integer.parseInt(fracString.substring(fracString
                        .indexOf("/") + 1));
            else
                denumerator = 1;
        } else {
            numerator = Integer.parseInt(fracString);
            denumerator = 1;
        }

        return new Fraction(numerator, denumerator);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + denumerator;
        result = prime * result + numerator;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fraction other = (Fraction) obj;
        if (denumerator != other.denumerator)
            return false;
        if (numerator != other.numerator)
            return false;
        return true;
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public int compareTo(Fraction fr) {
        int thisVal = this.numerator * fr.denumerator;
        int otherVal = fr.numerator * this.denumerator;

        return thisVal < otherVal ? -1 : (thisVal == otherVal ? 0 : 1);
    }
}
