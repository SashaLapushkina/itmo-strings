public class Main {
    public static void main(String args[]) {
        StringList string = new StringList("абвгдеёжзийклмнопрстуфхцчшщЪыьэюя");
        try { //Я не уверена, нужен ли здесь try
            System.out.println(string.substring(5, 35));
            string.insert(30, "1234");
            string.setCharAt(1, '0');
            string.append("abc");
            System.out.print(string);
        } catch (MyException e) {

        }
    }
}

class StringList{
    static final int N = 16;
    private StringItem head;

    //Пустой конструктор
    private StringList() {
        head = null;
    }

    //Конструктор со строки
    public StringList(String string){
        //substring + toCharArray - пихнуть в блок
        StringItem x = new StringItem();
        head = x;

        for (int i = 0; i < string.length(); i++){
            if (x.count == N) { //Если блок заполнен, создаем новый
                x.next = new StringItem();
                x = x.next;
            } else
                x.addSymbol(string.charAt(i));
        }
    }

    //Копирующий конструктор
    private StringList(StringList newList) {
        if (newList.head == null) head = null;
        else {
            head = new StringItem(newList.head);
            StringItem newX = newList.head.next;
            StringItem x = head;
            while (newX != null) {
                x.next = new StringItem(newX);
                newX = newX.next;
            }
        }
    }

    //Длина строки
    public int length() {
        int sum = 0;
        StringItem x = head;
        while (x != null) {
            if (x.next != null && x.count + x.next.count <= N) {
                x.copyItem(0, x.next.count, x.next); //Схлопывание
            }
            else {
                sum += x.count;
                x = x.next;
            }
        }

        return sum;
    }

    //Поиск блока по индексу
    private Symbol findSymbol(int index){
        if (index < 0) throw new MyException("Индекс за границами строки");
        StringItem x = head;
        int sum = 0;
        while (x != null && sum + x.count < index) {
            sum += x.count;
            x = x.next;
        }
        if (x == null) throw new MyException("Индекс за границами строки");
        return new Symbol (index - sum, x);
    }

    //Символ по индексу
    public char charAt(int index) {
        Symbol symbol = findSymbol(index);
        return symbol.item.symbols[symbol.index];
    }

    //Заменить символ по индексу
    public void setCharAt(int index, char ch) {
        Symbol symbol = findSymbol(index);
        symbol.item.symbols[symbol.index] = ch;
    }

    //Подстрока, включая start и не включая end
    public StringList substring(int start, int end) {
        if (start >= end) throw new MyException("Индекс за границами строки");

        Symbol symbolStart = findSymbol(start);
        Symbol symbolEnd = findSymbol(end);

        StringList newList = new StringList();
        newList.head = new StringItem();

        if (symbolStart.item == symbolEnd.item) {
            //Если start и end в одном блоке - копируем
            newList.head.copyItem(symbolStart.index,symbolEnd.index - symbolStart.index, symbolStart.item);
        }
        else {
            //Скопировали вторую половину
            newList.head.copyItem(symbolStart.index, symbolStart.item.count, symbolStart.item);

            //Переходим к следующему блоку
            symbolStart.item = symbolStart.item.next;
            StringItem x = newList.head;

            //Копируем все, что между блоками со start и end
            while (symbolStart.item != symbolEnd.item) {
                x.next = new StringItem(symbolStart.item);
                symbolStart.item = symbolStart.item.next;
                x = x.next;
            }

            //Копируем первую половину блока с end
            x.next = new StringItem();
            x.next.copyItem(0, symbolEnd.index, symbolStart.item);
        }
        return newList;
    }

    //Вставить символ в конец
    public void append(char ch){
        StringItem x = getLast();
        if (x.count < N) { //Проверяем, есть ли место в блоке
            x.symbols[x.count++] = ch;
        }
        else {
            x.next = new StringItem();
            x.next.addSymbol(ch);
        }
    }

    //Добавление в конец
    public void append(String string){
        StringList newList = new StringList(string);
        this.appendList(newList);
    }

    //Добавление в конец
    public void append(StringList string){
        StringList newList = new StringList(string); //Заменить на копирование
        this.appendList(newList);
    }

    //Суть метода append
    private void appendList(StringList string) {
        if (head == null) head = string.head;
        else getLast().next = string.head;
    }

    //Вставка по индексу
    public void insert(int index, String string) {
        StringList newList = new StringList(string);
        this.insertList(index, newList);
    }

    //Вставка по индексу
    public void insert(int index, StringList string) {
        StringList newList = new StringList(string);
        this.insertList(index, newList);
    }

    //Суть метода insert
    private void insertList(int index, StringList string)  {
        if (index == 0) {
            string.append(this);
        } else {
            //Нашли символ
            Symbol symbol = findSymbol(index);

            //Скопировали вторую половину блока в новый блок
            StringItem newItem = new StringItem();
            newItem.copyItem(symbol.index, symbol.item.count, symbol.item);

            //Прикрепляем ко второй половине блока конец строки
            newItem.next = symbol.item.next;

            //Прикрепили вставляемую строку к блоку со второй половиной
            string.getLast().next = newItem;

            //Скопировали первую половину блока в новый блок
            StringItem item = new StringItem();
            item.copyItem(0, symbol.index, symbol.item);

            //Заменили массив в старом блоке на массив только с первой половиной символов
            symbol.item.symbols = item.symbols;

            //Прикрепили к старому блоку начало вставляемой строки
            symbol.item.next = string.head;
        }
    }

    //Взятие последнего блока в строке
    private StringItem getLast() {
        StringItem x = head, prX = head;
        while (x != null) {
            prX = x;
            x = x.next;
        }
        return prX;
    }

    //Преобразование в строку
    public String toString(){
        String string = "";
        StringItem x = this.head;
        while (x != null) {
            string += String.valueOf(x.symbols); //Добавление к строке строку из массива одного блока
            x = x.next;
        }
        return string;
    }

    //Класс блока
    static class StringItem{
        private char[] symbols = new char[N];
        private StringItem next;
        private byte count = 0;

        //Пустой конструктор
        private StringItem() {}

        //Копирующий конструктор
        private StringItem(StringItem newItem) {
            this.copyItem(0, newItem.count, newItem);
        }

        //Копирование в конец данного блока символы от start до end из блока item
        private void copyItem(int start, int end, StringItem item) {
            for (int i = start; i < end; i++) {
                this.addSymbol(item.symbols[i]);
            }
        }

        //Добавление символа в конец блока
        private void addSymbol(char ch) {
            symbols[count++] = ch;
        }
    }

    //Класс используется для поиска символа по индексу. Хранит блок с данным символом и его номер в этом блоке
    static class Symbol {
        private int index; //Индекс символа в блоке
        private StringItem item; //Блок

        Symbol(int index, StringItem item) {
            this.index = index;
            this.item = item;
        }
    }
}

class MyException extends RuntimeException {
    private String info;
    MyException(String string) {
        info = string;
    }

    public void printInfo(){
        System.out.println(info);
    }
}
