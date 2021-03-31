public class Main {
    public static void main(String args[]) {
        StringList string = new StringList("абвгдеёжзийклмнопрстуфхцчшщЪыьэюя");
        System.out.println(string.substring(5, 31));
        string.insert(2, "1234");
        string.setCharAt(1, '0');
        string.append("abc");
        System.out.print(string);
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
            if (x.count == 16) { //Если блок заполнен, создаем новый
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
        StringItem x = head;
        int sum = 0;
        while (sum + x.count < index) {
            sum += x.count;
            x = x.next;
        }
        return new Symbol (index - sum, x);
    }

    //Символ по индексу
    public char charAt(int index) {
        if (index > length()) throw new MyException("Индекс за границами строки");
        Symbol symbol = findSymbol(index);
        return symbol.item.symbols[symbol.index];
    }

    //Заменить символ по индексу
    public void setCharAt(int index, char ch) {
        if (index > length()) throw new MyException("Индекс за границами строки");
        Symbol symbol = findSymbol(index);
        symbol.item.symbols[symbol.index] = ch;
    }

    //Подстрока, включая start и не включая end
    public StringList substring(int start, int end) {
        if (start > length() || end > length() || start >= end) throw new MyException("Индекс за границами строки");

        Symbol symbol = findSymbol(start);

        //Запомнили, сколько символов до нашего блока
        int sum = start - symbol.index;

        //Скопировали первую половину (или часть блока, если start и end в одном блоке)
        StringList newList = new StringList();
        newList.head = new StringItem();
        newList.head.copyItem(symbol.index, (end - sum < symbol.item.count ? end - sum: symbol.item.count), symbol.item);

        //Переходим к следующему блоку
        sum += symbol.item.count;
        symbol.item = symbol.item.next;
        StringItem x = newList.head;

        //Копируем все, что между блоками со start и end
        while (sum + symbol.item.count < end) {
            x.next = new StringItem(symbol.item);
            sum += symbol.item.count;
            symbol.item = symbol.item.next;
            x = x.next;
        }

        //Если start и end не в одном блоке, копируем первую половину блока с end
        if (end - sum > 0) {
            x.next = new StringItem();
            x.next.copyItem(0, end - sum, symbol.item);
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
        if (index > length()) throw new MyException("Индекс за границами строки");
        StringList newList = new StringList(string);
        this.insertList(index, newList);
    }

    //Вставка по индексу
    public void insert(int index, StringList string) {
        if (index > length()) throw new MyException("Индекс за границами строки");
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
