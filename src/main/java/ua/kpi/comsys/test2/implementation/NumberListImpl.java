/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * @author Бечке Олексій Ігорович
 * @group ІА-32
 * @recordBook 3202
 */

package ua.kpi.comsys.test2.implementation;

import java.io.*;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Кільцева однонаправлена реалізація списку для представлення чисел.
 * Для номера заліковки 3202:
 * - 3202 % 5 = 2: основна система - вісімкова (8), додаткова - десяткова (10)
 * - 3202 % 7 = 3: додаткова операція - цілочисельне ділення
 */
public class NumberListImpl implements NumberList {

    private Node head;
    private int size;
    private int radix; // Система числення (8 або 10)

    /**
     * Внутрішній клас для вузла кільцевого списку
     */
    private static class Node {
        Byte data;
        Node next;

        Node(Byte data) {
            this.data = data;
            this.next = null;
        }
    }

    /**
     * Конструктор за замовчуванням. Створює порожній список у вісімковій системі.
     */
    public NumberListImpl() {
        this.head = null;
        this.size = 0;
        this.radix = 8; // За замовчуванням вісімкова
    }

    /**
     * Внутрішній конструктор для створення списку в певній системі числення.
     */
    private NumberListImpl(int radix) {
        this.head = null;
        this.size = 0;
        this.radix = radix;
    }

    /**
     * Конструктор, що створює список з файлу з десятковим числом.
     *
     * @param file файл з десятковим числом
     */
    public NumberListImpl(File file) {
        this();
        if (file == null || !file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.trim().isEmpty()) {
                initFromDecimalString(line.trim());
            }
        } catch (IOException e) {
            // Залишаємо список порожнім
        }
    }

    /**
     * Конструктор, що створює список з рядкового представлення десяткового числа.
     *
     * @param value десяткове число у вигляді рядка
     */
    public NumberListImpl(String value) {
        this();
        if (value != null && !value.isEmpty()) {
            initFromDecimalString(value);
        }
    }

    /**
     * Ініціалізує список з десяткового рядка, конвертуючи в вісімкову систему.
     */
    private void initFromDecimalString(String decimal) {
        if (!isValidDecimal(decimal)) {
            return;
        }

        BigInteger num = new BigInteger(decimal);
        if (num.signum() < 0) {
            return;
        }

        String octal = num.toString(8);
        for (int i = 0; i < octal.length(); i++) {
            add((byte) (octal.charAt(i) - '0'));
        }
    }

    /**
     * Перевіряє, чи є рядок валідним десятковим числом.
     */
    private boolean isValidDecimal(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Зберігає число у файл в десятковій системі числення.
     *
     * @param file файл для збереження
     */
    public void saveList(File file) {
        if (file == null) {
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(toDecimalString());
        } catch (IOException e) {
            // Ігноруємо помилки запису
        }
    }

    /**
     * Повертає номер заліковки студента.
     *
     * @return 3202
     */
    public static int getRecordBookNumber() {
        return 3202;
    }

    /**
     * Конвертує число з вісімкової (поточної) у десяткову систему числення.
     * 3202 % 5 = 2: octal -> decimal
     *
     * @return новий список з числом у десятковій системі
     */
    public NumberListImpl changeScale() {
        if (isEmpty()) {
            return new NumberListImpl(10);
        }

        // Отримуємо десяткове значення
        String decimal = toDecimalString();

        // Створюємо новий список у десятковій системі
        NumberListImpl result = new NumberListImpl(10);

        // Додаємо цифри десяткового числа
        for (int i = 0; i < decimal.length(); i++) {
            result.add((byte) (decimal.charAt(i) - '0'));
        }

        return result;
    }

    /**
     * Виконує операцію цілочисельного ділення.
     * 3202 % 7 = 3: integer division
     *
     * @param arg дільник
     * @return результат ділення
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (arg == null || arg.isEmpty() || this.isEmpty()) {
            return new NumberListImpl();
        }

        BigInteger dividend = new BigInteger(this.toDecimalString());
        BigInteger divisor = new BigInteger(((NumberListImpl) arg).toDecimalString());

        if (divisor.equals(BigInteger.ZERO)) {
            return new NumberListImpl();
        }

        BigInteger result = dividend.divide(divisor);
        return new NumberListImpl(result.toString());
    }

    /**
     * Повертає рядкове представлення числа в десятковій системі.
     *
     * @return десяткове представлення
     */
    public String toDecimalString() {
        if (isEmpty()) {
            return "0";
        }

        BigInteger result = BigInteger.ZERO;
        BigInteger base = BigInteger.valueOf(radix);

        Node current = head;
        for (int i = 0; i < size; i++) {
            result = result.multiply(base).add(BigInteger.valueOf(current.data));
            current = current.next;
        }

        return result.toString();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (radix == 16 && current.data >= 10) {
                // Для шістнадцяткової системи
                sb.append((char) ('A' + (current.data - 10)));
            } else {
                sb.append(current.data);
            }
            current = current.next;
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberListImpl)) return false;

        NumberListImpl other = (NumberListImpl) o;

        // Порівнюємо за десятковим значенням
        return this.toDecimalString().equals(other.toDecimalString());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) return false;

        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data.equals(o)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new NumberListIterator();
    }

    private class NumberListIterator implements Iterator<Byte> {
        private Node current = head;
        private int count = 0;

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public Byte next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Byte data = current.data;
            current = current.next;
            count++;
            return data;
        }
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node current = head;
        for (int i = 0; i < size; i++) {
            arr[i] = current.data;
            current = current.next;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null; // Не реалізовується за завданням
    }

    @Override
    public boolean add(Byte e) {
        if (e == null || e < 0 || e >= radix) {
            return false;
        }

        Node newNode = new Node(e);

        if (head == null) {
            head = newNode;
            newNode.next = head;
        } else {
            Node tail = getTail();
            tail.next = newNode;
            newNode.next = head;
        }

        size++;
        return true;
    }

    private Node getTail() {
        if (head == null) return null;

        Node current = head;
        for (int i = 0; i < size - 1; i++) {
            current = current.next;
        }
        return current;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte) || isEmpty()) {
            return false;
        }

        if (head.data.equals(o)) {
            if (size == 1) {
                head = null;
            } else {
                Node tail = getTail();
                head = head.next;
                tail.next = head;
            }
            size--;
            return true;
        }

        Node current = head;
        for (int i = 0; i < size - 1; i++) {
            if (current.next.data.equals(o)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean modified = false;
        for (Byte b : c) {
            if (add(b)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }

        boolean modified = false;
        for (Byte b : c) {
            add(index++, b);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            while (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node current = head;

        for (int i = 0; i < size; ) {
            if (!c.contains(current.data)) {
                Byte data = current.data;
                current = current.next;
                remove(data);
                modified = true;
            } else {
                current = current.next;
                i++;
            }
        }

        return modified;
    }

    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    @Override
    public Byte get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    @Override
    public Byte set(int index, Byte element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (element == null || element < 0 || element >= radix) {
            throw new IllegalArgumentException();
        }

        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        Byte oldValue = current.data;
        current.data = element;
        return oldValue;
    }

    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        if (element == null || element < 0 || element >= radix) {
            throw new IllegalArgumentException();
        }

        if (index == size) {
            add(element);
            return;
        }

        Node newNode = new Node(element);

        if (index == 0) {
            if (head == null) {
                head = newNode;
                newNode.next = head;
            } else {
                Node tail = getTail();
                newNode.next = head;
                head = newNode;
                tail.next = head;
            }
        } else {
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }

        size++;
    }

    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        Byte removedData;

        if (index == 0) {
            removedData = head.data;
            if (size == 1) {
                head = null;
            } else {
                Node tail = getTail();
                head = head.next;
                tail.next = head;
            }
        } else {
            Node current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            removedData = current.next.data;
            current.next = current.next.next;
        }

        size--;
        return removedData;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }

        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data.equals(o)) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }

        int lastIndex = -1;
        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.data.equals(o)) {
                lastIndex = i;
            }
            current = current.next;
        }
        return lastIndex;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return new NumberListListIterator(0);
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        return new NumberListListIterator(index);
    }

    private class NumberListListIterator implements ListIterator<Byte> {
        private int currentIndex;
        private Node lastReturned;

        NumberListListIterator(int index) {
            this.currentIndex = index;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        @Override
        public Byte next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = getNodeAt(currentIndex);
            currentIndex++;
            return lastReturned.data;
        }

        @Override
        public boolean hasPrevious() {
            return currentIndex > 0;
        }

        @Override
        public Byte previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            currentIndex--;
            lastReturned = getNodeAt(currentIndex);
            return lastReturned.data;
        }

        @Override
        public int nextIndex() {
            return currentIndex;
        }

        @Override
        public int previousIndex() {
            return currentIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Byte e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            lastReturned.data = e;
        }

        @Override
        public void add(Byte e) {
            NumberListImpl.this.add(currentIndex, e);
            currentIndex++;
            lastReturned = null;
        }
    }

    private Node getNodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }

        NumberListImpl sublist = new NumberListImpl(this.radix);
        Node current = head;

        for (int i = 0; i < fromIndex; i++) {
            current = current.next;
        }

        for (int i = fromIndex; i < toIndex; i++) {
            sublist.add(current.data);
            current = current.next;
        }

        return sublist;
    }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }

        if (index1 == index2) {
            return true;
        }

        Node node1 = getNodeAt(index1);
        Node node2 = getNodeAt(index2);

        Byte temp = node1.data;
        node1.data = node2.data;
        node2.data = temp;

        return true;
    }

    @Override
    public void sortAscending() {
        if (size <= 1) {
            return;
        }

        for (int i = 0; i < size - 1; i++) {
            Node current = head;
            for (int j = 0; j < size - i - 1; j++) {
                if (current.data > current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                }
                current = current.next;
            }
        }
    }

    @Override
    public void sortDescending() {
        if (size <= 1) {
            return;
        }

        for (int i = 0; i < size - 1; i++) {
            Node current = head;
            for (int j = 0; j < size - i - 1; j++) {
                if (current.data < current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                }
                current = current.next;
            }
        }
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) {
            return;
        }

        head = head.next;
    }

    @Override
    public void shiftRight() {
        if (size <= 1) {
            return;
        }

        Node tail = getTail();
        head = tail;
    }
}
